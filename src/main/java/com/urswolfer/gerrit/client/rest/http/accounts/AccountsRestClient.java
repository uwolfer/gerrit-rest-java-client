/*
 * Copyright 2013-2014 Urs Wolfer
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

import com.google.gerrit.extensions.api.accounts.AccountInput;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.accounts.AccountApi;
import com.urswolfer.gerrit.client.rest.accounts.Accounts;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.ChangeInfosParser;

import java.util.List;

/**
 * @author Urs Wolfer
 */
public class AccountsRestClient extends Accounts.NotImplemented implements Accounts {

    private final GerritRestClient gerritRestClient;
    private final AccountsParser accountsParser;
    private final SshKeysParser sshKeysParser;
    private final ChangeInfosParser changeInfosParser;

    public AccountsRestClient(GerritRestClient gerritRestClient, AccountsParser accountsParser,
                              SshKeysParser sshKeysParser, ChangeInfosParser changeInfosParser) {
        this.gerritRestClient = gerritRestClient;
        this.accountsParser = accountsParser;
        this.sshKeysParser = sshKeysParser;
        this.changeInfosParser = changeInfosParser;
    }

    @Override
    public AccountApi id(String id) throws RestApiException {
        return new AccountApiRestClient(gerritRestClient, accountsParser, sshKeysParser, changeInfosParser, id);
    }

    @Override
    public AccountApi id(int id) throws RestApiException {
        return id(String.valueOf(id));
    }

    @Override
    public AccountApi self() throws RestApiException {
        return id("self");
    }

    /**
     * Added in Gerrit 2.11.
     */
    @Override
    public SuggestAccountsRequest suggestAccounts() throws RestApiException {
        return new SuggestAccountsRequest() {
            @Override
            public List<AccountInfo> get() throws RestApiException {
                return AccountsRestClient.this.suggestAccounts(this);
            }
        };
    }

    /**
     * Added in Gerrit 2.11.
     */
    @Override
    public SuggestAccountsRequest suggestAccounts(String query) throws RestApiException {
        return suggestAccounts().withQuery(query);
    }

    @Override
    public AccountApi create(String username) throws RestApiException {
        AccountInput userInput = new AccountInput();
        userInput.username = username;
        return create(userInput);
    }

    @Override
    public AccountApi create(AccountInput input) throws RestApiException {
        String requestPath = String.format("/accounts/%s", Url.encode(input.username));
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(requestPath,body);
        AccountInfo info = accountsParser.parseAccountInfo(result);
        return new AccountApiRestClient(gerritRestClient, accountsParser, sshKeysParser, changeInfosParser, info.username);
    }

    private List<AccountInfo> suggestAccounts(SuggestAccountsRequest r) throws RestApiException {
        String encodedQuery = Url.encode(r.getQuery());
        return getSuggestAccounts(String.format("q=%s&n=%s", encodedQuery, r.getLimit()));
    }

    private List<AccountInfo> getSuggestAccounts(String queryPart) throws RestApiException {
        String request = String.format("/accounts/?suggest&%s", queryPart);
        JsonElement suggestedReviewers = gerritRestClient.getRequest(request);
        return accountsParser.parseAccountInfos(suggestedReviewers);
    }
}
