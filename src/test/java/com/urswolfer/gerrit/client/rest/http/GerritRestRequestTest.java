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

import com.google.gerrit.extensions.api.changes.DeleteVoteInput;
import com.google.gson.JsonObject;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.testng.Assert.fail;

/**
 * How {@link GerritRestRequest} reaches the rest client: with no body it uses the client's own verb
 * methods, so what arrives is what the call sites sent before the builder existed; with a body it
 * uses {@code requestJson}, which those methods delegate to anyway.
 */
public class GerritRestRequestTest extends AbstractJsonTest {

    @Test
    public void getWithoutABodyUsesTheGetMethod() throws Exception {
        GerritRestClient client = new GerritRestClientBuilder()
            .expectGet("/changes/1/reviewers", new JsonObject())
            .get();

        restContext(client).get("/changes/1/reviewers").send();

        EasyMock.verify(client);
    }

    @Test
    public void deleteWithoutABodyUsesTheDeleteMethod() throws Exception {
        GerritRestClient client = new GerritRestClientBuilder()
            .expectDelete("/changes/1/reviewers/jdoe")
            .get();

        restContext(client).delete("/changes/1/reviewers/jdoe").send();

        EasyMock.verify(client);
    }

    /**
     * A GET has nowhere to put a body, so setting one fails where it is set - before anything is
     * sent and before the login round trips - rather than being dropped on the way out.
     */
    @Test
    public void getRejectsABodyBeforeSendingAnything() throws Exception {
        GerritRestClient client = new GerritRestClientBuilder().get();
        GerritRestRequest request = restContext(client).get("/changes/");

        try {
            request.rawBody("{\"q\":\"is:open\"}");
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessageThat().contains("/changes/");
        }
        try {
            request.body(new JsonObject());
            fail("expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // same for a serialized body
        }

        EasyMock.verify(client);
    }

    /**
     * Gerrit's delete-vote endpoint is a DELETE that carries an input object, so this is not a
     * hypothetical shape.
     */
    @Test
    public void deleteSendsABodyWhenOneIsSet() throws Exception {
        DeleteVoteInput input = new DeleteVoteInput();
        input.label = "Code-Review";
        String expectedBody = getGerritJson().toJson(input);

        GerritRestClient client = new GerritRestClientBuilder()
            .expectJsonRequest("/changes/1/reviewers/jdoe/delete", expectedBody, HttpVerb.DELETE, new JsonObject())
            .get();

        restContext(client).delete("/changes/1/reviewers/jdoe/delete").body(input).send();

        EasyMock.verify(client);
        assertThat(expectedBody).contains("Code-Review");
    }

    @Test
    public void postPicksTheOverloadThatMatchesTheBody() throws Exception {
        GerritRestClient withoutBody = new GerritRestClientBuilder()
            .expectPost("/changes/1/abandon")
            .get();
        restContext(withoutBody).post("/changes/1/abandon").send();
        EasyMock.verify(withoutBody);

        GerritRestClient withBody = new GerritRestClientBuilder()
            .expectPost("/changes/1/abandon", "{\"message\":\"stale\"}")
            .get();
        restContext(withBody).post("/changes/1/abandon").rawBody("{\"message\":\"stale\"}").send();
        EasyMock.verify(withBody);
    }

    @Test
    public void putPicksTheOverloadThatMatchesTheBody() throws Exception {
        GerritRestClient withoutBody = new GerritRestClientBuilder()
            .expectPut("/changes/1/edit/a.txt")
            .get();
        restContext(withoutBody).put("/changes/1/edit/a.txt").send();
        EasyMock.verify(withoutBody);

        GerritRestClient withBody = new GerritRestClientBuilder()
            .expectPut("/changes/1/edit/a.txt", "content", new JsonObject())
            .get();
        restContext(withBody).put("/changes/1/edit/a.txt").rawBody("content").send();
        EasyMock.verify(withBody);
    }
}
