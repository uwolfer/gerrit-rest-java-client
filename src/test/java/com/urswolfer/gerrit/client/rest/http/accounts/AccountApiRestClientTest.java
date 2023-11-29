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
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.ChangeInfosParser;
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

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClientTest {

    private static final AccountInfo MOCK_ACCOUNT_INFO = EasyMock.createMock(AccountInfo.class);
    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);

    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe", MOCK_JSON_ELEMENT)
            .get();
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);

        EasyMock.expect(accountsParser.parseAccountInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_ACCOUNT_INFO)
            .once();
        EasyMock.replay(accountsParser);
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        AccountInfo accountInfo = accountsRestClient.get();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(accountInfo).isEqualTo(MOCK_ACCOUNT_INFO);
    }

    @Test
    public void testDetail() throws Exception {
        AccountDetailInfo mockAccountDetailInfo = EasyMock.createMock(AccountDetailInfo.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/detail", MOCK_JSON_ELEMENT)
            .get();
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);

        EasyMock.expect(accountsParser.parseAccountDetailInfo(MOCK_JSON_ELEMENT))
            .andReturn(mockAccountDetailInfo)
            .once();
        EasyMock.replay(accountsParser);
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        AccountDetailInfo accountInfo = accountsRestClient.detail();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(accountInfo).isEqualTo(mockAccountDetailInfo);
    }

    @Test
    void testGetActiveTrue() throws Exception {
        JsonElement mockedJson = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(mockedJson.getAsString()).andReturn("ok").anyTimes();
        EasyMock.replay(mockedJson);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isTrue();
    }

    @Test
    void testGetActiveFalse() throws Exception {
        JsonElement mockedJson = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(mockedJson.getAsString()).andReturn("foo").anyTimes();
        EasyMock.replay(mockedJson);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isFalse();
    }

    @Test
    void testGetActivenull() throws Exception {
        JsonElement mockedJson = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(mockedJson.getAsString()).andReturn(null).anyTimes();
        EasyMock.replay(mockedJson);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/active",mockedJson)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        boolean active = accountsRestClient.getActive();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(active).isFalse();
    }

    @Test
    public void testSetActive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/active")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.setActive(true);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetInActive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/active")
            .get();

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.setActive(false);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences", MOCK_JSON_ELEMENT)
            .get();
        GeneralPreferencesInfo mockPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);

        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseGeneralPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        GeneralPreferencesInfo result = accountsRestClient.getPreferences();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockPreferencesInfo);
    }

    @Test
    public void setPreferences()
        throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences", "{}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();

        GeneralPreferencesInfo mockPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseGeneralPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        GeneralPreferencesInfo result = accountsRestClient.setPreferences(EasyMock.createMock(GeneralPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockPreferencesInfo);
    }

    @Test
    public void getDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences.diff", MOCK_JSON_ELEMENT)
            .get();
        DiffPreferencesInfo mockDiffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);

        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseDiffPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockDiffPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        DiffPreferencesInfo result = accountsRestClient.getDiffPreferences();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockDiffPreferencesInfo);
    }

    @Test
    public void setDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences.diff", "{}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();

        DiffPreferencesInfo mockDiffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseDiffPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockDiffPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        DiffPreferencesInfo result = accountsRestClient.setDiffPreferences(EasyMock.createMock(DiffPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockDiffPreferencesInfo);
    }

    @Test
    public void getEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/preferences.edit", MOCK_JSON_ELEMENT)
            .get();
        EditPreferencesInfo mockEditPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);

        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseEditPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockEditPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        EditPreferencesInfo result = accountsRestClient.getEditPreferences();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockEditPreferencesInfo);
    }

    @Test
    public void setEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/preferences.edit", "{}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();

        EditPreferencesInfo mockEditPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseEditPreferences(MOCK_JSON_ELEMENT))
            .andReturn(mockEditPreferencesInfo)
            .once();
        EasyMock.replay(accountsParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            accountsParser, null, null, "jdoe");

        EditPreferencesInfo result = accountsRestClient.setEditPreferences(EasyMock.createMock(EditPreferencesInfo.class));

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockEditPreferencesInfo);
    }

    @Test
    public void testGetWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/watched.projects", MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.getWatchedProjects();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/watched.projects", "[{\"project\":\"foo\"}]", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");
        ProjectWatchInfo watched = new ProjectWatchInfo();
        watched.project = "foo";
        accountsRestClient.setWatchedProjects(Collections.singletonList(watched));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteWatchedProjects() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/watched.projects:delete", "[{\"project\":\"foo\"}]", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        ProjectWatchInfo watched = new ProjectWatchInfo();
        watched.project = "foo";
        accountsRestClient.deleteWatchedProjects(Collections.singletonList(watched));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getEmails() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/emails", MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.getEmails();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void createEmail() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/emails/john.doe@example.com",
                "{\"email\":\"john.doe@example.com\",\"preferred\":false,\"no_confirmation\":false}",
                MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.createEmail("john.doe@example.com");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void email() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/emails/john.doe@example.com",
                MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.email("john.doe@example.com").get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setStatusByString() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/status", "{\"status\":\"foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.setStatus("foo");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setStatus() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/status", "{\"status\":\"foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.setStatus(new StatusInput("foo"));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setDisplayNameByString() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/displayname", "{\"display_name\":\"foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.setDisplayName("foo");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setDisplayName() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/displayname", "{\"display_name\":\"foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

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
            .expectPut("/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9", MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.starChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUnStarChange() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9", MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        accountsRestClient.unstarChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetStars() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/stars.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9",
                "{\"add\":[],\"remove\":[]}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient,
            null, null, null, "jdoe");

        StarsInput input = new StarsInput(Collections.emptySet(),Collections.emptySet());
        accountsRestClient.setStars("Iccf90a8284f8371a211db9a2824d0617e95a79f9", input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetStars() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/stars.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9", MOCK_JSON_ELEMENT)
            .get();
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        SortedSet<String> labelSet = new TreeSet<>();
        labelSet.add("label1");
        labelSet.add("label2");
        EasyMock.expect(accountsParser.parseStarLabels(MOCK_JSON_ELEMENT))
            .andReturn(labelSet)
            .once();
        EasyMock.replay(accountsParser);
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, accountsParser,
            null, null, "jdoe");

        SortedSet<String> result = accountsRestClient.getStars("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);

        Truth.assertThat(result).isEqualTo(labelSet);
        Truth.assertThat(result).hasSize(2);
        Truth.assertThat(result).containsExactly("label1", "label2");
    }

    @Test
    public void testGetStarredChanges() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/starred.changes", MOCK_JSON_ELEMENT)
            .get();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        ChangeInfo changeInfo = EasyMock.mock(ChangeInfo.class);
        EasyMock.expect(changeInfosParser.parseChangeInfos(MOCK_JSON_ELEMENT))
            .andReturn(Collections.singletonList(changeInfo))
            .once();
        EasyMock.replay(changeInfosParser);

        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, changeInfosParser, "jdoe");

        List<ChangeInfo> results = accountsRestClient.getStarredChanges();

        Truth.assertThat(results).hasSize(1);
        Truth.assertThat(results).contains(changeInfo);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAddSshKey() throws Exception {
        SshKeyInfo mockSshInfo = EasyMock.createMock(SshKeyInfo.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectJsonRequest("/accounts/jdoe/sshkeys", "foo", RestClient.HttpVerb.POST_TEXT_PLAIN, MOCK_JSON_ELEMENT)
            .get();
        SshKeysParser sshKeysParser = EasyMock.createMock(SshKeysParser.class);
        EasyMock.expect(sshKeysParser.parseSshKeyInfo(MOCK_JSON_ELEMENT))
            .andReturn(mockSshInfo).once();
        EasyMock.replay(sshKeysParser);
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            sshKeysParser, null, "jdoe");

        SshKeyInfo result = accountsRestClient.addSshKey("foo");

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockSshInfo);
    }

    @Test
    public void testListSshKeys() throws Exception {
        List<SshKeyInfo> mockSshInfos = EasyMock.createMock(List.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/sshkeys", MOCK_JSON_ELEMENT)
            .get();
        SshKeysParser sshKeysParser = EasyMock.createMock(SshKeysParser.class);
        EasyMock.expect(sshKeysParser.parseSshKeyInfos(MOCK_JSON_ELEMENT))
            .andReturn(mockSshInfos).once();
        EasyMock.replay(sshKeysParser);
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            sshKeysParser, null, "jdoe");

        List<SshKeyInfo> result = accountsRestClient.listSshKeys();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo(mockSshInfos);
    }

    @Test
    public void testDeleteSshKey() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/sshkeys/1", MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        accountsRestClient.deleteSshKey(1);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndex() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/index")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        accountsRestClient.index();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void getExternalIds() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/external.ids",MOCK_JSON_ELEMENT)
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        List<AccountExternalIdInfo> result = accountsRestClient.getExternalIds();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void deleteExternalIds() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/external.ids:delete", "[\"mailto:john.doe@example.com\"]")
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        accountsRestClient.deleteExternalIds(Collections.singletonList("mailto:john.doe@example.com"));

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void deleteDraftComments() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/accounts/jdoe/drafts.delete", "{\"query\":\"message:foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        DeleteDraftCommentsInput input = new DeleteDraftCommentsInput();
        input.query = "message:foo";
        accountsRestClient.deleteDraftComments(input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void generateHttpPassword() throws Exception {
        JsonElement mockedJson = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(mockedJson.getAsString()).andReturn("foo").anyTimes();
        EasyMock.replay(mockedJson);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/password.http", "{\"generate\":true}", mockedJson)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        String result = accountsRestClient.generateHttpPassword();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(result).isEqualTo("foo");
    }

    @Test
    public void setHttpPassword() throws Exception {
        JsonElement mockedJson = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(mockedJson.getAsString()).andReturn("foo").anyTimes();
        EasyMock.replay(mockedJson);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/password.http", "{\"http_password\":\"foo\",\"generate\":false}", mockedJson)
            .expectGetGson()
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        String result = accountsRestClient.setHttpPassword("foo");
        EasyMock.verify(gerritRestClient);

        Truth.assertThat(result).isEqualTo("foo");
    }

    @Test
    public void deleteHttpPassword() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/password.http")
            .get();
        AccountApiRestClient accountsRestClient = getAccountApiRestClient(gerritRestClient, null,
            null, null, "jdoe");

        accountsRestClient.deleteHttpPassword();
        EasyMock.verify(gerritRestClient);
    }

    private AccountsRestClient getAccountsRestClient(GerritRestClient gerritRestClient) {
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        SshKeysParser sshKeyParser = EasyMock.createMock(SshKeysParser.class);
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        return new AccountsRestClient(gerritRestClient, accountsParser, sshKeyParser, changeInfosParser);
    }

    private AccountApiRestClient getAccountApiRestClient(GerritRestClient gerritRestClient, AccountsParser accountsParser,
                                                         SshKeysParser sshKeysParser, ChangeInfosParser changeInfosParser ,
                                                         String name){
        if(accountsParser == null){
            accountsParser = EasyMock.createMock(AccountsParser.class);
        }
        if(sshKeysParser == null){
            sshKeysParser = EasyMock.createMock(SshKeysParser.class);
        }
        if(changeInfosParser == null){
            changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        }
        return new AccountApiRestClient(gerritRestClient, accountsParser, sshKeysParser, changeInfosParser, name);
    }
}
