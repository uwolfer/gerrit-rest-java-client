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

import static com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest.restContext;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Thomas Forrer
 */
public class ChangeApiRestClientTest {
    @Test
    public void testListReviewers() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("[{\"name\":\"John Doe\"}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ReviewerInfo> listReviewers = changeApiRestClient.listReviewers();

        Truth.assertThat(listReviewers).hasSize(1);
        Truth.assertThat(listReviewers.get(0).name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
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
    public void testAddReviewerWithReviewerInput() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/reviewers",
                "{\"reviewer\":\"jdoe\",\"confirmed\":true}");
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ChangeApi changeApi = changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ReviewerInput input = new ReviewerInput();
        input.reviewer = "jdoe";
        input.confirmed = true;

        changeApi.addReviewer(input);

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
        JsonElement jsonElement = new JsonObject();
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/topic",
                    new JsonPrimitive("my-topic"))
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
            "{\"destination_branch\":\"destination_branch\",\"keep_all_votes\":false}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").move("destination_branch");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testMoveWithMessage() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/move",
            "{\"message\":\"Move to desination_branch\",\"destination_branch\":\"destination_branch\",\"keep_all_votes\":false}"
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
        JsonElement revertingChangeJsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert",
                "{\"notify\":\"ALL\"}", revertingChangeJsonElement)
            .get();

        ChangesRestClient changesRestClient = new ChangesRestClient(restContext(gerritRestClient));

        String revertingChangeId = changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").revert().id();

        Truth.assertThat(revertingChangeId).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRevertChangeWithMessage() throws Exception {
        JsonElement revertingChangeJsonElement = new JsonObject();
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revert", "{\"message\":\"Change need revert.\",\"notify\":\"ALL\"}", revertingChangeJsonElement)
            .get();

        ChangesRestClient changesRestClient = new ChangesRestClient(restContext(gerritRestClient));
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
        JsonElement jsonElement = JsonParser.parseString("[{\"account\":{\"name\":\"John Doe\"}}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=-1", jsonElement)
                .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").get();

        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
        Truth.assertThat(suggestedReviewerInfos.get(0).account.name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSuggestReviewersWithLimit() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("[{\"account\":{\"name\":\"John Doe\"}}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/suggest_reviewers?q=J&n=5", jsonElement)
                .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<SuggestedReviewerInfo> suggestedReviewerInfos = changeApiRestClient.suggestReviewers("J").withLimit(5).get();

        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
        Truth.assertThat(suggestedReviewerInfos.get(0).account.name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCheck() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/check", jsonElement)
            .get();

        ChangeInfo expectedChangeInfo = new ChangeInfo();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ChangeInfo changeInfo = changeApiRestClient.check();

        Truth.assertThat(changeInfo.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIncludedIn() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"branches\":[\"master\"]}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/in", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        IncludedInInfo includedInInfo = changeApiRestClient.includedIn();

        Truth.assertThat(includedInInfo.branches).containsExactly("master");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCheckFix() throws Exception {
        FixInput fixInput = new FixInput();
        fixInput.deletePatchSetIfCommitMissing = true;
        fixInput.expectMergedAs = "mergedAs";

        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
        String json = "{\"delete_patch_set_if_commit_missing\":" + fixInput.deletePatchSetIfCommitMissing
            + ",\"expect_merged_as\":\"" + fixInput.expectMergedAs + "\"}";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/check", json, jsonElement)
            .get();

        ChangeInfo expectedChangeInfo = new ChangeInfo();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        ChangeInfo changeInfo = changeApiRestClient.check(fixInput);

        Truth.assertThat(changeInfo.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testComments() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"a.txt\":[{\"message\":\"a comment\"}]}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/comments", jsonElement)
                .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<CommentInfo>> commentInfos = changeApiRestClient.comments();

        Truth.assertThat(commentInfos.keySet()).containsExactly("a.txt");
        Truth.assertThat(commentInfos.get("a.txt").get(0).message).isEqualTo("a comment");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRobotComments() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"a.txt\":[{\"robot_id\":\"checkstyle\"}]}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/robotcomments", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<RobotCommentInfo>> robotCommentInfos = changeApiRestClient.robotComments();

        Truth.assertThat(robotCommentInfos.keySet()).containsExactly("a.txt");
        Truth.assertThat(robotCommentInfos.get("a.txt").get(0).robotId).isEqualTo("checkstyle");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDrafts() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"a.txt\":[{\"message\":\"a comment\"}]}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/drafts", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Map<String, List<CommentInfo>> draftInfos = changeApiRestClient.drafts();

        Truth.assertThat(draftInfos.keySet()).containsExactly("a.txt");
        Truth.assertThat(draftInfos.get("a.txt").get(0).message).isEqualTo("a comment");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testMessages() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("[{\"id\":\"m1\"}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/messages", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ChangeMessageInfo> messageInfos = changeApiRestClient.messages();

        Truth.assertThat(messageInfos).hasSize(1);
        Truth.assertThat(messageInfos.get(0).id).isEqualTo("m1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetHashtags() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("[\"one\",\"two\"]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/hashtags", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        Set<String> hashtags = changeApiRestClient.getHashtags();

        Truth.assertThat(hashtags).containsExactly("one", "two");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetEdit() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"ref\":\"refs/users/01/1/edit\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/edit", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        EditInfo editInfo = changeApiRestClient.getEdit();

        Truth.assertThat(editInfo.ref).isEqualTo("refs/users/01/1/edit");
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
    public void testSetAssignee() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"name\":\"John Doe\"}");

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/assignee",
                "{\"assignee\":\"foo\"}",jsonElement)
            .get();
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        AssigneeInput assigneeInput = new AssigneeInput();
        assigneeInput.assignee = "foo";
        AccountInfo assigneeInfo =
            changeApiRestClient.setAssignee(assigneeInput);
        Truth.assertThat(assigneeInfo.name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetAssignee() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"name\":\"John Doe\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/assignee", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        AccountInfo assigneeInfo = changeApiRestClient.getAssignee();

        Truth.assertThat(assigneeInfo.name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetPastAssignees() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("[{\"name\":\"John Doe\"}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/past_assignees", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<AccountInfo> pastAssigneesInfo = changeApiRestClient.getPastAssignees();

        Truth.assertThat(pastAssigneesInfo).hasSize(1);
        Truth.assertThat(pastAssigneesInfo.get(0).name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteAssignee() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"name\":\"John Doe\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/assignee",jsonElement)
            .get();
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        AssigneeInput assigneeInput = new AssigneeInput();
        assigneeInput.assignee = "foo";
        AccountInfo assigneeInfo =
            changeApiRestClient.deleteAssignee();
        Truth.assertThat(assigneeInfo.name).isEqualTo("John Doe");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChangeGet() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940?o=LABELS&o=DETAILED_LABELS", jsonElement)
            .get();
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        EnumSet<ListChangesOption> options = EnumSet.of(ListChangesOption.LABELS, ListChangesOption.DETAILED_LABELS);
        ChangeInfo result = changeApiRestClient.get(options);

        Truth.assertThat(result.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChangeGetOnGerrit214() throws Exception {
        String expectedChangeId = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
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
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, expectedChangeId);
        ChangeInfo result = changeApiRestClient.get();

        Truth.assertThat(result.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChangeGetShouldUseAllOptionsOnLatestGerrit() throws Exception {
        String expectedChangeId = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
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
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, expectedChangeId);
        ChangeInfo result = changeApiRestClient.get();

        Truth.assertThat(result.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChangeInfo() throws Exception {
        JsonElement jsonElement = JsonParser.parseString("{\"id\":\"foo\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940", jsonElement)
            .get();
        ChangeInfo expectedChangeInfo = EasyMock.createMock(ChangeInfo.class);

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        ChangeInfo result = changeApiRestClient.info();

        Truth.assertThat(result.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
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
        JsonElement jsonElement = JsonParser.parseString("[{\"id\":\"foo\"}]");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/submitted_together", jsonElement)
            .get();


        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        List<ChangeInfo> changeInfos = changeApiRestClient.submittedTogether();

        Truth.assertThat(changeInfos).hasSize(1);
        Truth.assertThat(changeInfos.get(0).id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIgnore() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/ignore")
            .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        changeApiRestClient.ignore(true);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUnignore() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/unignore")
            .get();

        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(restContext(gerritRestClient), null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");

        changeApiRestClient.ignore(false);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRebaseChange() throws Exception {
        GerritRestClient gerritRestClient = getGerritRestClient(
                "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/rebase",
                "{\"base\":\"1234\",\"allow_conflicts\":false,\"on_behalf_of_uploader\":false}"
        );
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        RebaseInput rebaseInput = new RebaseInput();
        rebaseInput.base = "1234";
        changesRestClient.id("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940").rebase(rebaseInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCreateMergePatchSet() throws Exception {
        JsonElement response = JsonParser.parseString("{\"id\":\"foo\"}");
        ChangeInfo expectedChangeInfo = new ChangeInfo();
        expectedChangeInfo.id = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
        String request = "/changes/" + expectedChangeInfo.id + "/merge";
        String json = "{\"subject\":\"Refresh feature merge\",\"inherit_parent\":false,"
                + "\"merge\":{\"source\":\"refs/heads/feature\","
                + "\"source_branch\":\"refs/heads/feature\",\"allow_conflicts\":false}}";

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(request, json, response)
                .get();

        ChangeApiRestClient changeApi = new ChangeApiRestClient(restContext(gerritRestClient), null, expectedChangeInfo.id);
        MergePatchSetInput input = new MergePatchSetInput();
        input.subject = "Refresh feature merge";
        input.inheritParent = false;
        input.merge = new MergeInput();
        input.merge.source = "refs/heads/feature";
        input.merge.sourceBranch = "refs/heads/feature";
        input.merge.allowConflicts = false;

        ChangeInfo actualChangeInfo = changeApi.createMergePatchSet(input);

        Truth.assertThat(actualChangeInfo.id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }
    private GerritRestClient getGerritRestClient(String expectedRequest, String expectedJson) throws Exception {
        return new GerritRestClientBuilder()
                .expectPost(expectedRequest, expectedJson)
                .get();
    }

    private GerritRestClient getGerritRestClient(String expectedRequest) throws Exception {
      return new GerritRestClientBuilder()
              .expectPost(expectedRequest)
              .get();
  }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        return new ChangesRestClient(restContext(gerritRestClient));
    }
}
