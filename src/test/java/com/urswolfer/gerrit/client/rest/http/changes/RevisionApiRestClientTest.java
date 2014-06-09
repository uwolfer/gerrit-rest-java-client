package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.SubmitInput;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
                        .expectReviewUrl("/changes/" + CHANGE_ID + "/revisions/current/review")
                        .expectSubmitUrl("/changes/" + CHANGE_ID + "/submit")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/current/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/current/comments/")
                        .get(),
                withRevision("3")
                        .expectReviewUrl("/changes/" + CHANGE_ID + "/revisions/3/review")
                        .expectSubmitUrl("/changes/" + CHANGE_ID + "/submit")
                        .expectFileReviewedUrl("/changes/" + CHANGE_ID + "/revisions/3/files/" + FILE_PATH_ENCODED + "/reviewed")
                        .expectGetCommentsUrl("/changes/" + CHANGE_ID + "/revisions/3/comments/")
                        .get()
        ).iterator();
    }

    @Test(dataProvider = "TestCases")
    public void testReview(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPost(
                        testCase.reviewUrl,
                        "{\"message\":\"Looks good!\",\"labels\":{\"Code-Review\":2},\"strict_labels\":true,\"drafts\":\"DELETE\",\"notify\":\"ALL\"}"
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

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).setReviewed(FILE_PATH);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testDeleteFileReviewed(RevisionApiTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectDelete(testCase.fileReviewedUrl)
                .get();

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).deleteReviewed(FILE_PATH);

        EasyMock.verify(gerritRestClient);
    }

    @Test(dataProvider = "TestCases")
    public void testGetComments(RevisionApiTestCase testCase) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet(testCase.getCommentsUrl, jsonElement)
                .get();

        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseCommentInfos(jsonElement)).andReturn(null).once();
        EasyMock.replay(commentsParser);

        ChangesRestClient changesRestClient = getChangesRestClient(gerritRestClient, commentsParser);

        changesRestClient.id(CHANGE_ID).revision(testCase.revision).getComments();

        EasyMock.verify(gerritRestClient, commentsParser);
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient) {
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        return new ChangesRestClient(gerritRestClient, changesParser, commentsParser);
    }

    private ChangesRestClient getChangesRestClient(GerritRestClient gerritRestClient, CommentsParser commentsParser) {
        return new ChangesRestClient(
                gerritRestClient,
                EasyMock.createMock(ChangesParser.class),
                commentsParser
        );
    }

    private static RevisionApiTestCase withRevision(String revision) {
        return new RevisionApiTestCase(revision);
    }

    private static final class RevisionApiTestCase {
        private final String revision;
        private String reviewUrl;
        private String submitUrl;
        private String fileReviewedUrl;
        private String getCommentsUrl;

        private RevisionApiTestCase(String revision) {
            this.revision = revision;
        }

        private RevisionApiTestCase expectReviewUrl(String reviewUrl) {
            this.reviewUrl = reviewUrl;
            return this;
        }

        private RevisionApiTestCase expectSubmitUrl(String submitUrl) {
            this.submitUrl = submitUrl;
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

        private RevisionApiTestCase[] get() {
            return new RevisionApiTestCase[]{this};
        }

        @Override
        public String toString() {
            return revision;
        }
    }
}
