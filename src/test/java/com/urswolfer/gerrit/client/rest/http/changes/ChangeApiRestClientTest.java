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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.AbandonInput;
import com.google.gerrit.extensions.api.changes.AddReviewerInput;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.FixInput;
import com.google.gerrit.extensions.api.changes.RestoreInput;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    public void testPublish() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/publish")
                .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").publish();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDelete() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectDelete("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940")
                .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").delete();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetTopic() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/topic", jsonElement)
                .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").topic();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetTopic() throws Exception {
        String topic = "my-topic";
        String json = "{\"topic\":\"" + topic + "\"}";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/topic", json, null)
                .expectGetGson()
                .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").topic(topic);

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

    @Test
    public void testRestoreChange() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/restore",
                "{}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").restore();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRestoreChangeWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/restore",
                "{\"message\":\"Reviving this change.\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        RestoreInput restoreInput = new RestoreInput();
        restoreInput.message = "Reviving this change.";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").restore(restoreInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSuggestReviewers() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=-1", jsonElement)
                .get();

        List<SuggestedReviewerInfo> expectedSuggestedReviewerInfos = Lists.newArrayList();

        SuggestedReviewerInfoParser suggestedReviewerInfoParser = EasyMock.createMock(SuggestedReviewerInfoParser.class);
        EasyMock.expect(suggestedReviewerInfoParser.parseSuggestReviewerInfos(jsonElement)).andReturn(expectedSuggestedReviewerInfos).once();
        EasyMock.replay(suggestedReviewerInfoParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, null, null, null,
                suggestedReviewerInfoParser,
                "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").get();

        Truth.assertThat(suggestedReviewerInfos).isSameAs(expectedSuggestedReviewerInfos);
        EasyMock.verify(gerritRestClient, suggestedReviewerInfoParser);
    }

    @Test
    public void testSuggestReviewersWithLimit() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=5", jsonElement)
                .get();

        List<SuggestedReviewerInfo> expectedSuggestedReviewerInfos = Lists.newArrayList();

        SuggestedReviewerInfoParser suggestedReviewerInfoParser = EasyMock.createMock(SuggestedReviewerInfoParser.class);
        EasyMock.expect(suggestedReviewerInfoParser.parseSuggestReviewerInfos(jsonElement)).andReturn(expectedSuggestedReviewerInfos).once();
        EasyMock.replay(suggestedReviewerInfoParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, null, null, null,
                suggestedReviewerInfoParser,
                "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").withLimit(5).get();

        Truth.assertThat(suggestedReviewerInfos).isSameAs(expectedSuggestedReviewerInfos);
        EasyMock.verify(gerritRestClient, suggestedReviewerInfoParser);
    }

    @Test
    public void testCheck() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/check", jsonElement)
            .get();

        ChangeInfo expectedChangeInfo = new ChangeInfo();
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        EasyMock.expect(changesParser.parseChangeInfos(jsonElement)).andReturn(Lists.newArrayList(expectedChangeInfo)).once();
        EasyMock.replay(changesParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changesParser, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ChangeInfo changeInfo = changeApiRestClient.check();
        Truth.assertThat(changeInfo).isSameAs(expectedChangeInfo);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCheckFix() throws Exception {
        FixInput fixInput = new FixInput();
        fixInput.deletePatchSetIfCommitMissing = true;
        fixInput.expectMergedAs = "mergedAs";

        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        String json = "{\"delete_patch_set_if_commit_missing\":" + fixInput.deletePatchSetIfCommitMissing
            + ",\"expect_merged_as\":\"" + fixInput.expectMergedAs + "\"}";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/check", json, jsonElement)
            .expectGetGson()
            .get();

        ChangeInfo expectedChangeInfo = new ChangeInfo();
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        EasyMock.expect(changesParser.parseChangeInfos(jsonElement)).andReturn(Lists.newArrayList(expectedChangeInfo)).once();
        EasyMock.replay(changesParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changesParser, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ChangeInfo changeInfo = changeApiRestClient.check(fixInput);
        Truth.assertThat(changeInfo).isSameAs(expectedChangeInfo);

        EasyMock.verify(gerritRestClient);
    }
    
    @Test
    public void testComments() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/comments", jsonElement)
                .get();

        TreeMap<String, List<CommentInfo>> expectedCommentInfos = Maps.newTreeMap();
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseCommentInfos(jsonElement)).andReturn(expectedCommentInfos).once();
        EasyMock.replay(commentsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, commentsParser,
                null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<CommentInfo>> commentInfos = changeApiRestClient.comments();

        Truth.assertThat(commentInfos).isSameAs(expectedCommentInfos);
        EasyMock.verify(gerritRestClient, commentsParser);
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
                EasyMock.createMock(DiffInfoParser.class),
                null);
    }
}
