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
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.accounts.AccountApi;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClient extends AccountApi.NotImplemented implements AccountApi {

    private final AccountsParser accountsParser;
    private final SshKeysParser sshKeysParser;

    private final GerritRestClient gerritRestClient;
    private final String name;

    public AccountApiRestClient(GerritRestClient gerritRestClient,
                                AccountsParser accountsParser,
                                SshKeysParser sshKeysParser,
                                String name) {
        this.gerritRestClient = gerritRestClient;
        this.accountsParser = accountsParser;
        this.sshKeysParser = sshKeysParser;
        this.name = name;
    }

    @Override
    public AccountInfo get() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath());
        return accountsParser.parseAccountInfo(result);
    }

    @Override
    public void starChange(String id) throws RestApiException {
        gerritRestClient.putRequest(createStarUrl(id));
    }

    @Override
    public void unstarChange(String id) throws RestApiException {
        gerritRestClient.deleteRequest(createStarUrl(id));
    }

    /**
     * Star-endpoint added in Gerrit 2.8.
     */
    private String createStarUrl(String id) {
        return getRequestPath() + "/starred.changes/" + id;
    }

    @Override
    public BinaryResult downloadAvatar(int size) throws RestApiException {
        String request = getRequestPath() + "/avatar?s=" + size;
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw new RestApiException("Failed to get avatar.", e);
        }
    }

    private String getRequestPath() {
        return "/accounts/" + Url.encode(name);
    }

    @Override
    public SshKeyInfo addSshKey(String key) throws RestApiException {
        String request = getRequestPath() + "/sshkeys";
        JsonElement result = gerritRestClient.requestJson(request,key, HttpVerb.POST_TEXT_PLAIN);
        return sshKeysParser.parseSshKeyInfo(result);
    }

    @Override
    public List<SshKeyInfo> listSshKeys() throws RestApiException {
        String request = getRequestPath() + "/sshkeys";
        JsonElement result = gerritRestClient.getRequest(request);
        return sshKeysParser.parseSshKeyInfos(result);
    }

    @Override
    public void deleteSshKey(int seq) throws RestApiException {
        String request = getRequestPath()+ "/sshkeys/" + seq;
        gerritRestClient.deleteRequest(request);
    }
}
