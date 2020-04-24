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
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, accountsParser,sshKeysParser);
        accountsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testSelf() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/accounts/self");
        AccountsParser accountsParser = getAccountsParser();
        SshKeysParser sshKeysParser = getSshKeysParser();

        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, accountsParser,sshKeysParser);
        accountsRestClient.self().get();

        EasyMock.verify(gerritRestClient, accountsParser);
    }

    @Test
    public void testStarChange() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectPut(
                "/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9");
        AccountsRestClient accountsRestClient = new AccountsRestClient(
                gerritRestClient,
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(SshKeysParser.class));

        accountsRestClient.id("jdoe").starChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUnStarChange() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectDelete(
                "/accounts/jdoe/starred.changes/Iccf90a8284f8371a211db9a2824d0617e95a79f9");
        AccountsRestClient accountsRestClient = new AccountsRestClient(
                gerritRestClient,
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(SshKeysParser.class));

        accountsRestClient.id("jdoe").unstarChange("Iccf90a8284f8371a211db9a2824d0617e95a79f9");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSuggestAccount() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet(
                "/accounts/?suggest&q=jdoe&n=5");
        AccountsRestClient accountsRestClient = new AccountsRestClient(
                gerritRestClient,
                EasyMock.createMock(AccountsParser.class),
                EasyMock.createMock(SshKeysParser.class));

        accountsRestClient.suggestAccounts("jdoe").withLimit(5).get();

        EasyMock.verify(gerritRestClient);
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
        SshKeysParser accountsParser = EasyMock.createMock(SshKeysParser.class);
        EasyMock.expect(accountsParser.parseSshKeyInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_SSHKEY_INFO).once();
        EasyMock.replay(accountsParser);
        return accountsParser;
    }
}
