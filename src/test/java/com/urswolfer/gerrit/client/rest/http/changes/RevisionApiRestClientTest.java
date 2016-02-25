/*
 * Copyright 2013-2015 Urs Wolfer
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
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.SubmitInput;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
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

/**
 * @author Thomas Forrer
 */
public class RevisionApiRestClientTest {

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
                        .expectRebaseUrl("/changes/" + CHANGE_ID + "/revisions/current/rebase")
                        .expectGetFileUrl("/changes/" + CHANGE_ID + "/revisions/current/files")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/current/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/current/comments/")
                        .expectGetDraftsUrl("/changes/" + CHANGE_ID + "/revisions/current/drafts/")
                        .get(),
                withRevision("3")
                        .expectRevisionUrl("/changes/" + CHANGE_ID + "/revisions/3")
                        .expectReviewUrl("/changes/" + CHANGE_ID + "/revisions/3/review")
                        .expectSubmitUrl("/changes/" + CHANGE_ID + "/submit")
                        .expectPublishUrl("/changes/" + CHANGE_ID + "/revisions/3/publish")
                        .expectRebaseUrl("/changes/" + CHANGE_ID + "/revisions/3/rebase")
                        .expectGetFileUrl("/changes/" + CHANGE_ID + "/revisions/3/files")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/3/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/3/comments/")
                        .expectGetDraftsUrl("/changes/" + CHANGE_ID + "/revisions/3/drafts/")
                        .get()
        ).iterator();
    }

    @Test(dataProvider = "TestCases")
    public void testReview(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(
                        testCase.reviewUrl,
                        "{\"message\":\"Looks good!\",\"labels\":{\"Code-Review\":2},\"strict_labels\":true,\"drafts\":\"DELETE\",\"notify\":\"ALL\",\"omit_duplicate_comments\":false}"
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
                .expectPost(testCase.submitUrl, "{\"wait_for_merge\":false}")
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
                .expectPost(testCase.submitUrl, "{\"wait_for_merge\":true,\"on_behalf_of\":\"jdoe\"}")
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
    public void testDeleteFileReviewed(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectDelete(testCase.fileReviewedUrl)
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).setReviewed(FILE_PATH, false);

        EasyMock.verify(gerritRestClient);
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
            .expectRequest(requestUrl, null, GerritRestClient.HttpVerb.GET, httpResponse)
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

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        FileInfoParser fileInfoParser = EasyMock.createMock(FileInfoParser.class);
        DiffInfoParser diffInfoParser = EasyMock.createMock(DiffInfoParser.class);
        return new ChangesRestClient(gerritRestClient, changesParser, commentsParser, fileInfoParser, diffInfoParser, null);
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient, CommentsParser commentsParser) {
        return new ChangesRestClient(
                gerritRestClient,
                EasyMock.createMock(ChangesParser.class),
                commentsParser,
                EasyMock.createMock(FileInfoParser.class),
                EasyMock.createMock(DiffInfoParser.class),
                null);
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
        private String rebaseUrl;
        private String fileUrl;
        private String fileReviewedUrl;
        private String getCommentsUrl;
        private String getDraftsUrl;

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

        private RevisionApiTestCase expectGetCommentsUrl(String getCommentsUrl) {
            this.getCommentsUrl = getCommentsUrl;
            return this;
        }

        private RevisionApiTestCase expectGetDraftsUrl(String getDraftsUrl) {
            this.getDraftsUrl = getDraftsUrl;
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
