/*
 * Copyright 2013-2023 Urs Wolfer
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

import com.google.gerrit.extensions.api.accounts.EmailApi;
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;


public class EmailApiRestClient extends EmailApi.NotImplemented implements EmailApi {

    private final AccountsParser accountsParser;

    private final GerritRestClient gerritRestClient;
    private final String name;
    private final String email;

    public EmailApiRestClient(GerritRestClient gerritRestClient,
                              AccountsParser accountsParser,
                              String name,
                              String email) {
        this.gerritRestClient = gerritRestClient;
        this.accountsParser = accountsParser;
        this.name = name;
        this.email = email;
    }

    @Override
    public EmailInfo get() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath());
        return accountsParser.parseSingleEmailInfo(response);
    }

    @Override
    public void delete() throws  RestApiException {
        gerritRestClient.deleteRequest(getRequestPath());
    }

    @Override
    public void setPreferred() throws  RestApiException {
        gerritRestClient.putRequest(getRequestPath() + "/preferred");
    }

    private String getRequestPath() {
        return "/accounts/" + Url.encode(name) + "/emails/" + email;
    }
}
