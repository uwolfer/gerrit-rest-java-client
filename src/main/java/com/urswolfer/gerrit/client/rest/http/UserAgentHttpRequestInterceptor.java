/*
 * Copyright 2013-2018 Urs Wolfer
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

import com.urswolfer.gerrit.client.rest.Version;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * @author Urs Wolfer
 */
class UserAgentHttpRequestInterceptor implements HttpRequestInterceptor {

    @Override
    public void process(final HttpRequest request, final HttpContext context) {
        Header existingUserAgent = request.getFirstHeader(HttpHeaders.USER_AGENT);
        String userAgent = String.format("gerrit-rest-java-client/%s using %s",
            Version.get(), existingUserAgent.getValue());
        request.setHeader(HttpHeaders.USER_AGENT, userAgent);
    }
}
