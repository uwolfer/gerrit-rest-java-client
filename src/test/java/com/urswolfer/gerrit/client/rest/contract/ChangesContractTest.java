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

package com.urswolfer.gerrit.client.rest.contract;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gerrit.extensions.api.changes.ReviewerInfo;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.FileInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.urswolfer.gerrit.client.rest.GerritRestApi;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pins what {@code gerritApi.changes()} puts on the wire and what it makes of the response.
 *
 * <p>See {@link FakeGerritServer} for why these tests go through the public API instead of mocking
 * the client internals.
 */
public class ChangesContractTest {

    private static final String CHANGE_ID = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";
    private static final String CHANGE_PATH = "/changes/" + CHANGE_ID;
    private static final String OTHER_CHANGE_ID = "myProject~master~I0000000000000000000000000000000000000000";
    private static final String OTHER_CHANGE_PATH = "/changes/" + OTHER_CHANGE_ID;

    /** Everything Gerrit 2.10 understands, minus {@code CHECK}. */
    private static final String OPTIONS_FOR_2_10 =
        "o=LABELS&o=DETAILED_LABELS&o=CURRENT_REVISION&o=ALL_REVISIONS"
            + "&o=CURRENT_COMMIT&o=ALL_COMMITS&o=CURRENT_FILES&o=ALL_FILES&o=DETAILED_ACCOUNTS"
            + "&o=MESSAGES&o=CURRENT_ACTIONS&o=REVIEWED&o=DRAFT_COMMENTS&o=DOWNLOAD_COMMANDS"
            + "&o=WEB_LINKS";

    @Test
    public void query() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/changes/?q=status:open", "changes/changes.json");

        List<ChangeInfo> changes = server.api().changes().query("status:open").get();

        Truth.assertThat(changes).hasSize(3);
        Truth.assertThat(changes.get(0).project).isEqualTo("packages/test");
        Truth.assertThat(server.trace()).containsExactly("GET /changes/?q=status:open");
        server.verify();
    }

    /**
     * Note that the query is not URL-encoded here, while
     * {@code changes().id(..).suggestReviewers(..)} does encode it.
     */
    @Test
    public void queryWithAllParameters() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/changes/?q=owner:self&n=10&S=5&o=LABELS&o=MESSAGES", "changes/changes.json");

        server.api().changes().query()
            .withQuery("owner:self")
            .withLimit(10)
            .withStart(5)
            .withOption(ListChangesOption.LABELS)
            .withOption(ListChangesOption.MESSAGES)
            .get();

        server.verify();
    }

    @Test
    public void info() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH, "changes/parsers/change.json");

        ChangeInfo changeInfo = server.api().changes().id(CHANGE_ID).info();

        Truth.assertThat(changeInfo).isNotNull();
        Truth.assertThat(server.trace()).containsExactly("GET " + CHANGE_PATH);
        server.verify();
    }

    /**
     * {@code get()} asks the server for its version first, so it can limit the requested
     * {@link ListChangesOption}s to the ones that version understands - here everything up to
     * {@code WEB_LINKS}, which is what Gerrit 2.10 supports, minus {@code CHECK}.
     */
    @Test
    public void getFetchesServerVersionFirst() throws Exception {
        String options = OPTIONS_FOR_2_10;
        FakeGerritServer server = new FakeGerritServer()
            .stubJson("GET", "/config/server/version", "\"2.10.1\"")
            .stub("GET", CHANGE_PATH + '?' + options, "changes/parsers/change.json");

        server.api().changes().id(CHANGE_ID).get();

        Truth.assertThat(server.trace()).containsExactly(
            "GET /config/server/version",
            "GET " + CHANGE_PATH + '?' + options).inOrder();
        server.verify();
    }

    /**
     * The version is read once for the whole API instance. It used to be cached per
     * {@code ChangeApi}, so each change cost its own {@code /config/server/version} request.
     */
    @Test
    public void getReadsTheServerVersionOncePerApiInstance() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubJson("GET", "/config/server/version", "\"2.10.1\"")
            .stub("GET", CHANGE_PATH + '?' + OPTIONS_FOR_2_10, "changes/parsers/change.json")
            .stub("GET", OTHER_CHANGE_PATH + '?' + OPTIONS_FOR_2_10, "changes/parsers/change.json");
        GerritRestApi api = server.api();

        api.changes().id(CHANGE_ID).get();
        api.changes().id(OTHER_CHANGE_ID).get();

        Truth.assertThat(server.trace()).containsExactly(
            "GET /config/server/version",
            "GET " + CHANGE_PATH + '?' + OPTIONS_FOR_2_10,
            "GET " + OTHER_CHANGE_PATH + '?' + OPTIONS_FOR_2_10).inOrder();
        server.verify();
    }

    @Test
    public void idWithProjectIsUrlEncoded() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/changes/my%2Fproject~123", "changes/parsers/change.json");

        server.api().changes().id("my/project", 123).info();

        server.verify();
    }

    @Test
    public void abandon() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("POST", CHANGE_PATH + "/abandon");

        server.api().changes().id(CHANGE_ID).abandon();

        Truth.assertThat(server.request("POST", CHANGE_PATH + "/abandon").getBody()).isEqualTo("{}");
        server.verify();
    }

    @Test
    public void setTopic() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("PUT", CHANGE_PATH + "/topic");

        server.api().changes().id(CHANGE_ID).topic("my-topic");

        Truth.assertThat(server.request("PUT", CHANGE_PATH + "/topic").getBody())
            .isEqualTo("{\"topic\":\"my-topic\"}");
        server.verify();
    }

    @Test
    public void comments() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH + "/comments", "changes/parsers/comments.json");

        Map<String, List<CommentInfo>> comments = server.api().changes().id(CHANGE_ID).comments();

        Truth.assertThat(comments.keySet())
            .containsExactly("playingfield.iml", "src/ch/tf/playingfield/PlayingField.java");
        server.verify();
    }

    @Test
    public void reviewers() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH + "/reviewers", "changes/parsers/reviewers.json");

        List<ReviewerInfo> reviewers = server.api().changes().id(CHANGE_ID).reviewers();

        Truth.assertThat(reviewers).hasSize(2);
        server.verify();
    }

    /**
     * Several endpoints return a bare object instead of a single-element array; the client has to
     * normalize that into a list.
     */
    @Test
    public void reviewersWithSingleObjectResponse() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH + "/reviewers", "changes/parsers/reviewer.json");

        List<ReviewerInfo> reviewers = server.api().changes().id(CHANGE_ID).reviewers();

        Truth.assertThat(reviewers).hasSize(1);
        Truth.assertThat(reviewers.get(0).name).isEqualTo("John Doe");
        server.verify();
    }

    @Test
    public void reviewCurrentRevision() throws Exception {
        String path = CHANGE_PATH + "/revisions/current/review";
        FakeGerritServer server = new FakeGerritServer()
            .stub("POST", path, "changes/parsers/review.json");

        ReviewInput reviewInput = new ReviewInput();
        reviewInput.message = "looks good";
        ReviewResult reviewResult = server.api().changes().id(CHANGE_ID).current().review(reviewInput);

        Truth.assertThat(reviewResult).isNotNull();
        Truth.assertThat(server.request("POST", path).getBody()).isEqualTo(
            "{\"message\":\"looks good\",\"omit_duplicate_comments\":false,\"work_in_progress\":false,"
                + "\"ready\":false,\"ignore_automatic_attention_set_rules\":false}");
        server.verify();
    }

    @Test
    public void revisionFiles() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH + "/revisions/1/files", "changes/parsers/files.json");

        Map<String, FileInfo> files = server.api().changes().id(CHANGE_ID).revision("1").files();

        Truth.assertThat(files.keySet()).containsExactly(
            "/COMMIT_MSG",
            "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java");
        server.verify();
    }

    @Test
    public void fileContentIsReturnedAsBinaryResult() throws Exception {
        String path = CHANGE_PATH + "/revisions/1/files/a%2Fb.txt/content";
        String content = "file content";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-FYI-Content-Type", "text/plain");
        headers.put("X-FYI-Content-Encoding", "base64");
        FakeGerritServer server = new FakeGerritServer()
            .stubRaw("GET", path, "text/plain",
                new String(Base64.encodeBase64(content.getBytes("UTF-8")), "UTF-8"), headers);

        BinaryResult binaryResult =
            server.api().changes().id(CHANGE_ID).revision("1").file("a/b.txt").content();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            binaryResult.writeTo(out);

            Truth.assertThat(new String(Base64.decodeBase64(out.toString()), "UTF-8")).isEqualTo(content);
            Truth.assertThat(binaryResult.isBase64()).isTrue();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("text/plain");
        } finally {
            binaryResult.close();
        }
        server.verify();
    }

    /**
     * Without the {@code X-FYI-Content-Type} header Gerrit sets on file content, the client falls
     * back to the plain {@code Content-Type} of the response.
     */
    @Test
    public void fileContentFallsBackToTheResponseContentType() throws Exception {
        String path = CHANGE_PATH + "/revisions/1/files/a%2Fb.txt/content";
        FakeGerritServer server = new FakeGerritServer()
            .stubRaw("GET", path, "text/plain", "file content",
                Collections.<String, String>emptyMap());

        BinaryResult binaryResult =
            server.api().changes().id(CHANGE_ID).revision("1").file("a/b.txt").content();
        try {
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("text/plain");
            Truth.assertThat(binaryResult.isBase64()).isFalse();
            Truth.assertThat(binaryResult.asString()).isEqualTo("file content");
        } finally {
            binaryResult.close();
        }
        server.verify();
    }

    /**
     * Gerrit emits a trailing comma in the reviewed-files list, which Gson turns into a null
     * element; the client has to drop it. That lived in a parser of its own until the parsers were
     * folded into GerritJson, so it is now only reachable through the API.
     */
    @Test
    public void reviewedFilesDropTheTrailingNull() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", CHANGE_PATH + "/revisions/1/files?reviewed", "changes/parsers/files-reviewed.json");

        Set<String> reviewed = server.api().changes().id(CHANGE_ID).revision("1").reviewed();

        Truth.assertThat(reviewed).containsExactly(
            "/COMMIT_MSG",
            "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java");
        server.verify();
    }

    @Test
    public void create() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubJson("POST", "/changes/", "{\"id\":\"" + CHANGE_ID + "\",\"_number\":42}");

        ChangeInput changeInput = new ChangeInput();
        changeInput.project = "myProject";
        changeInput.branch = "master";
        changeInput.subject = "a subject";
        ChangeApi changeApi = server.api().changes().create(changeInput);

        Truth.assertThat(changeApi.id()).isEqualTo("42");
        Truth.assertThat(server.request("POST", "/changes/").getBody()).isEqualTo(
            "{\"project\":\"myProject\",\"branch\":\"master\",\"subject\":\"a subject\"}");
        server.verify();
    }

    @Test
    public void unstubbedRequestFailsTheTest() throws Exception {
        FakeGerritServer server = new FakeGerritServer();
        AssertionError thrown = null;
        try {
            server.api().changes().id(CHANGE_ID).info();
        } catch (AssertionError e) {
            thrown = e;
        }

        Truth.assertThat(thrown).isNotNull();
        Truth.assertThat(thrown).hasMessageThat().contains("GET " + CHANGE_PATH);
        Truth.assertThat(server.trace()).isEmpty();
    }
}
