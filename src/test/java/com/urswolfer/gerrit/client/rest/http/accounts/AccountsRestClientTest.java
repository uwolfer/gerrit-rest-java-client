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

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.ChangeInfosParser;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * @author Thomas Forrer
 */
public class AccountsRestClientTest {
    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final AccountInfo MOCK_ACCOUNT_INFO = EasyMock.createMock(AccountInfo.class);
    private static final SshKeyInfo MOCK_SSHKEY_INFO = EasyMock.createMock(SshKeyInfo.class);

    @Test
    public void testId() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/accounts/jdoe");
        AccountsParser accountsParser = getAccountsParser();
        SshKeysParser sshKeysParser = getSshKeysParser();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, accountsParser, sshKeysParser, changeInfosParser);
        accountsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testSelf() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/accounts/self");
        AccountsParser accountsParser = getAccountsParser();
        SshKeysParser sshKeysParser = getSshKeysParser();
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, accountsParser, sshKeysParser, changeInfosParser);
        accountsRestClient.self().get();

        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testSuggestAccount() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet(
                "/accounts/?suggest&q=jdoe&n=5");
        AccountsRestClient accountsRestClient = new AccountsRestClient(
                gerritRestClient,
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(SshKeysParser.class),
                EasyMock.createMock(ChangeInfosParser.class));

        accountsRestClient.suggestAccounts("jdoe").withLimit(5).get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCreate() throws Exception {
        String username = "foo";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPut("/accounts/" + username, "{\"username\":\"foo\"}", MOCK_JSON_ELEMENT)
            .get();
        AccountsParser accountsParser = new AccountsParserBuilder()
            .expectParseAccountInfo(MOCK_JSON_ELEMENT, MOCK_ACCOUNT_INFO)
            .get();
        SshKeysParser sshParser = EasyMock.createMock(SshKeysParser.class);
        ChangeInfosParser changeInfosParser = EasyMock.createMock(ChangeInfosParser.class);
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, accountsParser, sshParser, changeInfosParser);
        accountsRestClient.create(username);
        EasyMock.verify(gerritRestClient, accountsParser);
    }

    private GerritRestClient gerritRestClientExpectGet(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
            .andReturn(MOCK_JSON_ELEMENT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GerritRestClient gerritRestClientExpectPut(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.putRequest(expectedUrl))
            .andReturn(MOCK_JSON_ELEMENT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GerritRestClient gerritRestClientExpectDelete(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.deleteRequest(expectedUrl))
            .andReturn(MOCK_JSON_ELEMENT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private AccountsParser getAccountsParser() throws Exception {
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        EasyMock.expect(accountsParser.parseAccountInfo(MOCK_JSON_ELEMENT))
                .andReturn(MOCK_ACCOUNT_INFO).once();
        EasyMock.replay(accountsParser);
        return accountsParser;
    }

    private SshKeysParser getSshKeysParser() throws Exception {
        SshKeysParser sshKeysParser = EasyMock.createMock(SshKeysParser.class);
        EasyMock.expect(sshKeysParser.parseSshKeyInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_SSHKEY_INFO).once();
        EasyMock.replay(sshKeysParser);
        return sshKeysParser;
    }
}
