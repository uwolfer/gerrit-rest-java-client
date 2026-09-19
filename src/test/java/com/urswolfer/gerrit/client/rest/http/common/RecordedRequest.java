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

package com.urswolfer.gerrit.client.rest.http.common;

/**
 * A single HTTP request captured by {@link FakeGerritServer}, reduced to the parts a REST client
 * contract cares about: the verb, the path including the raw query string, and the request body.
 */
public final class RecordedRequest {
    private final String method;
    private final String path;
    private final String body;

    RecordedRequest(String method, String path, String body) {
        this.method = method;
        this.path = path;
        this.body = body;
    }

    /**
     * The HTTP verb as it went on the wire, for example {@code "GET"}.
     */
    public String getMethod() {
        return method;
    }

    /**
     * Path and raw (i.e. not re-encoded) query string relative to the Gerrit host, for example
     * {@code "/changes/?q=status:open"}.
     */
    public String getPath() {
        return path;
    }

    /**
     * The request body, or {@code null} when the request carried no entity.
     */
    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return method + " " + path;
    }
}
