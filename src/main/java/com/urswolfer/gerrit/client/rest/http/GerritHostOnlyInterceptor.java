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
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

/**
 * Keeps the XSRF token to the Gerrit host, on every request the HTTP client sends, a redirect
 * included: the HTTP client copies the headers of a request onto a redirect to another host, such
 * as the sign-in page of an authentication proxy.
 */
class GerritHostOnlyInterceptor implements HttpRequestInterceptor {
    static final String XSRF_TOKEN_HEADER = "X-Gerrit-Auth";

    private final GerritAuthData authData;

    GerritHostOnlyInterceptor(GerritAuthData authData) {
        this.authData = authData;
    }

    @Override
    public void process(HttpRequest request, HttpContext context) {
        if (hostName(authData.getHost()) == null) { // a host the HTTP client gets along with, but which this cannot tell
            return;
        }
        if (!isGerritHost(authData.getHost(), HttpClientContext.adapt(context).getTargetHost(), true)) {
            request.removeHeaders(XSRF_TOKEN_HEADER);
        }
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
}
