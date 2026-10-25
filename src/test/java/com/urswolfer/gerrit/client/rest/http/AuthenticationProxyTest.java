/*
 * Copyright 2026 Urs Wolfer
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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.RestClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Runs the client against a stand-in for an authentication proxy in front of Gerrit, which
 * redirects to a sign-in page on another host until a request carries its cookie.
 */
public class AuthenticationProxyTest {

    private HttpServer proxy;
    private HttpServer signIn;
    /** Whether the proxy shows its sign-in page itself rather than redirecting to it. */
    private volatile boolean pageRightAway;
    /** What the sign-in page got: path, Accept, Cookie and X-Gerrit-Auth. */
    private final List<String> signInRequests = new CopyOnWriteArrayList<>();
    private final List<String> proxyCookies = new CopyOnWriteArrayList<>();

    @BeforeMethod
    public void startServers() throws IOException {
        pageRightAway = false;
        signInRequests.clear();
        proxyCookies.clear();
        signIn = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        signIn.createContext("/", exchange -> {
            signInRequests.add(exchange.getRequestURI().getPath() + " accept=" + header(exchange, "Accept")
                + " cookie=" + header(exchange, "Cookie") + " xsrf=" + header(exchange, "X-Gerrit-Auth")
                + " auth=" + header(exchange, "Authorization"));
            if (exchange.getRequestURI().getPath().equals("/challenge")) {
                exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"sign-in\"");
                answer(exchange, 401, "text/html", "<html><body>Who are you?</body></html>");
                return;
            }
            answer(exchange, 200, "text/html; charset=utf-8", "<html><body>Sign in</body></html>");
        });
        signIn.start();
        proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxy.createContext("/", exchange -> {
            String cookie = header(exchange, "Cookie");
            proxyCookies.add(cookie);
            String path = exchange.getRequestURI().getPath();
            boolean granted = cookie.contains("ProxyAccess=granted");
            if (path.endsWith("/challenge")) { // with a login, under /a
                exchange.getResponseHeaders().add("Location", "http://localhost:" + signIn.getAddress().getPort() + "/challenge");
                answer(exchange, 302, "text/html", "");
            } else if (!granted || path.equals("/elsewhere")) {
                if (pageRightAway) {
                    answer(exchange, 200, "text/html", "<html><body>Sign in</body></html>");
                    return;
                }
                // "localhost" rather than 127.0.0.1: another host, as far as cookies go
                exchange.getResponseHeaders().add("Location",
                    "http://localhost:" + signIn.getAddress().getPort() + "/signin?state=secret");
                answer(exchange, 302, "text/html", "");
            } else if (path.equals("/renew/")) {
                exchange.getResponseHeaders().add("Set-Cookie", "ProxyAccess=renewed; Path=/");
                answer(exchange, 200, "application/json", ")]}'\n[]");
            } else if (path.startsWith("/changes/")) {
                answer(exchange, 200, "application/json; charset=utf-8", ")]}'\n[]");

            } else {
                answer(exchange, 404, "text/plain", "Not found");
            }
        });
        proxy.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopServers() {
        proxy.stop(0);
        signIn.stop(0);
    }

    @Test
    public void testSignInPageReportedRatherThanParsed() {
        RestApiException failure = Assert.expectThrows(RestApiException.class,
            () -> client(Collections.emptyMap()).getRequest("/changes/"));

        Truth.assertThat(failure.getCause()).isInstanceOf(SignInPageException.class);
        Truth.assertThat(((SignInPageException) failure.getCause()).getPageOrigin()).isEqualTo(signInOrigin());
        Truth.assertThat(failure.getMessage()).contains("answered with a web page from " + signInOrigin());
        Truth.assertThat(failure.getMessage()).doesNotContain("secret");
    }

    @Test
    public void testSignInPageAskedForOnce() {
        Assert.expectThrows(RestApiException.class, () -> client(Collections.emptyMap()).getRequest("/changes/"));

        Truth.assertThat(signInRequests.stream().filter(request -> request.contains("application/json"))
            .collect(Collectors.toList())).hasSize(1);
    }

    @Test
    public void testSignInPageShownRightAwayReported() {
        pageRightAway = true;

        RestApiException failure = Assert.expectThrows(RestApiException.class,
            () -> client(Collections.emptyMap()).getRequest("/changes/"));

        Truth.assertThat(((SignInPageException) failure.getCause()).getPageOrigin()).isEqualTo(proxyOrigin());
    }

    /**
     * The HTTP client follows no redirect of a PUT, so it never gets to the page itself.
     */
    @Test
    public void testRedirectOfAPutReported() {
        RestApiException failure = Assert.expectThrows(RestApiException.class,
            () -> client(Collections.emptyMap()).putRequest("/accounts/self/starred.changes/1"));

        Truth.assertThat(((SignInPageException) failure.getCause()).getPageOrigin()).isEqualTo(signInOrigin());
    }

    /**
     * Such as the commit-msg hook from a proxy which shows its sign-in page right away.
     */
    @Test
    public void testPageInsteadOfAFileReported() {
        pageRightAway = true;

        SignInPageException page = Assert.expectThrows(SignInPageException.class,
            () -> client(Collections.emptyMap()).request("/tools/hooks/commit-msg", null, RestClient.HttpVerb.GET));

        Truth.assertThat(page.getPageOrigin()).isEqualTo(proxyOrigin());
    }

    /**
     * Such as the commit-msg hook, which would be the sign-in page otherwise.
     */
    @Test
    public void testPageAskedForReportedWhereItComesFromAnotherHost() {
        SignInPageException page = Assert.expectThrows(SignInPageException.class,
            () -> client(Collections.emptyMap()).request("/tools/hooks/commit-msg", null, RestClient.HttpVerb.GET));

        Truth.assertThat(page.getPageOrigin()).isEqualTo(signInOrigin());
    }

    @Test
    public void testCookiesLetTheRequestThrough() throws Exception {
        Truth.assertThat(client(Collections.singletonMap("ProxyAccess", "granted")).getRequest("/changes/")
            .getAsJsonArray().size()).isEqualTo(0);
        Truth.assertThat(signInRequests).isEmpty();
    }

    @Test
    public void testCookiesSentWhereTheUrlHasCapitals() throws Exception {
        Truth.assertThat(((GerritRestClient) new GerritRestApiFactory().create(new MutableCookies(
                "http://LOCALHOST:" + proxy.getAddress().getPort(), Collections.singletonMap("ProxyAccess", "granted")))
            .restClient()).getRequest("/changes/").getAsJsonArray().size()).isEqualTo(0);
    }

    @Test
    public void testCookiesNotSentToAnotherHost() {
        Assert.expectThrows(RestApiException.class,
            () -> client(Collections.singletonMap("ProxyAccess", "granted")).getRequest("/elsewhere"));

        Truth.assertThat(signInRequests).hasSize(1);
        Truth.assertThat(signInRequests.get(0)).contains("cookie= ");
    }

    @Test
    public void testStoredCookieRatherThanOneTheServerSets() throws Exception {
        GerritRestClient client = client(Collections.singletonMap("ProxyAccess", "granted"));
        client.getRequest("/renew/");
        proxyCookies.clear();

        client.getRequest("/changes/");

        Truth.assertThat(proxyCookies).isNotEmpty();
        for (String cookie : proxyCookies) {
            Truth.assertThat(cookie).contains("ProxyAccess=granted");
            Truth.assertThat(cookie).doesNotContain("renewed");
        }
    }

    /**
     * The sign-in page of a proxy may ask for a login and password itself, which are not Gerrit's to give.
     */
    @Test
    public void testPasswordNotSentToAnotherHostWhichAsks() {
        GerritRestClient client = (GerritRestClient) new GerritRestApiFactory().create(
            new MutableCookies(proxyOrigin(), "user", "secret", Collections.singletonMap("ProxyAccess", "granted")))
            .restClient();

        RestApiException failure = Assert.expectThrows(RestApiException.class, () -> client.getRequest("/challenge"));

        // a sign-in, which a new Gerrit password would not mend
        Truth.assertThat(failure.getCause()).isInstanceOf(SignInPageException.class);
        Truth.assertThat(signInRequests).isNotEmpty();
        for (String request : signInRequests) {
            Truth.assertThat(request).endsWith("auth=");
        }
    }

    /**
     * A login which met the sign-in page took it for the way the host logs in, which a sign-in changes.
     */
    @Test
    public void testChangedCookiesStartANewLogin() {
        MutableCookies authData = new MutableCookies(proxyOrigin());
        GerritRestClient client = new GerritRestClient(authData, new HttpRequestExecutor());
        Assert.expectThrows(RestApiException.class, () -> client.getRequest("/changes/"));
        Truth.assertThat(client.loginCache().getHostSupportsGerritAuth()).isFalse();

        authData.cookies = Collections.singletonMap("ProxyAccess", "granted");

        Truth.assertThat(client.loginCache().getHostSupportsGerritAuth()).isTrue();
    }

    @Test
    public void testXsrfTokenAndCookiesOnlyToTheGerritHost() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies("https://review.example.org", Collections.singletonMap("ProxyAccess", "granted")));
        BasicCookieStore shared = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);

        BasicHttpRequest toGerrit = request();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("REVIEW.example.org", 443, "https"));
        interceptor.process(toGerrit, context);
        Truth.assertThat(toGerrit.getFirstHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER)).isNotNull();
        Truth.assertThat(names(context)).containsExactly("ProxyAccess");
        Truth.assertThat(shared.getCookies()).isEmpty();

        // the HTTP client's matching would send a cookie of review.example.org here, as the name ends in it
        BasicHttpRequest elsewhere = request();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("notreview.example.org", 443, "https"));
        interceptor.process(elsewhere, context);
        Truth.assertThat(elsewhere.getFirstHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER)).isNull();
        Truth.assertThat(names(context)).isEmpty();
        Truth.assertThat(shared.getCookies()).isEmpty();
    }

    @Test
    public void testCookiesOfTheServerKeptInTheSharedStore() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies("https://review.example.org", Collections.singletonMap("ProxyAccess", "granted")));
        BasicCookieStore shared = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", 443, "https"));
        interceptor.process(request(), context);

        BasicClientCookie session = new BasicClientCookie("GerritAccount", "session");
        session.setDomain("review.example.org");
        context.getCookieStore().addCookie(session);
        shared.clear(); // as a new login does, while the request goes on

        Truth.assertThat(names(context)).containsExactly("ProxyAccess");
    }

    @Test
    public void testCookiesAndXsrfTokenNotOverPlainHttpWhereGerritHasHttps() {
        Truth.assertThat(sent("https://review.example.org", new HttpHost("review.example.org", -1, "http"))).isFalse();
    }

    /**
     * As after a redirect from http to https, or to another port of the host.
     */
    @Test
    public void testCookiesAndXsrfTokenToAnotherSchemeOrPortOfTheHost() {
        Truth.assertThat(sent("http://review.example.org", new HttpHost("review.example.org", -1, "https"))).isTrue();
        Truth.assertThat(sent("https://review.example.org", new HttpHost("review.example.org", 8443, "https"))).isTrue();
    }

    /**
     * java.net.URI has no host for it, which the HTTP client gets along with.
     */
    @Test
    public void testXsrfTokenKeptForAHostWithAnUnderscore() {
        Truth.assertThat(sent("https://gerrit_srv.corp.local", new HttpHost("gerrit_srv.corp.local", -1, "https"))).isTrue();
    }

    private static boolean sent(String gerritUrl, HttpHost target) {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies(gerritUrl, Collections.singletonMap("ProxyAccess", "granted")));
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(new BasicCookieStore());
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, target);
        BasicHttpRequest request = request();

        interceptor.process(request, context);

        boolean xsrf = request.getFirstHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER) != null;
        boolean cookies = !names(context).isEmpty();
        Truth.assertThat(cookies).isEqualTo(xsrf);
        return xsrf;
    }

    @Test
    public void testServerCookieOfAnyDomainGivesWayToTheStoredOne() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies("https://review.example.org", Collections.singletonMap("ProxyAccess", "granted")));
        BasicCookieStore shared = new BasicCookieStore();
        BasicClientCookie fromServer = new BasicClientCookie("ProxyAccess", "other");
        fromServer.setDomain("example.org");
        shared.addCookie(fromServer);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(context.getCookieStore().getCookies()).hasSize(1);
        Truth.assertThat(context.getCookieStore().getCookies().get(0).getValue()).isEqualTo("granted");
    }

    /**
     * As a server which redirects from http to https: its refusal is still one of Gerrit's.
     */
    @Test
    public void testRefusalOfGerritsHostByAnotherSchemeNotASignInPage() throws Exception {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException.throwIfSignInPage(
            new BasicHttpResponse(HttpVersion.HTTP_1_1, 401, "Unauthorized"), context, "http://review.example.org");
    }

    @Test
    public void testOriginOfARedirectWithoutUserInfoAndWithAScheme() {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException page = Assert.expectThrows(SignInPageException.class, () -> SignInPageException
            .throwIfSignInPage(redirect(307, "//user:token@sso.example.org:8443/authorize?state=secret"), context,
                "https://review.example.org"));

        Truth.assertThat(page.getPageOrigin()).isEqualTo("https://sso.example.org:8443");
    }

    /**
     * Its name ends in Gerrit's host, which the HTTP client's cookie matching would send them to.
     */
    @Test
    public void testCookiesOfGerritNotToAnotherHost() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(new MutableCookies("https://review.example.org"));
        BasicCookieStore shared = new BasicCookieStore();
        BasicClientCookie session = new BasicClientCookie("GerritAccount", "session");
        session.setDomain("review.example.org");
        shared.addCookie(session);
        BasicClientCookie other = new BasicClientCookie("sso", "state");
        other.setDomain("auth.review.example.org");
        shared.addCookie(other);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("auth.review.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(names(context)).containsExactly("sso");
    }

    /**
     * Such as the sign-in of oauth2-proxy, which a PUT does not follow.
     */
    @Test
    public void testRedirectNotFollowedToTheSameHostReported() {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException page = Assert.expectThrows(SignInPageException.class, () -> SignInPageException
            .throwIfSignInPage(redirect(302, "/oauth2/start?rd=%2Fchanges%2F"), context, "https://review.example.org"));

        Truth.assertThat(page.getPageOrigin()).isEqualTo("https://review.example.org");
    }

    @Test
    public void testCookiesOfGerritsDomainNotToAnotherHost() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(new MutableCookies("https://review.example.org"));
        BasicCookieStore shared = new BasicCookieStore();
        BasicClientCookie session = new BasicClientCookie("GerritAccount", "session");
        session.setDomain("example.org");
        shared.addCookie(session);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("sso.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(names(context)).isEmpty();
    }

    /**
     * Such as a single sign-on which checks on the way through its pages that the cookie it set came back.
     */
    @Test
    public void testCookiesOfAnotherHostOnGerritsDomainToThatHost() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(new MutableCookies("https://review.example.org"));
        BasicCookieStore shared = new BasicCookieStore();
        BasicClientCookie check = new BasicClientCookie("SMSESSION", "set by the sign-in");
        check.setDomain("example.org");
        shared.addCookie(check);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("sso.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(names(context)).containsExactly("SMSESSION");
    }

    @Test
    public void testRedirectNotFollowedToALocationNotToReadReported() {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException page = Assert.expectThrows(SignInPageException.class, () -> SignInPageException
            .throwIfSignInPage(redirect(302, "https://sso.example.org/authorize?return=a|b c"), context,
                "https://review.example.org"));

        Truth.assertThat(page.getPageOrigin()).isEqualTo("https://review.example.org");
    }

    @Test
    public void testOriginOfARedirectToAHostWithAnUnderscore() {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException page = Assert.expectThrows(SignInPageException.class, () -> SignInPageException
            .throwIfSignInPage(redirect(307, "https://sso_login.example.org/authorize"), context,
                "https://review.example.org"));

        Truth.assertThat(page.getPageOrigin()).isEqualTo("https://sso_login.example.org");
    }

    /**
     * The sign-in has nothing to go by where the answer does not say where it came from, or Gerrit's url
     * cannot be read.
     */
    @Test
    public void testNoSignInPageWhereItsHostIsNotKnown() throws Exception {
        BasicHttpResponse page = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        page.setEntity(new StringEntity("<html></html>", ContentType.TEXT_HTML));
        HttpClientContext noTarget = HttpClientContext.create();
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException.throwIfSignInPage(page, noTarget, "https://review.example.org");
        SignInPageException.throwIfSignInPage(page, context, "http://review example.org");
    }

    @Test
    public void testPageWithoutABodyOrOfAnotherTypeIsNotASignInPage() throws Exception {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));
        BasicHttpResponse json = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
        json.setEntity(new StringEntity("[]", ContentType.APPLICATION_JSON));

        SignInPageException.throwIfSignInPage(
            new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK"), context, "https://review.example.org");
        SignInPageException.throwIfSignInPage(json, context, "https://review.example.org");
    }

    /**
     * What the server sets goes to the store of the client, which the view keeps up to date.
     */
    @Test
    public void testViewOfTheCookieStoreDelegatesToTheSharedOne() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies("https://review.example.org", Collections.singletonMap("ProxyAccess", "granted")));
        BasicCookieStore shared = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));
        interceptor.process(request(), context);

        BasicClientCookie expired = new BasicClientCookie("old", "1"); // by the time cleared
        expired.setDomain("review.example.org");
        expired.setExpiryDate(new Date(System.currentTimeMillis() + 3_600_000));
        context.getCookieStore().addCookie(expired);
        Truth.assertThat(shared.getCookies()).hasSize(1);
        Truth.assertThat(context.getCookieStore().clearExpired(new Date(System.currentTimeMillis() + 7_200_000))).isTrue();
        Truth.assertThat(shared.getCookies()).isEmpty();

        context.getCookieStore().addCookie(new BasicClientCookie("again", "1"));
        context.getCookieStore().clear();
        Truth.assertThat(shared.getCookies()).isEmpty();
    }

    @Test
    public void testNothingToDoWithoutACookieStore() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(
            new MutableCookies("https://review.example.org", Collections.singletonMap("ProxyAccess", "granted")));
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(context.getCookieStore()).isNull();
    }

    @Test
    public void testCookiesWithoutADomainKeptAndOnesOfAnyCaseWithADotHidden() {
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(new MutableCookies("https://review.example.org"));
        BasicCookieStore shared = new BasicCookieStore();
        shared.addCookie(new BasicClientCookie("nodomain", "1"));
        BasicClientCookie dotted = new BasicClientCookie("sso", "state");
        dotted.setDomain(".Review.Example.org");
        shared.addCookie(dotted);
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(shared);
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("sso.example.org", -1, "https"));

        interceptor.process(request(), context);

        Truth.assertThat(names(context)).containsExactly("nodomain");
    }

    @Test
    public void testRedirectWithoutALocationReported() {
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        SignInPageException page = Assert.expectThrows(SignInPageException.class, () -> SignInPageException
            .throwIfSignInPage(new BasicHttpResponse(HttpVersion.HTTP_1_1, 302, "Found"), context,
                "https://review.example.org"));

        Truth.assertThat(page.getPageOrigin()).isEqualTo("https://review.example.org");
    }

    private static HttpResponse redirect(int status, String location) {
        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, status, "Redirect");
        response.addHeader("Location", location);
        return response;
    }

    private static BasicHttpRequest request() {
        BasicHttpRequest request = new BasicHttpRequest("GET", "/changes/");
        request.addHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER, "token");
        return request;
    }

    private static List<String> names(HttpClientContext context) {
        return context.getCookieStore().getCookies().stream().map(Cookie::getName).collect(Collectors.toList());
    }

    private GerritRestClient client(Map<String, String> cookies) {
        return (GerritRestClient) new GerritRestApiFactory()
            .create(new MutableCookies(proxyOrigin(), cookies)).restClient();
    }

    private String proxyOrigin() {
        return "http://127.0.0.1:" + proxy.getAddress().getPort();
    }

    private String signInOrigin() {
        return "http://localhost:" + signIn.getAddress().getPort();
    }

    private static final class MutableCookies extends GerritAuthData.Basic {
        volatile Map<String, String> cookies;

        MutableCookies(String host) {
            this(host, Collections.emptyMap());
        }

        MutableCookies(String host, Map<String, String> cookies) {
            this(host, "", "", cookies);
        }

        MutableCookies(String host, String login, String password, Map<String, String> cookies) {
            super(host, login, password);
            this.cookies = cookies;
        }

        @Override
        public Map<String, String> getCookies() {
            return cookies;
        }
    }

    private static String header(HttpExchange exchange, String name) {
        String value = exchange.getRequestHeaders().getFirst(name);
        return value != null ? value : "";
    }

    private static void answer(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
