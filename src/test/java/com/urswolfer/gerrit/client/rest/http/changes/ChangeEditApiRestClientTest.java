package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.FileContentInput;
import com.google.gerrit.extensions.api.changes.PublishChangeEditInput;
import com.google.gerrit.extensions.common.EditInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

public class ChangeEditApiRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testGet() throws Exception {
        // this used to stub a parser to hand back a mock EditInfo; with the parsing no longer
        // injectable, the response itself carries what the assertion checks
        JsonElement editInfo = JsonParser.parseString("{\"ref\":\"refs/users/01/1/edit\"}");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/1/edit", editInfo)
            .get();

        Optional<EditInfo> returned = getEditApiClient(gerritRestClient, "1").get();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(returned.isPresent()).isTrue();
        Truth.assertThat(returned.get().ref).isEqualTo("refs/users/01/1/edit");
    }

    @Test
    public void testDelete() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectDelete("/changes/1/edit")
            .get();
        getEditApiClient(gerritRestClient, "1").delete();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRebase() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectPost("/changes/1/edit:rebase")
            .get();
        getEditApiClient(gerritRestClient, "1").rebase();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testPublish() throws Exception {
        PublishChangeEditInput input = EasyMock.createMock(PublishChangeEditInput.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectPost("/changes/1/edit:publish","{}")
            .get();
        getEditApiClient(gerritRestClient, "1").publish(input);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetFile() throws Exception {
        String content = "someContent to a file";
        String base64String = Base64.encodeBase64String(content.getBytes("UTF-8"));
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(base64String.getBytes("UTF-8")));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "base64"));
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "text/plain"));
        EasyMock.replay(httpEntity, httpResponse);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectRequest("/changes/1/edit/file1",
                null, RestClient.HttpVerb.GET,httpResponse)
            .get();
        Optional<BinaryResult> returned = getEditApiClient(gerritRestClient, "1").getFile("file1");
        EasyMock.verify(gerritRestClient);
        Truth.assertThat(returned.isPresent()).isTrue();
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            returned.get().writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));
            Truth.assertThat(actualContent).isEqualTo(content);
        }
    }

    @Test
    public void testRenameFile() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectPost("/changes/1/edit",
                "{\"old_path\":\"file\",\"new_path\":\"newfile1\"}")
            .get();
        getEditApiClient(gerritRestClient, "1").renameFile("file", "newfile1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRestoreFile() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectPost("/changes/1/edit",
                "{\"restore_path\":\"restoredFile\"}")
            .get();
        getEditApiClient(gerritRestClient, "1").restoreFile("restoredFile");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testModifyFile() throws Exception {
        HttpResponse response = EasyMock.createMock(HttpResponse.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectRequest("/changes/1/edit/dir%2Ffile1",
                "some text goes here", RestClient.HttpVerb.PUT_TEXT_PLAIN, response)
            .get();
        FileContentInput input = new FileContentInput();
        input.binaryContent = "some text goes here";
        getEditApiClient(gerritRestClient, "1").modifyFile("dir/file1",input);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteFile() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectDelete("/changes/1/edit/dir%2Ffile1")
            .get();
        getEditApiClient(gerritRestClient, "1").deleteFile("dir/file1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetCommitMessage() throws Exception {
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(gson.toJson("Some commit message"), JsonElement.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectGet("/changes/1/edit:message",
                element)
            .get();
        String message = getEditApiClient(gerritRestClient, "1").getCommitMessage();
        Truth.assertThat(message).isEqualTo("Some commit message");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testModifyCommitMessage() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectPut("/changes/1/edit:message",
                "{\"message\":\"New commit Message\"}",EMPTY_JSON_OBJECT)
            .get();
        getEditApiClient(gerritRestClient, "1")
            .modifyCommitMessage("New commit Message");
        EasyMock.verify(gerritRestClient);
    }

    private ChangeEditApiRestClient getEditApiClient(GerritRestClient gerritRestClient, String id) {
        return new ChangeEditApiRestClient(gerritRestClient, gerritJson, id);
    }
}
