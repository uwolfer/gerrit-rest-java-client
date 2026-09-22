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
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

/**
 * Which http request object each verb produces, and what happens to the body.
 */
public class HttpRequestsTest {

    private static final String URI = "http://gerrit.example.com/changes/1";

    @Test
    public void verbsMapToTheirHttpMethod() {
        assertThat(method(HttpVerb.GET, null)).isEqualTo("GET");
        assertThat(method(HttpVerb.DELETE, null)).isEqualTo("DELETE");
        assertThat(method(HttpVerb.POST, null)).isEqualTo("POST");
        assertThat(method(HttpVerb.POST_TEXT_PLAIN, null)).isEqualTo("POST");
        assertThat(method(HttpVerb.PUT, null)).isEqualTo("PUT");
        assertThat(method(HttpVerb.PUT_TEXT_PLAIN, null)).isEqualTo("PUT");
    }

    @Test
    public void theUriIsTakenAsGiven() {
        assertThat(HttpRequests.create(HttpVerb.GET, URI + "?q=is:open+owner:self", null).getURI().toString())
            .isEqualTo(URI + "?q=is:open+owner:self");
    }

    @Test
    public void jsonVerbsDeclareTheJsonContentType() throws Exception {
        assertThat(contentType(HttpVerb.POST, "{}")).contains("application/json");
        assertThat(contentType(HttpVerb.PUT, "{}")).contains("application/json");
    }

    @Test
    public void textVerbsDeclareThePlainTextContentType() throws Exception {
        assertThat(contentType(HttpVerb.POST_TEXT_PLAIN, "a message")).contains("text/plain");
        assertThat(contentType(HttpVerb.PUT_TEXT_PLAIN, "a message")).contains("text/plain");
    }

    @Test
    public void aVerbWithoutABodyEnclosesNothing() throws Exception {
        assertThat(body(HttpVerb.POST, null)).isNull();
        assertThat(body(HttpVerb.PUT, null)).isNull();
    }

    /**
     * {@code HttpDelete} has nowhere to put an entity, so a DELETE that carries one - Gerrit's
     * delete-vote endpoint, for instance - needs the other request base. Before this, the body was
     * accepted by the call site and then quietly dropped on the way out.
     */
    @Test
    public void deleteCarriesABodyWhenOneIsGiven() throws Exception {
        HttpRequestBase request = HttpRequests.create(HttpVerb.DELETE, URI, "{\"label\":\"Code-Review\"}");

        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request).isInstanceOf(HttpEntityEnclosingRequest.class);
        assertThat(body(request)).isEqualTo("{\"label\":\"Code-Review\"}");
        assertThat(request.getURI().toString()).isEqualTo(URI);
    }

    @Test
    public void deleteWithoutABodyEnclosesNothing() throws Exception {
        assertThat(body(HttpVerb.DELETE, null)).isNull();
    }

    /**
     * A GET has nowhere to put a body. GerritRestRequest refuses one where it is set; a body passed
     * straight to the public RestClient methods is ignored here, as it always has been, rather than
     * turning into an exception for callers outside this library.
     */
    @Test
    public void getIgnoresABodyPassedStraightToTheRestClient() {
        HttpRequestBase request = HttpRequests.create(HttpVerb.GET, URI, "{}");

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request).isNotInstanceOf(HttpEntityEnclosingRequest.class);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void anUnsupportedVerbIsRejected() {
        HttpRequests.create(HttpVerb.HEAD, URI, null);
    }

    private static String method(HttpVerb verb, String requestBody) {
        return HttpRequests.create(verb, URI, requestBody).getMethod();
    }

    private static String contentType(HttpVerb verb, String requestBody) {
        HttpEntity entity = entity(HttpRequests.create(verb, URI, requestBody));
        return entity.getContentType().getValue();
    }

    private static String body(HttpVerb verb, String requestBody) throws IOException {
        return body(HttpRequests.create(verb, URI, requestBody));
    }

    private static String body(HttpRequestBase request) throws IOException {
        HttpEntity entity = entity(request);
        return entity == null ? null : EntityUtils.toString(entity);
    }

    private static HttpEntity entity(HttpRequestBase request) {
        if (!(request instanceof HttpEntityEnclosingRequest)) {
            return null;
        }
        return ((HttpEntityEnclosingRequest) request).getEntity();
    }
}
