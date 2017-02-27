/*
 * Copyright 2013-2015 Urs Wolfer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.urswolfer.gerrit.client.rest.http;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.Version;
import com.urswolfer.gerrit.client.rest.gson.GsonFactory;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Urs Wolfer
 */
public class GerritRestClient implements RestClient {

    private static final String JSON_MIME_TYPE = ContentType.APPLICATION_JSON.getMimeType();
    private static final Pattern GERRIT_AUTH_PATTERN = Pattern.compile(".*?xGerritAuth=\"(.+?)\"");
    private static final int CONNECTION_TIMEOUT_MS = 30000;
    private static final String PREEMPTIVE_AUTH = "preemptive-auth";
    private static final Gson GSON = GsonFactory.create();

    private final GerritAuthData authData;
    private final HttpRequestExecutor httpRequestExecutor;
    private final List<HttpClientBuilderExtension> httpClientBuilderExtensions;

    private final BasicCookieStore cookieStore;
    private final LoginCache loginCache;

    public GerritRestClient(GerritAuthData authData,
                            HttpRequestExecutor httpRequestExecutor,
                            HttpClientBuilderExtension... httpClientBuilderExtensions) {
        this.authData = authData;
        this.httpRequestExecutor = httpRequestExecutor;
        this.httpClientBuilderExtensions = Arrays.asList(httpClientBuilderExtensions);

        cookieStore = new BasicCookieStore();
        loginCache = new LoginCache(authData, cookieStore);
    }

    @Override
    public Gson getGson() {
        return GSON;
    }

    @Override
    public JsonElement getRequest(String path) throws RestApiException {
        return requestJson(path, null, HttpVerb.GET);
    }

    @Override
    public JsonElement postRequest(String path) throws RestApiException {
        return postRequest(path, null);
    }

    @Override
    public JsonElement postRequest(String path, String requestBody) throws RestApiException {
        return requestJson(path, requestBody, HttpVerb.POST);
    }

    @Override
    public JsonElement putRequest(String path) throws RestApiException {
        return putRequest(path, null);
    }

    @Override
    public JsonElement putRequest(String path, String requestBody) throws RestApiException {
        return requestJson(path, requestBody, HttpVerb.PUT);
    }

    @Override
    public JsonElement deleteRequest(String path) throws RestApiException {
        return requestJson(path, null, HttpVerb.DELETE);
    }

    @Override
    public JsonElement requestJson(String path, String requestBody, HttpVerb verb) throws RestApiException {
        try {
            HttpResponse response = requestRest(path, requestBody, verb);

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }

            checkContentType(entity);

            JsonElement ret = parseResponse(entity.getContent());
            if (ret.isJsonNull()) {
                throw new RestApiException("Unexpectedly empty response.");
            }
            return ret;
        } catch (IOException e) {
            throw new RestApiException("Request failed.", e);
        }
    }

    @Override
    public HttpResponse requestRest(String path,
                                    String requestBody,
                                    HttpVerb verb) throws IOException, HttpStatusException {
        return requestRest(path, requestBody, verb, false);
    }

    private HttpResponse requestRest(String path,
                                     String requestBody,
                                     HttpVerb verb,
                                     boolean isRetry) throws IOException, HttpStatusException {
        BasicHeader acceptHeader = new BasicHeader("Accept", JSON_MIME_TYPE);
        return request(path, requestBody, verb, isRetry, acceptHeader);
    }

    @Override
    public HttpResponse request(String path,
                                String requestBody,
                                HttpVerb verb,
                                Header... headers) throws IOException, HttpStatusException {
        return request(path, requestBody, verb, false, headers);
    }

    private HttpResponse request(String path,
                                 String requestBody,
                                 HttpVerb verb,
                                 boolean isRetry,
                                 Header... headers) throws IOException, HttpStatusException {
        HttpContext httpContext = new BasicHttpContext();
        HttpClientBuilder client = getHttpClient(httpContext);

        Optional<String> gerritAuthOptional = updateGerritAuthWhenRequired(httpContext, client);

        String uri = authData.getHost();
        // only use /a when http login is required (i.e. we haven't got a gerrit-auth cookie)
        // it would work in most cases also with /a, but it breaks with HTTP digest auth ("Forbidden" returned)
        if (authData.isLoginAndPasswordAvailable() && !gerritAuthOptional.isPresent()) {
            uri += "/a";
        }
        uri += path;

        HttpRequestBase method;
        switch (verb) {
            case POST:
                method = new HttpPost(uri);
                setRequestBody(requestBody, method);
                break;
            case GET:
                method = new HttpGet(uri);
                break;
            case DELETE:
                method = new HttpDelete(uri);
                break;
            case PUT:
                method = new HttpPut(uri);
                setRequestBody(requestBody, method);
                break;
            default:
                throw new IllegalStateException("Unknown or unsupported HttpVerb method: " + verb.toString());
        }
        if (gerritAuthOptional.isPresent()) {
            method.addHeader("X-Gerrit-Auth", gerritAuthOptional.get());
        }

        for (Header header : headers) {
            method.addHeader(header);
        }

        HttpResponse response = httpRequestExecutor.execute(client, method, httpContext);

        if (!isRetry && response.getStatusLine().getStatusCode() == 403 && loginCache.getGerritAuthOptional().isPresent()) {
            // handle expired sessions: try again with a fresh login
            loginCache.invalidate();
            response = requestRest(path, requestBody, verb, true);
        }

        checkStatusCode(response);

        return response;
    }

    private void setRequestBody(String requestBody, HttpRequestBase method) {
        if (requestBody != null) {
            ((HttpEntityEnclosingRequestBase) method).setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
        }
    }

    private Optional<String> updateGerritAuthWhenRequired(HttpContext httpContext, HttpClientBuilder client) throws IOException, HttpStatusException {
        if (!loginCache.getHostSupportsGerritAuth()) {
            // We do not not need a cookie here since we are sending credentials as HTTP basic / digest header again.
            // In fact cookies could hurt: googlesource.com Gerrit instances block requests which send a magic cookie
            // named "gi" with a 400 HTTP status (as of 01/29/15).
            cookieStore.clear();
            return Optional.absent();
        } else if (authData.isHttpPassword()) {
            // Do not use a Gerrit HTTP password token to authenticate against the
            // login page.  This will cause Gerrit to use the password to authenticate
            // against the configured authentication source (LDAP, etc) and potentially
            // lock the account.
            return Optional.absent();
        }

        Optional<Cookie> gerritAccountCookie = findGerritAccountCookie();
        if (!gerritAccountCookie.isPresent() || gerritAccountCookie.get().isExpired(new Date())) {
            return updateGerritAuth(httpContext, client);
        }
        return loginCache.getGerritAuthOptional();
    }

    private Optional<String> updateGerritAuth(HttpContext httpContext, HttpClientBuilder client) throws IOException, HttpStatusException {
        Optional<String> gerritAuthOptional = tryGerritHttpAuth(client, httpContext)
            .or(tryGerritHttpFormAuth(client, httpContext));
        loginCache.setGerritAuthOptional(gerritAuthOptional);
        return gerritAuthOptional;
    }

    /**
     * Handles LDAP auth (but not LDAP_HTTP) which uses a HTML form.
     */
    private Optional<String> tryGerritHttpFormAuth(HttpClientBuilder client, HttpContext httpContext) throws IOException, HttpStatusException {
        if (!authData.isLoginAndPasswordAvailable()) {
            return Optional.absent();
        }
        String loginUrl = authData.getHost() + "/login/";
        HttpPost method = new HttpPost(loginUrl);
        List<BasicNameValuePair> parameters = Lists.newArrayList(
            new BasicNameValuePair("username", authData.getLogin()),
            new BasicNameValuePair("password", authData.getPassword())
        );
        method.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
        HttpResponse loginResponse = httpRequestExecutor.execute(client, method, httpContext);
        return extractGerritAuth(loginResponse);
    }

    /**
     * Try to authenticate against Gerrit instances with HTTP auth (not OAuth or something like that).
     * In case of success we get a GerritAccount cookie. In that case no more login credentials need to be sent as
     * long as we use the *same* HTTP client. Even requests against authenticated rest api (/a) will be processed
     * with the GerritAccount cookie.
     *
     * This is a workaround for "double" HTTP authentication (i.e. reverse proxy *and* Gerrit do HTTP authentication
     * for rest api (/a)).
     *
     * Following old notes from README about the issue:
     * If you have correctly set up a HTTP Password in Gerrit, but still have authentication issues, your Gerrit instance
     * might be behind a HTTP Reverse Proxy (like Nginx or Apache) with enabled HTTP Authentication. You can identify that if
     * you have to enter an username and password (browser password request) for opening the Gerrit web interface. Since this
     * plugin uses Gerrit REST API (with authentication enabled), you need to tell your system administrator that he should
     * disable HTTP Authentication for any request to <code>/a</code> path (e.g. https://git.example.com/a). For these requests
     * HTTP Authentication is done by Gerrit (double HTTP Authentication will not work). For more information see
     * [Gerrit documentation].
     * [Gerrit documentation]: https://gerrit-review.googlesource.com/Documentation/rest-api.html#authentication
     */
    private Optional<String> tryGerritHttpAuth(HttpClientBuilder client, HttpContext httpContext) throws IOException, HttpStatusException {
        String loginUrl = authData.getHost() + "/login/";
        HttpResponse loginResponse = httpRequestExecutor.execute(client, new HttpGet(loginUrl), httpContext);
        return extractGerritAuth(loginResponse);
    }

    private Optional<String> extractGerritAuth(HttpResponse loginResponse) throws IOException, HttpStatusException {
        checkStatusCodeServerError(loginResponse);
        if (loginResponse.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
            return getXsrfCookie().or(getXsrfFromHtmlBody(loginResponse));
        }
        return Optional.absent();
    }

    /**
     * In Gerrit >= 2.12 the XSRF token got moved to a cookie.
     * Introduced in: https://gerrit-review.googlesource.com/72031/
     */
    private Optional<String> getXsrfCookie() {
        Optional<Cookie> xsrfCookie = findCookie("XSRF_TOKEN");
        if (xsrfCookie.isPresent()) {
            return Optional.of(xsrfCookie.get().getValue());
        }
        return Optional.absent();
    }


    /**
     * In Gerrit < 2.12 the XSRF token was included in the start page HTML.
     */
    private Optional<String> getXsrfFromHtmlBody(HttpResponse loginResponse) throws IOException {
        Optional<Cookie> gerritAccountCookie = findGerritAccountCookie();
        if (gerritAccountCookie.isPresent()) {
            Matcher matcher = GERRIT_AUTH_PATTERN.matcher(EntityUtils.toString(loginResponse.getEntity(), Consts.UTF_8));
            if (matcher.find()) {
                return Optional.of(matcher.group(1));
            }
        }
        return Optional.absent();
    }

    private Optional<Cookie> findGerritAccountCookie() {
        return findCookie("GerritAccount");
    }

    private Optional<Cookie> findCookie(final String cookieName) {
        List<Cookie> cookies = cookieStore.getCookies();
        return Iterables.tryFind(cookies, new Predicate<Cookie>() {
            @Override
            public boolean apply(Cookie cookie) {
                return cookie.getName().equals(cookieName);
            }
        });
    }

    private HttpClientBuilder getHttpClient(HttpContext httpContext) {
        HttpClientBuilder client = HttpClients.custom();

        client.useSystemProperties(); // see also: com.intellij.util.net.ssl.CertificateManager

        // we need to get redirected result after login (which is done with POST) for extracting xGerritAuth
        client.setRedirectStrategy(new LaxRedirectStrategy());

        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        RequestConfig.Builder requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT_MS) // how long it takes to connect to remote host
                .setSocketTimeout(CONNECTION_TIMEOUT_MS) // how long it takes to retrieve data from remote host
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS);
        client.setDefaultRequestConfig(requestConfig.build());

        CredentialsProvider credentialsProvider = getCredentialsProvider();
        client.setDefaultCredentialsProvider(credentialsProvider);

        if (authData.isLoginAndPasswordAvailable()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(authData.getLogin(), authData.getPassword()));

            BasicScheme basicAuth = new BasicScheme();
            httpContext.setAttribute(PREEMPTIVE_AUTH, basicAuth);
            client.addInterceptorFirst(new PreemptiveAuthHttpRequestInterceptor(authData));
        }

        client.addInterceptorLast(new UserAgentHttpRequestInterceptor());

        for (HttpClientBuilderExtension httpClientBuilderExtension : httpClientBuilderExtensions) {
            client = httpClientBuilderExtension.extend(client, authData);
            credentialsProvider = httpClientBuilderExtension.extendCredentialProvider(client, credentialsProvider, authData);
        }

        return client;
    }

    /**
     * With this impl, it only returns the same credentials once. Otherwise it's possible that a loop will occur.
     * When server returns status code 401, the HTTP client provides the same credentials forever.
     * Since we create a new HTTP client for every request, we can handle it this way.
     */
    private BasicCredentialsProvider getCredentialsProvider() {
        return new BasicCredentialsProvider() {
            private Set<AuthScope> authAlreadyTried = Sets.newHashSet();

            @Override
            public Credentials getCredentials(AuthScope authscope) {
                if (authAlreadyTried.contains(authscope)) {
                    return null;
                }
                authAlreadyTried.add(authscope);
                return super.getCredentials(authscope);
            }
        };
    }

    private JsonElement parseResponse(InputStream response) throws IOException {
        Reader reader = new InputStreamReader(response, Consts.UTF_8);
        try {
            return new JsonParser().parse(reader);
        } catch (JsonSyntaxException jse) {
            throw new IOException(String.format("Couldn't parse response: %n%s", CharStreams.toString(reader)), jse);
        } finally {
            reader.close();
        }
    }

    /**
     * @throws HttpStatusException on any error (client 4xx and server 5xx).
     */
    private void checkStatusCode(HttpResponse response) throws HttpStatusException, IOException {
        checkStatusCodeClientError(response);
        checkStatusCodeServerError(response);
    }

    /**
     * @throws HttpStatusException on client error (4xx).
     */
    private void checkStatusCodeClientError(HttpResponse response) throws HttpStatusException, IOException {
        checkStatusCodeError(response, 400, 499);
    }

    /**
     * @throws HttpStatusException on server error (5xx).
     */
    private void checkStatusCodeServerError(HttpResponse response) throws HttpStatusException, IOException {
        checkStatusCodeError(response, 500, 599);
    }

    private void checkStatusCodeError(HttpResponse response, int errorIfMin, int errorIfMax) throws HttpStatusException, IOException {
        StatusLine statusLine = response.getStatusLine();
        int code = statusLine.getStatusCode();
        if (code >= errorIfMin && code <= errorIfMax) {
            throwHttpStatusException(response);
        }
    }

    private void throwHttpStatusException(HttpResponse response) throws IOException, HttpStatusException {
        StatusLine statusLine = response.getStatusLine();
        String body = "<empty>";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = EntityUtils.toString(entity).trim();
        }
        String message = String.format("Request not successful. Message: %s. Status-Code: %s. Content: %s.",
                statusLine.getReasonPhrase(), statusLine.getStatusCode(), body);
        throw new HttpStatusException(statusLine.getStatusCode(), statusLine.getReasonPhrase(), message);
    }

    private void checkContentType(HttpEntity entity) throws RestApiException {
        Header contentType = entity.getContentType();
        if (contentType != null && !contentType.getValue().contains(JSON_MIME_TYPE)) {
            throw new RestApiException(String.format("Expected JSON but got '%s'.", contentType.getValue()));
        }
    }

    /**
     * With preemptive auth, it will send the basic authentication response even before the server gives an unauthorized
     * response in certain situations, thus reducing the overhead of making the connection again.
     *
     * Based on:
     * https://subversion.jfrog.org/jfrog/build-info/trunk/build-info-client/src/main/java/org/jfrog/build/client/PreemptiveHttpClient.java
     */
    private static class PreemptiveAuthHttpRequestInterceptor implements HttpRequestInterceptor {
        private GerritAuthData authData;

        public PreemptiveAuthHttpRequestInterceptor(GerritAuthData authData) {
            this.authData = authData;
        }

        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            // never ever send credentials preemptively to a host which is not the configured Gerrit host
            if (!isForGerritHost(request)) {
                return;
            }

            AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

            // if no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(PREEMPTIVE_AUTH);
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(authData.getLogin(), authData.getPassword());
                authState.update(authScheme, creds);
            }
        }

        /**
         * Checks if request is intended for Gerrit host.
         */
        private boolean isForGerritHost(HttpRequest request) {
            if (!(request instanceof HttpRequestWrapper)) return false;
            HttpRequest originalRequest = ((HttpRequestWrapper) request).getOriginal();
            if (!(originalRequest instanceof HttpRequestBase)) return false;
            URI uri = ((HttpRequestBase) originalRequest).getURI();
            URI authDataUri = URI.create(authData.getHost());
            if (uri == null || uri.getHost() == null) return false;
            boolean hostEquals = uri.getHost().equals(authDataUri.getHost());
            boolean portEquals = uri.getPort() == authDataUri.getPort();
            return hostEquals && portEquals;
        }
    }

    private static class UserAgentHttpRequestInterceptor implements HttpRequestInterceptor {

        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            Header existingUserAgent = request.getFirstHeader(HttpHeaders.USER_AGENT);
            String userAgent = String.format("gerrit-rest-java-client/%s", Version.get());
            userAgent += " using " + existingUserAgent.getValue();
            request.setHeader(HttpHeaders.USER_AGENT, userAgent);
        }
    }
}
