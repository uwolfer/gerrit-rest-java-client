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
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

/**
 * Follows redirects as {@link LaxRedirectStrategy} does, the one after the login, which is a POST,
 * included, but not a 307 or 308 to another host of anything but a GET or HEAD: those repeat the
 * request there, with its body, such as the login and password of the form login, or a DELETE,
 * at the sign-in page of an authentication proxy.
 */
class MethodKeepingRedirectStrategy extends LaxRedirectStrategy {

    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        if (!super.isRedirected(request, response, context)) {
            return false;
        }
        int status = response.getStatusLine().getStatusCode();
        String method = request.getRequestLine().getMethod();
        if (status != HttpStatus.SC_TEMPORARY_REDIRECT && status != 308
            || method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
            return true;
        }
        // not getLocationURI, which counts the location as visited, and getRedirect asks it again
        Header location = response.getFirstHeader(HttpHeaders.LOCATION);
        HttpHost target = HttpClientContext.adapt(context).getTargetHost();
        URI uri = location != null ? GerritHostOnlyInterceptor.toUri(location.getValue(), target) : null;
        if (uri == null) {
            return false;
        }
        String host = GerritHostOnlyInterceptor.hostName(uri);
        return host == null && uri.getRawAuthority() == null // relative
            || host != null && target != null && host.equalsIgnoreCase(target.getHostName());
    }
}
