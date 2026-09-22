/*
 * Copyright 2013-2026 Urs Wolfer
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

import static org.apache.http.HttpStatus.SC_OK;

import com.urswolfer.gerrit.client.rest.GerritAuthData;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Getting a session out of Gerrit, and deciding when one is needed.
 *
 * <p>A Gerrit that authenticates over HTTP hands out a {@code GerritAccount} cookie and an XSRF
 * token; with those in hand the requests that follow need no credentials, which is what makes this
 * worth doing rather than sending basic auth every time. Where the token comes from depends on the
 * server: a cookie on Gerrit >= 2.12, the start page HTML before that. Several kinds of Gerrit have
 * to be left alone entirely - an HTTP password must not be sent to the login page, and a
 * GitHub/OAuth instance would only loop - and {@link LoginCache} remembers which.
 */
class GerritAuthenticator {

    private static final Pattern GERRIT_AUTH_PATTERN = Pattern.compile("xGerritAuth=\"([^\"]+)\"");

    private final GerritAuthData authData;
    private final HttpRequestExecutor httpRequestExecutor;
    private final BasicCookieStore cookieStore;
    private final LoginCache loginCache;

    GerritAuthenticator(GerritAuthData authData,
                        HttpRequestExecutor httpRequestExecutor,
                        BasicCookieStore cookieStore) {
        this.authData = authData;
        this.httpRequestExecutor = httpRequestExecutor;
        this.cookieStore = cookieStore;
        this.loginCache = new LoginCache(authData, cookieStore);
    }

    LoginCache loginCache() {
        return loginCache;
    }

    /**
     * Whether a session obtained earlier is still being used, which is what makes a {@code 403}
     * worth retrying.
     */
    boolean hasSession() {
        return loginCache.getGerritAuthOptional().isPresent();
    }

    void invalidateSession() {
        loginCache.invalidate();
    }

    /**
     * The XSRF token to send with the request, logging in first if that is both possible and
     * necessary. Empty means this host wants credentials on every request instead.
     */
    Optional<String> authenticate(HttpClientBuilder client, HttpContext httpContext)
            throws IOException, HttpStatusException {
        if (!loginCache.getHostSupportsGerritAuth()) {
            // We do not need a cookie here since we are sending credentials as HTTP basic / digest header again.
            // In fact cookies could hurt: googlesource.com Gerrit instances block requests which send a magic cookie
            // named "gi" with a 400 HTTP status (as of 2015-01-29).
            cookieStore.clear();
            return Optional.empty();
        }
        if (authData.isHttpPassword()) {
            // Do not use a Gerrit HTTP password token to authenticate against the
            // login page. This will cause Gerrit to use the password to authenticate
            // against the configured authentication source (LDAP, etc) and potentially
            // lock the account.
            return Optional.empty();
        }

        if (loginCache.isGithubOAuthDetected()) {
            // When Gerrit is configured with GitHub/OAuth authentication, do not keep on
            // trying the /login page as it would just result in a continuous loop of failed
            // login attempts.
            return Optional.empty();
        }

        Optional<Cookie> gerritAccountCookie = findGerritAccountCookie();
        if (!gerritAccountCookie.isPresent()
            || gerritAccountCookie.get().isExpired(new Date())
            || !isSessionValid(client, httpContext)) {
            return login(httpContext, client);
        }
        return loginCache.getGerritAuthOptional();
    }

    private Optional<String> login(HttpContext httpContext, HttpClientBuilder client) throws IOException, HttpStatusException {
        Optional<String> gerritAuthOptional = tryGerritHttpAuth(client, httpContext);
        if (!gerritAuthOptional.isPresent()) {
            // only fall back to the form login - which posts the credentials in the request body and
            // starts another session - when the plain GET did not already authenticate us
            gerritAuthOptional = tryGerritHttpFormAuth(client, httpContext);
        }
        loginCache.setGerritAuthOptional(gerritAuthOptional);
        return gerritAuthOptional;
    }

    /**
     * Handles LDAP auth (but not LDAP_HTTP) which uses a HTML form.
     */
    private Optional<String> tryGerritHttpFormAuth(HttpClientBuilder client, HttpContext httpContext) throws IOException, HttpStatusException {
        if (!authData.isLoginAndPasswordAvailable()) {
            return Optional.empty();
        }
        String loginUrl = authData.getHost() + "/login/";
        HttpPost method = new HttpPost(loginUrl);
        List<BasicNameValuePair> parameters = Arrays.asList(
            new BasicNameValuePair("username", authData.getLogin()),
            new BasicNameValuePair("password", authData.getPassword())
        );
        method.setEntity(new UrlEncodedFormEntity(parameters, Consts.UTF_8));
        HttpResponse loginResponse = httpRequestExecutor.execute(client, method, httpContext);
        return extractGerritAuth(loginResponse, httpContext);
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
        return extractGerritAuth(loginResponse, httpContext);
    }

    private Optional<String> extractGerritAuth(HttpResponse loginResponse, HttpContext httpContext) throws IOException, HttpStatusException {
        try {
            HttpResponses.checkStatusCodeServerError(loginResponse);
            if (!loginCache.isGitHubOAuthRequested(httpContext) && loginResponse.getStatusLine().getStatusCode() != HttpStatus.SC_UNAUTHORIZED) {
                Optional<String> xsrfCookie = getXsrfCookie();
                if (xsrfCookie.isPresent()) {
                    return xsrfCookie;
                }
                return getXsrfFromHtmlBody(loginResponse);
            }
            return Optional.empty();
        } finally {
            // the connection behind a streaming response is only released once its entity has been
            // consumed, and not every branch above reads the login page body
            EntityUtils.consume(loginResponse.getEntity());
        }
    }

    private boolean isSessionValid(HttpClientBuilder client, HttpContext httpContext) throws IOException {
        String accountsSelfUrl = authData.getHost() + "/accounts/self";
        // HEAD could be used instead when we only support Gerrit >=2.12; https://gerrit-review.googlesource.com/c/80962
        HttpResponse response = httpRequestExecutor.execute(client, new HttpGet(accountsSelfUrl), httpContext);
        try {
            return response.getStatusLine().getStatusCode() == SC_OK;
        } finally {
            // only the status is wanted, but the connection is not released until the body is read
            EntityUtils.consumeQuietly(response.getEntity());
        }
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
        return Optional.empty();
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
        return Optional.empty();
    }

    private Optional<Cookie> findGerritAccountCookie() {
        return findCookie("GerritAccount");
    }

    private Optional<Cookie> findCookie(final String cookieName) {
        List<Cookie> cookies = cookieStore.getCookies();
        return cookies.stream().filter(cookie -> cookie.getName().equals(cookieName)).findFirst();
    }
}
