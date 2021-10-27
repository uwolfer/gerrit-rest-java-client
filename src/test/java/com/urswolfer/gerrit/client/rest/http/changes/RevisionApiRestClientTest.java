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
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.CherryPickInput;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.SubmitInput;
import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.TestSubmitRuleInput;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.*;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import static com.urswolfer.gerrit.client.rest.RestClient.HttpVerb.GET;

/**
 * @author Thomas Forrer
 */
public class RevisionApiRestClientTest extends AbstractParserTest {

    private static final String CHANGE_ID = "packages%2Ftest~master~Ieabd72e73f3da0df90fd6e8cba8f6c5dd7d120df";
    private static final String FILE_PATH = "src/main/README.md";
    private static final String FILE_PATH_ENCODED = "src%2Fmain%2FREADME.md";

    @DataProvider(name = "TestCases")
    public Iterator<RevisionApiTestCase[]> testCases() throws Exception {
        return Lists.newArrayList(
                withRevision("current")
                        .expectRevisionUrl("/changes/" + CHANGE_ID + "/revisions/current")
                        .expectReviewUrl("/changes/" + CHANGE_ID + "/revisions/current/review")
                        .expectSubmitUrl("/changes/" + CHANGE_ID + "/submit")
                        .expectPublishUrl("/changes/" + CHANGE_ID + "/revisions/current/publish")
                        .expectCherryPickUrl("/changes/" + CHANGE_ID + "/revisions/current/cherrypick")
                        .expectRebaseUrl("/changes/" + CHANGE_ID + "/revisions/current/rebase")
                        .expectGetFileUrl("/changes/" + CHANGE_ID + "/revisions/current/files")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/current/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectMergeableUrl("/changes/" + CHANGE_ID + "/revisions/current/mergeable")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/current/comments/")
                        .expectGetRobotCommentsUrl("/changes/" + CHANGE_ID + "/revisions/current/robotcomments/")
                        .expectGetDraftsUrl("/changes/" + CHANGE_ID + "/revisions/current/drafts/")
                        .expectSubmitTypeUrl("/changes/" + CHANGE_ID + "/revisions/current/submit_type")
                        .expectTestSubmitTypeUrl("/changes/" + CHANGE_ID + "/revisions/current/test.submit_type")
                        .expectDescriptionUrl("/changes/" + CHANGE_ID + "/revisions/current/description")
                        .expectGetCommitUrl("/changes/" + CHANGE_ID + "/revisions/current/commit")
                        .expectGetActionsUrl("/changes/" + CHANGE_ID + "/revisions/current/actions")
                        .get(),
                withRevision("3")
                        .expectRevisionUrl("/changes/" + CHANGE_ID + "/revisions/3")
                        .expectReviewUrl("/changes/" + CHANGE_ID + "/revisions/3/review")
                        .expectSubmitUrl("/changes/" + CHANGE_ID + "/submit")
                        .expectPublishUrl("/changes/" + CHANGE_ID + "/revisions/3/publish")
                        .expectCherryPickUrl("/changes/" + CHANGE_ID + "/revisions/3/cherrypick")
                        .expectRebaseUrl("/changes/" + CHANGE_ID + "/revisions/3/rebase")
                        .expectGetFileUrl("/changes/" + CHANGE_ID + "/revisions/3/files")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/3/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectMergeableUrl("/changes/" + CHANGE_ID + "/revisions/3/mergeable")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/3/comments/")
                        .expectGetRobotCommentsUrl("/changes/" + CHANGE_ID + "/revisions/3/robotcomments/")
                        .expectGetDraftsUrl("/changes/" + CHANGE_ID + "/revisions/3/drafts/")
                        .expectSubmitTypeUrl("/changes/" + CHANGE_ID + "/revisions/3/submit_type")
                        .expectTestSubmitTypeUrl("/changes/" + CHANGE_ID + "/revisions/3/test.submit_type")
                        .expectDescriptionUrl("/changes/" + CHANGE_ID + "/revisions/3/description")
                        .expectGetCommitUrl("/changes/" + CHANGE_ID + "/revisions/3/commit")
                        .expectGetActionsUrl("/changes/" + CHANGE_ID + "/revisions/3/actions")
                        .get()
        ).iterator();
    }

    @Test(dataProvider = "TestCases")
    public void testReview(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(
                        testCase.reviewUrl,
                        "{\"message\":\"Looks good!\",\"labels\":{\"Code-Review\":2},\"omit_duplicate_comments\":false,\"work_in_progress\":false,\"ready\":false,\"ignore_automatic_attention_set_rules\":false}"
                )
                .expectGetGson()
                .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ReviewInput reviewInput = new ReviewInput();
        reviewInput.label("Code-Review", 2).message("Looks good!");

        String revision = testCase.revision;
        if (revision.equals("current")) {
            changesRestClient.id(CHANGE_ID)
                    .current()
                    .review(reviewInput);
        } else {
            changesRestClient.id(CHANGE_ID)
                    .revision(revision)
                    .review(reviewInput);
        }

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testSubmit(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(testCase.submitUrl, "{\"wait_for_merge\":false,\"notify\":\"ALL\"}")
                .expectGetGson()
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).submit();

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testDelete(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete(testCase.revisionUrl)
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).delete();

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testPublish(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost(testCase.publishUrl)
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).publish();

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testCherryPick(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost(testCase.cherryPickUrl,
                "{\"message\":\"Implementing Feature X\",\"destination\":\"release-branch\",\"notify\":\"ALL\",\"keep_reviewers\":false,\"allow_conflicts\":false,\"allow_empty\":false}")
            .expectGetGson()
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        CherryPickInput cherryPickInput = new CherryPickInput();
        cherryPickInput.message = "Implementing Feature X";
        cherryPickInput.destination = "release-branch";
        changesRestClient.id(CHANGE_ID).revision(testCase.revision).cherryPick(cherryPickInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testRebase(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost(testCase.rebaseUrl, "{}")
            .expectGetGson()
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).rebase();

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testSubmitWithSubmitInput(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(testCase.submitUrl, "{\"wait_for_merge\":true,\"on_behalf_of\":\"jdoe\",\"notify\":\"ALL\"}")
                .expectGetGson()
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        SubmitInput submitInput = new SubmitInput();
        submitInput.onBehalfOf = "jdoe";
        submitInput.waitForMerge = true;

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).submit(submitInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testSetFileReviewed(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPut(testCase.fileReviewedUrl)
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).setReviewed(FILE_PATH, true);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testGetFiles(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet(testCase.fileUrl, jsonElement)
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).files();

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testGetCommit(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.getCommitUrl, jsonElement)
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).commit(false);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testDeleteFileReviewed(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectDelete(testCase.fileReviewedUrl)
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).setReviewed(FILE_PATH, false);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testMergeable(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.mergeableUrl, jsonElement)
            .get();

        MergeableInfoParser mergeableInfoParser = EasyMock.createMock(MergeableInfoParser.class);
        EasyMock.expect(mergeableInfoParser.parseMergeableInfo(jsonElement)).andReturn(null).once();
        EasyMock.replay(mergeableInfoParser);

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient, mergeableInfoParser);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).mergeable();

        EasyMock.verify(gerritRestClient, mergeableInfoParser);
    }

    @Test(dataProvider = "TestCases")
    public void testGetCommentsAndDrafts(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet(testCase.getCommentsUrl, jsonElement)
                .expectGet(testCase.getDraftsUrl, jsonElement)
                .get();

        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseCommentInfos(jsonElement)).andReturn(null).times(2);
        EasyMock.replay(commentsParser);

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient, commentsParser);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).comments();
        changesRestClient.id(CHANGE_ID).revision(testCase.revision).drafts();

        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test(dataProvider = "TestCases")
    public void testGetRobotComments(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.robotCommentsUrl, jsonElement)
            .get();

        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseRobotCommentInfos(jsonElement)).andReturn(null).once();
        EasyMock.replay(commentsParser);

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient, commentsParser);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).robotComments();

        EasyMock.verify(gerritRestClient, commentsParser);
    }

    @Test
    public void testPatch() throws Exception {
        String patchContent = "patch content";
        String requestUrl = "/changes/122/revisions/1/patch";
        String base64String = Base64.encodeBase64String(patchContent.getBytes("UTF-8"));
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(base64String.getBytes("UTF-8")));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "base64"));
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "application/mbox"));
        EasyMock.replay(httpEntity, httpResponse);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GET, httpResponse)
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = changesRestClient.id(122).revision(1).patch();
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));

            Truth.assertThat(actualContent).isEqualTo(patchContent);
            Truth.assertThat(binaryResult.isBase64()).isTrue();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("application/mbox");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    @Test(dataProvider = "TestCases")
    public void testActions(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.actionsUrl, jsonElement)
            .get();

        CommitInfosParser commitInfosParser = EasyMock.createMock(CommitInfosParser.class);
        EasyMock.expect(commitInfosParser.parseActionInfos(jsonElement)).andReturn(null).once();
        EasyMock.replay(commitInfosParser);

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient, commitInfosParser);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).actions();

        EasyMock.verify(gerritRestClient, commitInfosParser);
    }

    @Test(dataProvider = "TestCases")
    public void testSubmitType(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = getJsonElement("submittype.json");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.submitTypeUrl, jsonElement)
            .expectGetGson()
            .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        SubmitType expectSubmitType = changesRestClient.id(CHANGE_ID).revision(testCase.revision).submitType();

        Truth.assertThat(expectSubmitType).isEqualTo(SubmitType.MERGE_IF_NECESSARY);
        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testTestSubmitType(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = getJsonElement("testsubmittype.json");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost(testCase.testSubmitTypeUrl, "{\"rule\":\"submit_type(cherry_pick)\",\"filters\":\"SKIP\"}", jsonElement)
            .expectGetGson()
            .expectGetGson()
            .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        TestSubmitRuleInput testSubmitRuleInput = new TestSubmitRuleInput();
        testSubmitRuleInput.filters = TestSubmitRuleInput.Filters.SKIP;
        testSubmitRuleInput.rule = "submit_type(cherry_pick)";
        SubmitType expectSubmitType = changesRestClient.id(CHANGE_ID).revision(testCase.revision).testSubmitType(testSubmitRuleInput);

        Truth.assertThat(expectSubmitType).isEqualTo(SubmitType.CHERRY_PICK);
        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testDescription(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet(testCase.descriptionUrl, jsonElement)
            .get();
        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);
        changesRestClient.id(CHANGE_ID).revision(testCase.revision).description();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSubmitPreview() throws Exception {
        String previewContent = "binary data";
        String requestUrl = "/changes/" + CHANGE_ID + "/revisions/current/preview_submit?format=zip";
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(previewContent.getBytes()));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(null);
        EasyMock.expect(httpResponse.getFirstHeader("Content-Type")).andStubReturn(
            new BasicHeader("Content-Type", "application/x-zip"));
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(null);
        EasyMock.replay(httpEntity, httpResponse);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GET, httpResponse)
            .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = changesRestClient.id(CHANGE_ID).revision("current").submitPreview("zip");
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(byteArrayOutputStream.toString());

            Truth.assertThat(actualContent).isEqualTo(previewContent);
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("application/x-zip");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        FileInfoParser fileInfoParser = EasyMock.createMock(FileInfoParser.class);
        ReviewerInfosParser reviewerInfosParser = EasyMock.createMock(ReviewerInfosParser.class);
        ReviewResultParser reviewResultParser = EasyMock.createMock(ReviewResultParser.class);
        CommitInfosParser commitInfosParser = EasyMock.createMock(CommitInfosParser.class);
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        MergeableInfoParser mergeableInfoParser = EasyMock.createMock(MergeableInfoParser.class);
        ReviewInfoParser reviewInfoParser = EasyMock.createMock(ReviewInfoParser.class);
        return new ChangesRestClient(gerritRestClient, changeInfosParser, commentsParser,
            fileInfoParser, reviewerInfosParser, reviewResultParser,
            commitInfosParser, accountsParser, mergeableInfoParser, reviewInfoParser);
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient, CommentsParser commentsParser) {
        return new ChangesRestClient(
                gerritRestClient,
                EasyMock.createMock(ChangeInfosParser.class),
                commentsParser,
                EasyMock.createMock(FileInfoParser.class),
                EasyMock.createMock(ReviewerInfosParser.class),
                EasyMock.createMock(ReviewResultParser.class),
                EasyMock.createMock(CommitInfosParser.class),
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(MergeableInfoParser.class),
                EasyMock.createMock(ReviewInfoParser.class));
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient, MergeableInfoParser mergeableInfoParser) {
        return new ChangesRestClient(
            gerritRestClient,
            EasyMock.createMock(ChangeInfosParser.class),
            EasyMock.createMock(CommentsParser.class),
            EasyMock.createMock(FileInfoParser.class),
            EasyMock.createMock(ReviewerInfosParser.class),
            EasyMock.createMock(ReviewResultParser.class),
            EasyMock.createMock(CommitInfosParser.class),
            EasyMock.createMock(AccountsParser.class),
            mergeableInfoParser,
            EasyMock.createMock(ReviewInfoParser.class));
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient, CommitInfosParser commitInfosParser) {
        return new ChangesRestClient(
            gerritRestClient,
            EasyMock.createMock(ChangeInfosParser.class),
            EasyMock.createMock(CommentsParser.class),
            EasyMock.createMock(FileInfoParser.class),
            EasyMock.createMock(ReviewerInfosParser.class),
            EasyMock.createMock(ReviewResultParser.class),
            commitInfosParser,
            EasyMock.createMock(AccountsParser.class),
            EasyMock.createMock(MergeableInfoParser.class),
            EasyMock.createMock(ReviewInfoParser.class));
    }

    private static RevisionApiTestCase withRevision(String revision) {
        return new RevisionApiTestCase(revision);
    }

    private static final class RevisionApiTestCase {
        private final String revision;
        private String revisionUrl;
        private String reviewUrl;
        private String submitUrl;
        private String publishUrl;
        private String cherryPickUrl;
        private String rebaseUrl;
        private String fileUrl;
        private String fileReviewedUrl;
        private String mergeableUrl;
        private String getCommentsUrl;
        private String robotCommentsUrl;
        private String getDraftsUrl;
        private String submitTypeUrl;
        private String testSubmitTypeUrl;
        private String descriptionUrl;
        private String getCommitUrl;
        private String actionsUrl;

        private RevisionApiTestCase(String revision) {
            this.revision = revision;
        }

        private RevisionApiTestCase expectRevisionUrl(String revisionUrl) {
            this.revisionUrl = revisionUrl;
            return this;
        }

        private RevisionApiTestCase expectReviewUrl(String reviewUrl) {
            this.reviewUrl = reviewUrl;
            return this;
        }

        private RevisionApiTestCase expectSubmitUrl(String submitUrl) {
            this.submitUrl = submitUrl;
            return this;
        }

        private RevisionApiTestCase expectPublishUrl(String publishUrl) {
            this.publishUrl = publishUrl;
            return this;
        }

        private RevisionApiTestCase expectCherryPickUrl(String cherryPickUrl) {
            this.cherryPickUrl = cherryPickUrl;
            return this;
        }

        private RevisionApiTestCase expectRebaseUrl(String rebaseUrl) {
            this.rebaseUrl = rebaseUrl;
            return this;
        }

        private RevisionApiTestCase expectGetFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
            return this;
        }

        private RevisionApiTestCase expectFileReviewedUrl(String setReviewedUrl) {
            this.fileReviewedUrl = setReviewedUrl;
            return this;
        }

        private RevisionApiTestCase expectMergeableUrl(String mergeableUrl) {
            this.mergeableUrl = mergeableUrl;
            return this;
        }

        private RevisionApiTestCase expectGetCommentsUrl(String getCommentsUrl) {
            this.getCommentsUrl = getCommentsUrl;
            return this;
        }

        private RevisionApiTestCase expectGetRobotCommentsUrl(String robotCommentsUrl) {
            this.robotCommentsUrl = robotCommentsUrl;
            return this;
        }

        private RevisionApiTestCase expectGetDraftsUrl(String getDraftsUrl) {
            this.getDraftsUrl = getDraftsUrl;
            return this;
        }

        private RevisionApiTestCase expectSubmitTypeUrl(String submitTypeUrl) {
            this.submitTypeUrl = submitTypeUrl;
            return this;
        }

        private RevisionApiTestCase expectTestSubmitTypeUrl(String testSubmitTypeUrl) {
            this.testSubmitTypeUrl = testSubmitTypeUrl;
            return this;
        }

        private RevisionApiTestCase expectDescriptionUrl(String descriptionUrl) {
            this.descriptionUrl = descriptionUrl;
            return this;
        }

        private RevisionApiTestCase expectGetCommitUrl(String getCommitUrl) {
            this.getCommitUrl = getCommitUrl;
            return this;
        }

        private RevisionApiTestCase expectGetActionsUrl(String actionsUrl) {
            this.actionsUrl = actionsUrl;
            return this;
        }

        private RevisionApiTestCase[] get() {
            return new RevisionApiTestCase[]{this};
        }

        @Override
        public String toString() {
            return revision;
        }
    }
}
