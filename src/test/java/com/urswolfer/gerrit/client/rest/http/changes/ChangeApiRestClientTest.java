/*
 * Copyright 2013-2021 Urs Wolfer
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
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.*;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Thomas Forrer
 */
public class ChangeApiRestClientTest {
    @Test
    public void testListReviewers() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers", jsonElement)
            .get();

        List<ReviewerInfo> expectedListReviewers = Lists.newArrayList();

        ReviewerInfosParser reviewerInfoParser = EasyMock.createMock(ReviewerInfosParser.class);
        EasyMock.expect(reviewerInfoParser.parseReviewerInfos(jsonElement)).andReturn(expectedListReviewers).once();
        EasyMock.replay(reviewerInfoParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, null,
            null, null, reviewerInfoParser,
            null, null,null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ReviewerInfo> listReviewers = changeApiRestClient.listReviewers();

        Truth.assertThat(listReviewers).isSameAs(expectedListReviewers);
        EasyMock.verify(gerritRestClient, reviewerInfoParser);
    }

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
    public void testMove() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/move",
            "{\"destination_branch\":\"destination_branch\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").move("destination_branch");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testMoveWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/move",
            "{\"message\":\"Move to desination_branch\",\"destination_branch\":\"destination_branch\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        MoveInput moveInput = new MoveInput();
        moveInput.destinationBranch = "destination_branch";
        moveInput.message = "Move to desination_branch";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").move(moveInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRevertChange() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert",
            "{\"notify\":\"ALL\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").revert();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRevertChangeWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert",
            "{\"message\":\"Change need revert.\",\"notify\":\"ALL\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        RevertInput revertInput = new RevertInput();
        revertInput.message = "Change need revert.";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").revert(revertInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRevertSubmission() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert_submission",
            "{\"notify\":\"ALL\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").revertSubmission();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRevertSubmissionWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert_submission",
            "{\"message\":\"Submission need revert.\",\"notify\":\"ALL\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        RevertInput revertInput = new RevertInput();
        revertInput.message = "Submission need revert.";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").revertSubmission(revertInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSuggestReviewers() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=-1", jsonElement)
                .get();

        List<SuggestedReviewerInfo> expectedSuggestedReviewerInfos = Lists.newArrayList();

        ReviewerInfosParser reviewerInfosParser = EasyMock.createMock(ReviewerInfosParser.class);
        EasyMock.expect(reviewerInfosParser.parseSuggestReviewerInfos(jsonElement)).andReturn(expectedSuggestedReviewerInfos).once();
        EasyMock.replay(reviewerInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null,
            null, null, null, reviewerInfosParser,
            null, null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").get();

        Truth.assertThat(suggestedReviewerInfos).isSameAs(expectedSuggestedReviewerInfos);
        EasyMock.verify(gerritRestClient, reviewerInfosParser);
    }

    @Test
    public void testSuggestReviewersWithLimit() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=5", jsonElement)
                .get();

        List<SuggestedReviewerInfo> expectedSuggestedReviewerInfos = Lists.newArrayList();

        ReviewerInfosParser reviewerInfosParser = EasyMock.createMock(ReviewerInfosParser.class);
        EasyMock.expect(reviewerInfosParser.parseSuggestReviewerInfos(jsonElement)).andReturn(expectedSuggestedReviewerInfos).once();
        EasyMock.replay(reviewerInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null,
            null, null, null, reviewerInfosParser,
            null, null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").withLimit(5).get();

        Truth.assertThat(suggestedReviewerInfos).isSameAs(expectedSuggestedReviewerInfos);
        EasyMock.verify(gerritRestClient, reviewerInfosParser);
    }

    @Test
    public void testCheck() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/check", jsonElement)
            .get();

        ChangeInfo expectedChangeInfo = new ChangeInfo();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ChangeInfo changeInfo = changeApiRestClient.check();
        Truth.assertThat(changeInfo).isSameAs(expectedChangeInfo);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIncludedIn() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/in", jsonElement)
            .get();

        IncludedInInfo expectedIncludedInInfo = new IncludedInInfo(new ArrayList<>(), new ArrayList<>(), null);
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        EasyMock.expect(changeInfosParser.parseIncludedInInfos(jsonElement)).andReturn(expectedIncludedInInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null, null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        IncludedInInfo includedInInfo = changeApiRestClient.includedIn();
        Truth.assertThat(includedInInfo).isSameAs(expectedIncludedInInfo);

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
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null,null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

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
            null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<CommentInfo>> commentInfos = changeApiRestClient.comments();

        Truth.assertThat(commentInfos).isSameAs(expectedCommentInfos);
        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test
    public void testRobotComments() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/robotcomments", jsonElement)
            .get();

        TreeMap<String, List<RobotCommentInfo>> expectedRobotCommentInfos = Maps.newTreeMap();
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseRobotCommentInfos(jsonElement)).andReturn(expectedRobotCommentInfos).once();
        EasyMock.replay(commentsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, commentsParser,
            null, null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<RobotCommentInfo>> robotCommentInfos = changeApiRestClient.robotComments();

        Truth.assertThat(robotCommentInfos).isSameAs(expectedRobotCommentInfos);
        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test
    public void testDrafts() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/drafts", jsonElement)
            .get();

        TreeMap<String, List<CommentInfo>> expectedDraftInfos = Maps.newTreeMap();
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseCommentInfos(jsonElement)).andReturn(expectedDraftInfos).once();
        EasyMock.replay(commentsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, commentsParser,
            null, null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<CommentInfo>> draftInfos = changeApiRestClient.drafts();

        Truth.assertThat(draftInfos).isSameAs(expectedDraftInfos);
        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test
    public void testMessages() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/messages", jsonElement)
            .get();

        List<ChangeMessageInfo> expectedMessageInfos = new ArrayList<>();
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseChangeMessageInfos(jsonElement)).andReturn(expectedMessageInfos).once();
        EasyMock.replay(commentsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, commentsParser,
            null, null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ChangeMessageInfo> messageInfos = changeApiRestClient.messages();

        Truth.assertThat(messageInfos).isSameAs(expectedMessageInfos);
        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test
    public void testGetHashtags() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/hashtags", jsonElement)
            .get();

        Set<String> expectedHashtags = new LinkedHashSet<>();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        EasyMock.expect(changeInfosParser.parseHashtags(jsonElement)).andReturn(expectedHashtags).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null, null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Set<String> hashtags = changeApiRestClient.getHashtags();

        Truth.assertThat(hashtags).isSameAs(expectedHashtags);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }

    @Test
    public void testGetEdit() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/edit", jsonElement)
            .get();

        EditInfo expectedEditInfo = new EditInfo();
        CommitInfosParser commitInfosParser = EasyMock.createMock(CommitInfosParser.class);
        EasyMock.expect(commitInfosParser.parseEditInfos(jsonElement)).andReturn(Lists.newArrayList(expectedEditInfo)).once();
        EasyMock.replay(commitInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null,
            null, null, null, null, commitInfosParser,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        EditInfo editInfo = changeApiRestClient.getEdit();
        Truth.assertThat(editInfo).isSameAs(expectedEditInfo);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/message",
            "{\"message\":\"New Commit message \\n\\nChange-Id: I10394472cbd17dd12454f229e4f6de00b143a444\\n\"}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        CommitMessageInput commitMessageInput = new CommitMessageInput();
        commitMessageInput.message = "New Commit message \n\nChange-Id: I10394472cbd17dd12454f229e4f6de00b143a444\n";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").setMessage(commitMessageInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetHashtags() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/hashtags",
            "{\"add\":[\"hashtag3\"],\"remove\":[\"hashtag2\"]}");
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        HashtagsInput hashtagsInput = new HashtagsInput(new HashSet<>(Arrays.asList("hashtag3")), new HashSet<>(Arrays.asList("hashtag2")));
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").setHashtags(hashtagsInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetAssignee() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/assignee", jsonElement)
            .get();

        AccountInfo expectedAssignee = new AccountInfo(null);
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseAccountInfo(jsonElement)).andReturn(expectedAssignee).once();
        EasyMock.replay(accountsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null,
            null, null, null, null, null,
            accountsParser, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        AccountInfo assigneeInfo = changeApiRestClient.getAssignee();

        Truth.assertThat(assigneeInfo).isSameAs(expectedAssignee);
        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testGetPastAssignees() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/past_assignees", jsonElement)
            .get();

        List<AccountInfo> expectedPastAssignees = new ArrayList<>();
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseAccountInfos(jsonElement)).andReturn(expectedPastAssignees).once();
        EasyMock.replay(accountsParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null,
            null, null, null, null, null,
            accountsParser, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<AccountInfo> pastAssigneesInfo = changeApiRestClient.getPastAssignees();

        Truth.assertThat(pastAssigneesInfo).isSameAs(expectedPastAssignees);
        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testChangeGet() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940?o=LABELS&o=DETAILED_LABELS", jsonElement)
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null,  null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        EnumSet<ListChangesOption> options = EnumSet.of(ListChangesOption.LABELS, ListChangesOption.DETAILED_LABELS);
        ChangeInfo result = changeApiRestClient.get(options);

        Truth.assertThat(result).isSameAs(expectedChangeInfo);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }

    @Test
    public void testChangeGetDetail() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/detail", jsonElement)
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null,  null, null, null,
            null, null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        ChangeInfo result = changeApiRestClient.getDetail();

        Truth.assertThat(result).isSameAs(expectedChangeInfo);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }


    @Test
    public void testChangeGetOnGerrit214() throws Exception {
        String expectedChangeId = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        String listOptions =
            String.join("&o=", Arrays.asList("LABELS", "DETAILED_LABELS",
                "CURRENT_REVISION",
                "ALL_REVISIONS",
                "CURRENT_COMMIT",
                "ALL_COMMITS",
                "CURRENT_FILES",
                "ALL_FILES",
                "DETAILED_ACCOUNTS",
                "MESSAGES",
                "CURRENT_ACTIONS",
                "REVIEWED",
                "DRAFT_COMMENTS",
                "DOWNLOAD_COMMANDS",
                "WEB_LINKS",
                "CHANGE_ACTIONS",
                "COMMIT_FOOTERS",
                "PUSH_CERTIFICATES",
                "REVIEWER_UPDATES",
                "SUBMITTABLE"));
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/" + expectedChangeId + "?o=" + listOptions, jsonElement)
            .expectGet("/config/server/version", new JsonPrimitive("2.14.20-102-g0b53142"))
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null,  null, null, null,
            null, null, null, expectedChangeId);
        ChangeInfo result = changeApiRestClient.get();

        Truth.assertThat(result).isSameAs(expectedChangeInfo);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }

    @Test
    public void testChangeGetShouldUseAllOptionsOnLatestGerrit() throws Exception {
        String expectedChangeId = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        List<String> allOptions = new ArrayList<>();
        for(Iterator<ListChangesOption> optionIterator = EnumSet.allOf(ListChangesOption.class).iterator(); optionIterator.hasNext(); ) {
            ListChangesOption option = optionIterator.next();
            if(option != ListChangesOption.CHECK) {
                allOptions.add(option.toString());
            }
        }
        String listOptions =
            String.join("&o=", allOptions);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/" + expectedChangeId + "?o=" + listOptions, jsonElement)
            .expectGet("/config/server/version", new JsonPrimitive("99.99"))
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser,
            null, null,  null, null, null,
            null, null, null, expectedChangeId);
        ChangeInfo result = changeApiRestClient.get();

        Truth.assertThat(result).isSameAs(expectedChangeInfo);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }

    @Test
    public void testChangeInfo() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940", jsonElement)
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseSingleChangeInfo(jsonElement)).andReturn(expectedChangeInfo).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser, null,
            null, null,  null, null, null,
            null, null,"myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        ChangeInfo result = changeApiRestClient.info();

        Truth.assertThat(result).isSameAs(expectedChangeInfo);
        EasyMock.verify(gerritRestClient, changeInfosParser);
    }

    @Test
    public void testIndexChange() throws Exception {
      GerritRestClient gerritRestClient = getGerritRestClient(
          "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/index");
      ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

      ChangeApi changeApi = changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

      changeApi.index();

      EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSubmittedTogether() throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/submitted_together", jsonElement)
            .get();

        List<ChangeInfo> expectedChangeInfos = Lists.newArrayList();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        EasyMock.expect(changeInfosParser.parseChangeInfos(jsonElement)).andReturn(expectedChangeInfos).once();
        EasyMock.replay(changeInfosParser);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, changeInfosParser, null,
            null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ChangeInfo> changeInfos = changeApiRestClient.submittedTogether();
        Truth.assertThat(changeInfos).isSameAs(expectedChangeInfos);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIgnore() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/ignore")
            .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, null,
            null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        changeApiRestClient.ignore(true);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUnignore() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/unignore")
            .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(gerritRestClient, null, null, null,
            null, null, null, null, null,
            null, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        changeApiRestClient.ignore(false);

        EasyMock.verify(gerritRestClient);
    }

    private GerritRestClient getGerritRestClient(String expectedRequest, String expectedJson) throws Exception {
        return new GerritRestClientBuilder()
                .expectPost(expectedRequest, expectedJson)
                .expectGetGson()
                .get();
    }

    private GerritRestClient getGerritRestClient(String expectedRequest) throws Exception {
      return new GerritRestClientBuilder()
              .expectPost(expectedRequest)
              .get();
  }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        return new ChangesRestClient(
                gerritRestClient,
                EasyMock.createMock(ChangeInfosParser.class),
                EasyMock.createMock(CommentsParser.class),
                EasyMock.createMock(FileInfoParser.class),
                EasyMock.createMock(ReviewerInfosParser.class),
                EasyMock.createMock(ReviewResultParser.class),
                EasyMock.createMock(CommitInfosParser.class),
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(MergeableInfoParser.class),
                EasyMock.createMock(ReviewInfoParser.class));
    }
}
