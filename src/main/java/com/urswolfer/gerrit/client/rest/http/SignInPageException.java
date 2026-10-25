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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * A request to Gerrit was answered with a web page, or a redirect to one, which is what an
 * authentication proxy in front of Gerrit does until someone signs in to it, such as with the
 * cookies of {@link com.urswolfer.gerrit.client.rest.GerritAuthData#getCookies()}: it redirects
 * to its sign-in page, or shows it right away.
 */
public class SignInPageException extends IOException {

    private final String pageOrigin;

    SignInPageException(String pageOrigin) {
        super("Gerrit's REST API answered with a web page from " + pageOrigin
            + ", such as the sign-in page of an authentication proxy in front of Gerrit.");
        this.pageOrigin = pageOrigin;
    }

    /**
     * @return scheme, host and port of the page, without its path, whose query can hold the state of
     *         a sign-in that has no place in a log or a message
     */
    public String getPageOrigin() {
        return pageOrigin;
    }

    /**
     * Gerrit answers its REST API with no web page, not even a request for a file or for the
     * commit-msg hook, which would take the page for its content: a page is the sign-in, wherever
     * it comes from. So is a redirect which the client did not follow, as it follows none of a PUT,
     * wherever it points; and an answer which asks for a login from another host, which a redirect
     * led to.
     * From Gerrit's own host, by whatever scheme or port, as Gerrit may redirect from http to https,
     * an error keeps its status, which says more, and a 401 asks for the password rather than for a
     * sign-in.
     *
     * @param context of the request, which knows the host the last of its redirects went to
     */
    static void throwIfSignInPage(HttpResponse response, HttpContext context, String gerritUrl)
            throws SignInPageException {
        HttpHost target = HttpClientContext.adapt(context).getTargetHost();
        if (target == null) { // where the answer came from is all this goes by
            return;
        }
        if (GerritHostOnlyInterceptor.hostName(gerritUrl) == null) {
            return;
        }
        boolean atGerrit = GerritHostOnlyInterceptor.isGerritHost(gerritUrl, target, false);
        int status = response.getStatusLine().getStatusCode();
        String origin = null;
        if (status >= 300 && status < 400) {
            Header location = response.getFirstHeader(HttpHeaders.LOCATION);
            URI uri = location != null ? GerritHostOnlyInterceptor.toUri(location.getValue(), target) : null;
            String host = uri != null ? GerritHostOnlyInterceptor.hostName(uri) : null;
            if (host != null) {
                int port = URIUtils.extractHost(uri).getPort();
                origin = uri.getScheme() + "://" + host + (port != -1 ? ":" + port : ""); // without user info
            } else { // relative, on the host the request went to, or none to tell
                origin = target.toURI();
            }
        } else if (isSuccessfulPage(response) || !atGerrit && (status == 401 || status == 403)) {
            origin = target.toURI();
        }
        if (origin != null) {
            EntityUtils.consumeQuietly(response.getEntity());
            throw new SignInPageException(origin);
        }
    }

    private static boolean isSuccessfulPage(HttpResponse response) {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        Header contentType = entity != null ? entity.getContentType() : null;
        return status >= 200 && status < 300
            && contentType != null && contentType.getValue().toLowerCase(Locale.ROOT).startsWith("text/html");
    }
}
