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

import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.net.URI;

/**
 * Turns a {@link HttpVerb} and a URI into the http client's request object, and attaches the body
 * where the verb takes one.
 *
 * <p>The verbs that carry a body differ only in the content type they declare: the
 * {@code *_TEXT_PLAIN} variants exist for the handful of Gerrit endpoints that take a bare string
 * rather than JSON.
 */
final class HttpRequests {

    private HttpRequests() {}

    static HttpRequestBase create(HttpVerb verb, String uri, String requestBody) {
        switch (verb) {
            case GET:
                // a GET has nowhere to put a body; GerritRestRequest refuses one before it gets this
                // far, and a body passed straight to RestClient is ignored here, as it always was
                return new HttpGet(uri);
            case DELETE:
                // HttpDelete cannot carry an entity, and Gerrit's delete-vote endpoint takes one
                return requestBody == null
                    ? new HttpDelete(uri)
                    : withBody(new HttpDeleteWithBody(uri), requestBody, ContentType.APPLICATION_JSON);
            case POST:
                return withBody(new HttpPost(uri), requestBody, ContentType.APPLICATION_JSON);
            case POST_TEXT_PLAIN:
                return withBody(new HttpPost(uri), requestBody, ContentType.TEXT_PLAIN);
            case PUT:
                return withBody(new HttpPut(uri), requestBody, ContentType.APPLICATION_JSON);
            case PUT_TEXT_PLAIN:
                return withBody(new HttpPut(uri), requestBody, ContentType.TEXT_PLAIN);
            default:
                throw new IllegalStateException("Unknown or unsupported HttpVerb method: " + verb);
        }
    }

    private static HttpRequestBase withBody(HttpEntityEnclosingRequestBase method,
                                            String requestBody,
                                            ContentType contentType) {
        if (requestBody != null) {
            method.setEntity(new StringEntity(requestBody, contentType));
        }
        return method;
    }

    /**
     * {@link HttpDelete} extends the request base that has no entity, so a {@code DELETE} with a
     * body needs its own class.
     */
    private static final class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        private HttpDeleteWithBody(String uri) {
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }
}
