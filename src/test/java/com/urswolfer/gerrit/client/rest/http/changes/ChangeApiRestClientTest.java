/*
 * Copyright 2013-2014 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.AbandonInput;
import com.google.gerrit.extensions.api.changes.AddReviewerInput;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * @author Thomas Forrer
 */
public class ChangeApiRestClientTest {
    @Test
    public void testAddReviewer() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers",
                "{\"reviewer\":\"jdoe\",\"confirmed\":true}");
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ChangeApi changeApi = changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        AddReviewerInput input = new AddReviewerInput();
        input.reviewer = "jdoe";
        input.confirmed = true;

        changeApi.addReviewer(input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAddReviewerWithStringParam() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers",
                "{\"reviewer\":\"jdoe\"}");
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ChangeApi changeApi = changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        changeApi.addReviewer("jdoe");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAbandonChange() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/abandon",
                "{}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").abandon();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAbandonChangeWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/abandon",
                "{\"message\":\"Change not necessary.\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        AbandonInput abandonInput = new AbandonInput();
        abandonInput.message = "Change not necessary.";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").abandon(abandonInput);

        EasyMock.verify(gerritRestClient);
    }

    private GerritRestClient getGerritRestClient(String expectedRequest, String expectedJson) throws Exception {
        return new GerritRestClientBuilder()
                .expectPost(expectedRequest, expectedJson)
                .expectGetGson()
                .get();
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        return new ChangesRestClient(
                gerritRestClient,
                EasyMock.createMock(ChangesParser.class),
                EasyMock.createMock(CommentsParser.class),
                EasyMock.createMock(FileInfoParser.class),
                EasyMock.createMock(DiffInfoParser.class));
    }
}
