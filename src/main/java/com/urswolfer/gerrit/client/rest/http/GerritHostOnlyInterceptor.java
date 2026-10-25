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

import com.urswolfer.gerrit.client.rest.GerritAuthData;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Keeps to the Gerrit host what is meant for it alone, on every request the HTTP client sends,
 * a redirect included: the cookies of {@link GerritAuthData#getCookies()}, and the XSRF token,
 * which the HTTP client would otherwise copy onto a redirect to another host, such as the sign-in
 * page of an authentication proxy.
 *
 * The cookies never go into the cookie store the requests of a client share, which the login
 * empties, for a host without Gerrit's own session before each request: each request gets a view
 * of it which adds them. Only on the way to the Gerrit host, see {@link #isGerritHost}: the HTTP
 * client's cookie matching would also send a cookie of {@code gerrit.example.com} to any host whose
 * name ends in it, such as {@code auth.gerrit.example.com}. On the way to another host, the view
 * leaves out the cookies of Gerrit's host, and Gerrit's session and XSRF cookies also where they
 * are set for a domain above it, as Gerrit's {@code auth.cookieDomain} does: the cookies another
 * host on that domain set for itself it still gets.
 */
class GerritHostOnlyInterceptor implements HttpRequestInterceptor {
    static final String XSRF_TOKEN_HEADER = "X-Gerrit-Auth";
    private static final Set<String> GERRIT_COOKIES = new HashSet<>(Arrays.asList("GerritAccount", "XSRF_TOKEN"));

    private final GerritAuthData authData;

    GerritHostOnlyInterceptor(GerritAuthData authData) {
        this.authData = authData;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        String gerritHost = hostName(authData.getHost());
        if (gerritHost == null) { // a host the HTTP client gets along with, but which this cannot tell
            return;
        }
        boolean toGerrit = isGerritHost(authData.getHost(), clientContext.getTargetHost(), true);
        if (!toGerrit) {
            request.removeHeaders(XSRF_TOKEN_HEADER);
        }

        CookieStore store = clientContext.getCookieStore();
        if (store instanceof WithAuthDataCookies) {
            store = ((WithAuthDataCookies) store).shared;
        }
        if (store == null) {
            return;
        }
        if (!toGerrit) {
            clientContext.setCookieStore(new WithAuthDataCookies(store, Collections.emptyList(), gerritHost));
            return;
        }
        Map<String, String> cookies = authData.getCookies();
        if (cookies.isEmpty()) {
            clientContext.setCookieStore(store);
            return;
        }
        List<Cookie> own = new ArrayList<>();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            BasicClientCookie cookie = new BasicClientCookie(entry.getKey(), entry.getValue());
            cookie.setDomain(gerritHost.toLowerCase(Locale.ROOT)); // the client's matching is case-sensitive
            cookie.setPath("/");
            own.add(cookie);
        }
        clientContext.setCookieStore(new WithAuthDataCookies(store, own, null));
    }

    /**
     * As the HTTP client reads it, which gets along with a name {@link URI#getHost()} does not, such
     * as one with an underscore.
     *
     * @return {@code null} for a url this cannot tell the host of
     */
    static String hostName(String url) {
        try {
            return hostName(URI.create(url));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * @return {@code null} for a relative uri, or one this cannot tell the host of
     */
    static String hostName(URI uri) {
        HttpHost host = URIUtils.extractHost(uri);
        return host != null ? host.getHostName() : null;
    }

    /**
     * @return the location, with the scheme of the request it answers where it leaves that out
     */
    static URI toUri(String location, HttpHost target) {
        try {
            URI uri = URI.create(location);
            return uri.getScheme() == null && uri.getRawAuthority() != null && target != null
                ? URI.create(target.getSchemeName() + ":" + location) : uri;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Gerrit's host is the host name of its url, by any port and scheme, as Gerrit may redirect from
     * http to https; what is meant for it alone, though, does not go there by plain http where its
     * url has https.
     *
     * @param secret whether what goes there is meant for Gerrit alone
     */
    static boolean isGerritHost(String gerritUrl, HttpHost target, boolean secret) {
        String gerritHost = hostName(gerritUrl);
        if (gerritHost == null || target == null || !gerritHost.equalsIgnoreCase(target.getHostName())) {
            return false;
        }
        return !secret || !gerritUrl.regionMatches(true, 0, "https:", 0, 6) || "https".equalsIgnoreCase(target.getSchemeName());
    }

    /**
     * The cookie store of the client, as a request to the Gerrit host sees it: with the cookies of
     * the auth data in place of any of the same name, whatever domain the server set that one for.
     * Or as a request to another host does, without the cookies of the Gerrit host. What the server
     * sets goes to the client's.
     */
    static final class WithAuthDataCookies implements CookieStore {
        final CookieStore shared;
        private final List<Cookie> own;
        private final String hiddenHost;

        WithAuthDataCookies(CookieStore shared, List<Cookie> own, String hiddenHost) {
            this.shared = shared;
            this.own = own;
            this.hiddenHost = hiddenHost;
        }

        @Override
        public void addCookie(Cookie cookie) {
            shared.addCookie(cookie);
        }

        @Override
        public List<Cookie> getCookies() {
            List<Cookie> cookies = new ArrayList<>(own);
            for (Cookie cookie : shared.getCookies()) {
                if (own.stream().noneMatch(mine -> mine.getName().equals(cookie.getName()))
                    && !(hiddenHost != null && isGerrits(cookie, hiddenHost))) {
                    cookies.add(cookie);
                }
            }
            return cookies;
        }

        private static boolean isGerrits(Cookie cookie, String host) {
            String domain = cookie.getDomain();
            if (domain == null) {
                return false;
            }
            domain = (domain.startsWith(".") ? domain.substring(1) : domain).toLowerCase(Locale.ROOT);
            String name = host.toLowerCase(Locale.ROOT);
            return name.equals(domain)
                || GERRIT_COOKIES.contains(cookie.getName()) && name.endsWith("." + domain);
        }

        @Override
        public boolean clearExpired(Date date) {
            return shared.clearExpired(date);
        }

        @Override
        public void clear() {
            shared.clear();
        }
    }
}
