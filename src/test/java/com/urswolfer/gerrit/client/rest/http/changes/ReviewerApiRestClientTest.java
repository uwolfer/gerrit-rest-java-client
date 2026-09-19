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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.truth.Truth;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Map;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;

/**
 * @author Chenglong Sun
 */
public class ReviewerApiRestClientTest extends AbstractJsonTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    private static final Integer ACCOUNT_ID = 1000096;
    private static final String LABEL = "Work-In-Progress";

    @Test
    public void testVotes() throws Exception {
        JsonElement jsonElement = getJsonElement("votes.json");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers/" + ACCOUNT_ID + "/votes", jsonElement)
            .get();
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, gerritJson, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        ReviewerApiRestClient reviewerApiRestClient = new ReviewerApiRestClient(gerritRestClient, gerritJson, changeApiRestClient, ACCOUNT_ID);
        Map<String, Short> votes = reviewerApiRestClient.votes();

        Truth.assertThat(votes.get("Work-In-Progress")).isSameInstanceAs((short) 2);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteVote() throws Exception {
        JsonElement jsonElement = new JsonObject();
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers/" + ACCOUNT_ID + "/votes/" + LABEL)
            .get();
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, gerritJson, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        ReviewerApiRestClient reviewerApiRestClient = new ReviewerApiRestClient(gerritRestClient, gerritJson, changeApiRestClient, ACCOUNT_ID);
        reviewerApiRestClient.deleteVote(LABEL);

        EasyMock.verify(gerritRestClient);
    }
}
