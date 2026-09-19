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

package com.urswolfer.gerrit.client.rest.http.accounts;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.accounts.DeleteDraftCommentsInput;
import com.google.gerrit.extensions.api.accounts.DisplayNameInput;
import com.google.gerrit.extensions.api.accounts.StatusInput;
import com.google.gerrit.extensions.api.changes.StarsInput;
import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gerrit.extensions.client.ProjectWatchInfo;
import com.google.gerrit.extensions.common.AccountDetailInfo;
import com.google.gerrit.extensions.common.AccountExternalIdInfo;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.urswolfer.gerrit.client.rest.RestClient.HttpVerb.GET;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        AccountInfo accountInfo = accountsRestClient.get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDetail() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/detail", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        AccountDetailInfo accountInfo = accountsRestClient.detail();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    void testGetActiveTrue() throws Exception {
        JsonElement mockedJson = new JsonPrimitive("ok");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isTrue();
    }

    @Test
    void testGetActiveFalse() throws Exception {
        JsonElement mockedJson = new JsonPrimitive("foo");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isFalse();
    }

    @Test
    void testGetActivenull() throws Exception {
        // a real response can never yield a null string, so this now covers the empty response
        JsonElement mockedJson = null;
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isFalse();
    }

    @Test
    public void testSetActive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/active")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.setActive(true);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetInActive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/active")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");


        accountsRestClient.setActive(false);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        GeneralPreferencesInfo mockPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);

        GeneralPreferencesInfo result = accountsRestClient.getPreferences();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setPreferences()
        throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences", "{}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        GeneralPreferencesInfo mockPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);

        GeneralPreferencesInfo result = accountsRestClient.setPreferences(EasyMock.createMock(GeneralPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences.diff", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        DiffPreferencesInfo mockDiffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);

        DiffPreferencesInfo result = accountsRestClient.getDiffPreferences();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences.diff", "{}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        DiffPreferencesInfo mockDiffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);

        DiffPreferencesInfo result = accountsRestClient.setDiffPreferences(EasyMock.createMock(DiffPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences.edit", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        EditPreferencesInfo mockEditPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);

        EditPreferencesInfo result = accountsRestClient.getEditPreferences();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences.edit", "{}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        EditPreferencesInfo mockEditPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);

        EditPreferencesInfo result = accountsRestClient.setEditPreferences(EasyMock.createMock(EditPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/watched.projects", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.getWatchedProjects();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/watched.projects", "[{\"project\":\"foo\"}]", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        ProjectWatchInfo watched = new ProjectWatchInfo();
        watched.project = "foo";
        accountsRestClient.setWatchedProjects(Collections.singletonList(watched));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/watched.projects:delete", "[{\"project\":\"foo\"}]", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        ProjectWatchInfo watched = new ProjectWatchInfo();
        watched.project = "foo";
        accountsRestClient.deleteWatchedProjects(Collections.singletonList(watched));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getEmails() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/emails", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.getEmails();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void createEmail() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/emails/john.doe@example.com",
                "{\"email\":\"john.doe@example.com\",\"preferred\":false,\"no_confirmation\":false}",
                EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.createEmail("john.doe@example.com");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void email() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/emails/john.doe@example.com",
                EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.email("john.doe@example.com").get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setStatusByString() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/status", "{\"status\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.setStatus("foo");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setStatus() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/status", "{\"status\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.setStatus(new StatusInput("foo"));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setDisplayNameByString() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/displayname", "{\"display_name\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.setDisplayName("foo");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setDisplayName() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/displayname", "{\"display_name\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.setDisplayName(new DisplayNameInput("foo"));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDownloadAvatar() throws Exception {
        String imageContent = "image content";
        String requestUrl = "/accounts/101/avatar?s=16";
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(imageContent.getBytes(StandardCharsets.UTF_8)));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(null);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(null);
        EasyMock.expect(httpResponse.getFirstHeader("Content-Type")).andStubReturn(
            new BasicHeader("Content-Type", "image/png"));
        EasyMock.replay(httpEntity, httpResponse);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GET, httpResponse)
            .get();

        AccountsRestClient accountsRestClient = getAccountsRestClient(gerritRestClient);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = accountsRestClient.id(101).downloadAvatar(16);
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = byteArrayOutputStream.toString();

            Truth.assertThat(actualContent).isEqualTo(imageContent);
            Truth.assertThat(binaryResult.isBase64()).isFalse();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("image/png");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    @Test
    public void testStarChange() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.starChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUnStarChange() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.unstarChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetStars() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/stars.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9",
                "{\"add\":[],\"remove\":[]}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        StarsInput input = new StarsInput(Collections.emptySet(),Collections.emptySet());
        accountsRestClient.setStars("Iccf90a8284f8371a211db9a2824d0617e95a79f9", input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetStars() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/stars.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9",
                new JsonParser().parse("[\"label1\",\"label2\"]"))
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        SortedSet<String> labelSet = new TreeSet<>();
        labelSet.add("label1");
        labelSet.add("label2");

        SortedSet<String> result = accountsRestClient.getStars("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);

        Truth.assertThat(result).isEqualTo(labelSet);
        Truth.assertThat(result).hasSize(2);
        Truth.assertThat(result).containsExactly("label1", "label2");
    }

    @Test
    public void testGetStarredChanges() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/starred.changes", new JsonParser().parse("[{\"id\":\"foo\"}]"))
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");
        List<ChangeInfo> results = accountsRestClient.getStarredChanges();

        Truth.assertThat(results).hasSize(1);
        Truth.assertThat(results.get(0).id).isEqualTo("foo");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAddSshKey() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectJsonRequest("/accounts/jdoe/sshkeys", "foo", RestClient.HttpVerb.POST_TEXT_PLAIN, EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        SshKeyInfo result = accountsRestClient.addSshKey("foo");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testListSshKeys() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/sshkeys", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        List<SshKeyInfo> result = accountsRestClient.listSshKeys();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteSshKey() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/sshkeys/1", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.deleteSshKey(1);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndex() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/index")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.index();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getExternalIds() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/external.ids",EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        List<AccountExternalIdInfo> result = accountsRestClient.getExternalIds();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void deleteExternalIds() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/external.ids:delete", "[\"mailto:john.doe@example.com\"]")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.deleteExternalIds(Collections.singletonList("mailto:john.doe@example.com"));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void deleteDraftComments() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/drafts.delete", "{\"query\":\"message:foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        DeleteDraftCommentsInput input = new DeleteDraftCommentsInput();
        input.query = "message:foo";
        accountsRestClient.deleteDraftComments(input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void generateHttpPassword() throws Exception {
        JsonElement mockedJson = new JsonPrimitive("foo");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/password.http", "{\"generate\":true}", mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        String result = accountsRestClient.generateHttpPassword();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo("foo");
    }

    @Test
    public void setHttpPassword() throws Exception {
        JsonElement mockedJson = new JsonPrimitive("foo");
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/password.http", "{\"http_password\":\"foo\",\"generate\":false}", mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        String result = accountsRestClient.setHttpPassword("foo");
        EasyMock.verify(gerritRestClient);

        Truth.assertThat(result).isEqualTo("foo");
    }

    @Test
    public void deleteHttpPassword() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/password.http")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, "jdoe");

        accountsRestClient.deleteHttpPassword();
        EasyMock.verify(gerritRestClient);
    }

    private AccountsRestClient getAccountsRestClient(GerritRestClient gerritRestClient) {
        return new AccountsRestClient(gerritRestClient, gerritJson);
    }

    private AccountApiRestClient getAccountApiRestClient(GerritRestClient gerritRestClient, String name) {
        return new AccountApiRestClient(gerritRestClient, gerritJson, name);
    }
}
