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
import com.google.gson.*;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.Version;
import com.urswolfer.gerrit.client.rest.gson.DateDeserializer;
import com.urswolfer.gerrit.client.rest.gson.DateSerializer;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.entity.UrlEncodedFormEntityHC4;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntityHC4;
import org.apache.http.impl.auth.BasicSchemeHC4;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContextHC4;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class provides basic http access to the rest interface of a gerrit instance.
 *
 * @author Urs Wolfer
 */
public class GerritRestClient {

    private static final Pattern GERRIT_AUTH_PATTERN = Pattern.compile(".*?xGerritAuth=\"(.+?)\"");
    private static final int CONNECTION_TIMEOUT_MS = 30000;
    private static final String PREEMPTIVE_AUTH = "preemptive-auth";
    private static final Gson GSON = initGson();

    private final GerritAuthData authData;
    private final HttpRequestExecutor httpRequestExecutor;
    private final List<HttpClientBuilderExtension> httpClientBuilderExtensions;

    private final BasicCookieStoreHC4 cookieStore;
    private final LoginCache loginCache;

    public GerritRestClient(GerritAuthData authData,
                            HttpRequestExecutor httpRequestExecutor,
                            HttpClientBuilderExtension... httpClientBuilderExtensions) {
        this.authData = authData;
        this.httpRequestExecutor = httpRequestExecutor;
        this.httpClientBuilderExtensions = Arrays.asList(httpClientBuilderExtensions);

        cookieStore = new BasicCookieStoreHC4();
        loginCache = new LoginCache(authData, cookieStore);
    }

    public enum HttpVerb {
        GET, POST, DELETE, HEAD, PUT
    }

    public Gson getGson() {
        return GSON;
    }

    public JsonElement getRequest(String path) throws RestApiException {
        return request(path, null, HttpVerb.GET);
    }

    public JsonElement postRequest(String path, String requestBody) throws RestApiException {
        return request(path, requestBody, HttpVerb.POST);
    }

    public JsonElement putRequest(String path) throws RestApiException {
        return putRequest(path, null);
    }

    public JsonElement putRequest(String path, String requestBody) throws RestApiException {
        return request(path, requestBody, HttpVerb.PUT);
    }

    public JsonElement deleteRequest(String path) throws RestApiException {
        return request(path, null, HttpVerb.DELETE);
    }

    public JsonElement request(String path, String requestBody, HttpVerb verb) throws RestApiException {
        try {
            HttpResponse response = doRest(path, requestBody, verb);

            if (response.getStatusLine().getStatusCode() == 403 && loginCache.getGerritAuthOptional().isPresent()) {
                // handle expired sessions: try again with a fresh login
                loginCache.invalidate();
                response = doRest(path, requestBody, verb);
            }

            checkStatusCode(response);

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }
            InputStream resp = entity.getContent();
            JsonElement ret = parseResponse(resp);
            if (ret.isJsonNull()) {
                throw new RestApiException("Unexpectedly empty response.");
            }
            return ret;
        } catch (IOException e) {
            throw new RestApiException("Request failed.", e);
        }
    }

    public HttpResponse doRest(String path, String requestBody, HttpVerb verb) throws IOException, RestApiException {
        HttpContext httpContext = new BasicHttpContextHC4();
        HttpClientBuilder client = getHttpClient(httpContext);

        Optional<String> gerritAuthOptional = updateGerritAuthWhenRequired(httpContext, client);

        String uri = authData.getHost();
        // only use /a when http login is required (i.e. we haven't got a gerrit-auth cookie)
        // it would work in most cases also with /a, but it breaks with HTTP digest auth ("Forbidden" returned)
        if (authData.isLoginAndPasswordAvailable() && !gerritAuthOptional.isPresent()) {
            uri += "/a";
        }
        uri += path;

        HttpRequestBaseHC4 method;
        switch (verb) {
            case POST:
                method = new HttpPostHC4(uri);
                setRequestBody(requestBody, method);
                break;
            case GET:
                method = new HttpGetHC4(uri);
                break;
            case DELETE:
                method = new HttpGetHC4(uri);
                break;
            case PUT:
                method = new HttpPutHC4(uri);
                setRequestBody(requestBody, method);
                break;
            default:
                throw new IllegalStateException("Unknown or unsupported HttpVerb method: " + verb.toString());
        }
        if (gerritAuthOptional.isPresent()) {
            method.addHeader("X-Gerrit-Auth", gerritAuthOptional.get());
        }
        method.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());

        return httpRequestExecutor.execute(client, method, httpContext);
    }

    private void setRequestBody(String requestBody, HttpRequestBaseHC4 method) {
        if (requestBody != null) {
            ((HttpEntityEnclosingRequestBaseHC4) method).setEntity(new StringEntityHC4(requestBody, ContentType.APPLICATION_JSON));
        }
    }

    private Optional<String> updateGerritAuthWhenRequired(HttpContext httpContext, HttpClientBuilder client) throws IOException {
        if (!loginCache.getHostSupportsGerritAuth()) {
            // We do not not need a cookie here since we are sending credentials as HTTP basic / digest header again.
            // In fact cookies could hurt: googlesource.com Gerrit instances block requests which send a magic cookie
            // named "gi" with a 400 HTTP status (as of 01/29/15).
            cookieStore.clear();
            return Optional.absent();
        }
        Optional<Cookie> gerritAccountCookie = findGerritAccountCookie();
        if (!gerritAccountCookie.isPresent() || gerritAccountCookie.get().isExpired(new Date())) {
            return updateGerritAuth(httpContext, client);
        }
        return loginCache.getGerritAuthOptional();
    }

    private Optional<String> updateGerritAuth(HttpContext httpContext, HttpClientBuilder client) throws IOException {
        Optional<String> gerritAuthOptional = tryGerritHttpAuth(client, httpContext)
            .or(tryGerritHttpFormAuth(client, httpContext));
        loginCache.setGerritAuthOptional(gerritAuthOptional);
        return gerritAuthOptional;
    }

    /**
     * Handles LDAP auth (but not LDAP_HTTP) which uses a HTML form.
     */
    private Optional<String> tryGerritHttpFormAuth(HttpClientBuilder client, HttpContext httpContext) throws IOException {
        if (!authData.isLoginAndPasswordAvailable()) {
            return Optional.absent();
        }
        String loginUrl = authData.getHost() + "/login/";
        HttpPostHC4 method = new HttpPostHC4(loginUrl);
        List<BasicNameValuePair> parameters = Lists.newArrayList(
            new BasicNameValuePair("username", authData.getLogin()),
            new BasicNameValuePair("password", authData.getPassword())
        );
        method.setEntity(new UrlEncodedFormEntityHC4(parameters, Consts.UTF_8));
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
    private Optional<String> tryGerritHttpAuth(HttpClientBuilder client, HttpContext httpContext) throws IOException {
        String loginUrl = authData.getHost() + "/login/";
        HttpResponse loginResponse = httpRequestExecutor.execute(client, new HttpGetHC4(loginUrl), httpContext);
        return extractGerritAuth(loginResponse);
    }

    private Optional<String> extractGerritAuth(HttpResponse loginResponse) throws IOException {
        if (loginResponse.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
            Optional<Cookie> gerritAccountCookie = findGerritAccountCookie();
            if (gerritAccountCookie.isPresent()) {
                Matcher matcher = GERRIT_AUTH_PATTERN.matcher(EntityUtilsHC4.toString(loginResponse.getEntity(), Consts.UTF_8));
                if (matcher.find()) {
                    return Optional.of(matcher.group(1));
                }
            }
        }
        return Optional.absent();
    }

    private Optional<Cookie> findGerritAccountCookie() {
        List<Cookie> cookies = cookieStore.getCookies();
        return Iterables.tryFind(cookies, new Predicate<Cookie>() {
            @Override
            public boolean apply(Cookie cookie) {
                return cookie.getName().equals("GerritAccount");
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

        BasicCredentialsProviderHC4 credentialsProvider = getCredentialsProvider();
        client.setDefaultCredentialsProvider(credentialsProvider);

        if (authData.isLoginAndPasswordAvailable()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(authData.getLogin(), authData.getPassword()));

            BasicSchemeHC4 basicAuth = new BasicSchemeHC4();
            httpContext.setAttribute(PREEMPTIVE_AUTH, basicAuth);
            client.addInterceptorFirst(new PreemptiveAuthHttpRequestInterceptor(authData));
        }

        client.addInterceptorLast(new UserAgentHttpRequestInterceptor());

        for (HttpClientBuilderExtension httpClientBuilderExtension : httpClientBuilderExtensions) {
            client = httpClientBuilderExtension.extend(client, authData);
            credentialsProvider = (BasicCredentialsProviderHC4) httpClientBuilderExtension.extendCredentialProvider(client, credentialsProvider, authData);
        }

        return client;
    }

    /**
     * With this impl, it only returns the same credentials once. Otherwise it's possible that a loop will occur.
     * When server returns status code 401, the HTTP client provides the same credentials forever.
     * Since we create a new HTTP client for every request, we can handle it this way.
     */
    private BasicCredentialsProviderHC4 getCredentialsProvider() {
        return new BasicCredentialsProviderHC4() {
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

    private void checkStatusCode(HttpResponse response) throws HttpStatusException, IOException {
        StatusLine statusLine = response.getStatusLine();
        int code = statusLine.getStatusCode();
        switch (code) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
            case HttpStatus.SC_NO_CONTENT:
                return;
            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_PAYMENT_REQUIRED:
            case HttpStatus.SC_FORBIDDEN:
            default:
                String body = "<empty>";
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    body = EntityUtilsHC4.toString(entity).trim();
                }
                String message = String.format("Request not successful. Message: %s. Status-Code: %s. Content: %s.",
                        statusLine.getReasonPhrase(), statusLine.getStatusCode(), body);
                throw new HttpStatusException(statusLine.getStatusCode(), statusLine.getReasonPhrase(), message);
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

        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            AuthStateHC4 authState = (AuthStateHC4) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

            // if no auth scheme available yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(PREEMPTIVE_AUTH);
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(authData.getLogin(), authData.getPassword());
                authState.update(authScheme, creds);
            }
        }
    }

    private static class UserAgentHttpRequestInterceptor implements HttpRequestInterceptor {

        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            Header existingUserAgent = request.getFirstHeader(HttpHeaders.USER_AGENT);
            String userAgent = String.format("gerrit-rest-java-client/%s", Version.get());
            userAgent += " using " + existingUserAgent.getValue();
            request.setHeader(HttpHeaders.USER_AGENT, userAgent);
        }
    }

    private static Gson initGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Date.class, new DateSerializer());
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder.create();
    }
}
