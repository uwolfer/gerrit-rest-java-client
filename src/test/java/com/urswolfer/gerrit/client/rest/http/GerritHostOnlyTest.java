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
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * What is meant for Gerrit alone stays with its host, also where a request is redirected to
 * another one, such as the sign-in page of an authentication proxy in front of Gerrit.
 */
public class GerritHostOnlyTest {

    private HttpServer gerrit;
    private HttpServer other;
    /** What the other host got: path and Authorization. */
    private final List<String> otherRequests = new CopyOnWriteArrayList<>();

    @BeforeMethod
    public void startServers() throws IOException {
        otherRequests.clear();
        other = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        other.createContext("/", exchange -> {
            otherRequests.add(exchange.getRequestURI().getPath() + " auth=" + header(exchange, "Authorization"));
            exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"other\"");
            answer(exchange, 401, "Who are you?");
        });
        other.start();
        gerrit = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        gerrit.createContext("/", exchange -> {
            // "localhost" rather than 127.0.0.1: another host
            exchange.getResponseHeaders().add("Location", "http://localhost:" + other.getAddress().getPort() + "/challenge");
            answer(exchange, 302, "");
        });
        gerrit.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopServers() {
        gerrit.stop(0);
        other.stop(0);
    }

    /**
     * Another host which asks for a login and password is not Gerrit's to give them to.
     */
    @Test
    public void testPasswordNotSentToAnotherHostWhichAsks() {
        GerritRestClient client = (GerritRestClient) new GerritRestApiFactory().create(
            new GerritAuthData.Basic("http://127.0.0.1:" + gerrit.getAddress().getPort(), "user", "secret"))
            .restClient();

        Assert.expectThrows(RestApiException.class, () -> client.getRequest("/changes/"));

        Truth.assertThat(otherRequests).isNotEmpty();
        for (String request : otherRequests) {
            Truth.assertThat(request).endsWith("auth=");
        }
    }

    /**
     * Such as a proxy in front of the network, which some set up with the same login as Gerrit.
     */
    @Test
    public void testLoginStillForAnHttpProxy() {
        GerritHostAuthenticationStrategy strategy = new GerritHostAuthenticationStrategy(
            new GerritAuthData.Basic("https://review.example.org", "user", "secret"));
        BasicHttpResponse challenge = new BasicHttpResponse(HttpVersion.HTTP_1_1, 401, "Unauthorized");
        challenge.addHeader("WWW-Authenticate", "Basic realm=\"x\"");

        Truth.assertThat(strategy.isAuthenticationRequested(new HttpHost("review.example.org", -1, "https"),
            challenge, HttpClientContext.create())).isTrue();
        Truth.assertThat(strategy.isAuthenticationRequested(new HttpHost("sso.example.org", -1, "https"),
            challenge, HttpClientContext.create())).isFalse();
        // the proxy authentication asks the same credentials provider, which still has the login for any host
    }

    @Test
    public void testXsrfTokenOnlyToTheGerritHost() {
        Truth.assertThat(sent("https://review.example.org", new HttpHost("REVIEW.example.org", 443, "https"))).isTrue();
        // the HTTP client's cookie matching would treat a name which ends in Gerrit's host as its own
        Truth.assertThat(sent("https://review.example.org", new HttpHost("notreview.example.org", 443, "https"))).isFalse();
        Truth.assertThat(sent("https://review.example.org", new HttpHost("sso.example.org", 443, "https"))).isFalse();
    }

    @Test
    public void testXsrfTokenNotOverPlainHttpWhereGerritHasHttps() {
        Truth.assertThat(sent("https://review.example.org", new HttpHost("review.example.org", -1, "http"))).isFalse();
    }

    /**
     * As after a redirect from http to https, or to another port of the host.
     */
    @Test
    public void testXsrfTokenToAnotherSchemeOrPortOfTheHost() {
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
        GerritHostOnlyInterceptor interceptor = new GerritHostOnlyInterceptor(new GerritAuthData.Basic(gerritUrl));
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, target);
        BasicHttpRequest request = new BasicHttpRequest("GET", "/changes/");
        request.addHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER, "token");

        interceptor.process(request, context);

        return request.getFirstHeader(GerritHostOnlyInterceptor.XSRF_TOKEN_HEADER) != null;
    }

    /**
     * A 307 or 308 sends the body on, such as the login and password of the form login, or repeats a
     * DELETE.
     */
    @Test
    public void testRequestNotRepeatedAtAnotherHost() throws Exception {
        MethodKeepingRedirectStrategy strategy = new MethodKeepingRedirectStrategy();
        HttpClientContext context = HttpClientContext.create();
        context.setAttribute(HttpClientContext.HTTP_TARGET_HOST, new HttpHost("review.example.org", -1, "https"));

        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(307, "https://sso.example.org/authorize"), context)).isFalse();
        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(308, "https://sso.example.org/authorize"), context)).isFalse();
        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(307, "/login/again"), context)).isTrue();
        // java.net.URI has no host for a name with an underscore, which the HTTP client gets along with
        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(307, "https://sso_login.example.org/authorize"), context)).isFalse();
        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(307, "//sso.example.org/authorize"), context)).isFalse();
        // a 302 turns the POST into a GET, without the body
        Truth.assertThat(strategy.isRedirected(new HttpPost("https://review.example.org/login/"),
            redirect(302, "https://sso.example.org/authorize"), context)).isTrue();
        Truth.assertThat(strategy.isRedirected(new HttpGet("https://review.example.org/changes/"),
            redirect(307, "https://sso.example.org/authorize"), context)).isTrue();
        Truth.assertThat(strategy.isRedirected(new HttpDelete("https://review.example.org/changes/1/reviewers/2"),
            redirect(307, "https://sso.example.org/authorize"), context)).isFalse();
    }

    private static HttpResponse redirect(int status, String location) {
        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, status, "Redirect");
        response.addHeader("Location", location);
        return response;
    }

    private static String header(HttpExchange exchange, String name) {
        String value = exchange.getRequestHeaders().getFirst(name);
        return value != null ? value : "";
    }

    private static void answer(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html");
        exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
