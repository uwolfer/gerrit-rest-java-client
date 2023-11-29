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

import com.google.gerrit.extensions.api.accounts.DeleteDraftCommentsInput;
import com.google.gerrit.extensions.api.accounts.DeletedDraftCommentInfo;
import com.google.gerrit.extensions.api.accounts.DisplayNameInput;
import com.google.gerrit.extensions.api.accounts.EmailApi;
import com.google.gerrit.extensions.api.accounts.EmailInput;
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
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gerrit.extensions.common.HttpPasswordInput;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.accounts.AccountApi;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.ChangeInfosParser;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.SortedSet;

import static com.urswolfer.gerrit.client.rest.RestClient.HttpVerb.GET;

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClient extends AccountApi.NotImplemented implements AccountApi {

    private final AccountsParser accountsParser;
    private final SshKeysParser sshKeysParser;
    private final ChangeInfosParser changeInfosParser;

    private final GerritRestClient gerritRestClient;
    private final String name;

    public AccountApiRestClient(GerritRestClient gerritRestClient,
                                AccountsParser accountsParser,
                                SshKeysParser sshKeysParser,
                                ChangeInfosParser changeInfosParser,
                                String name) {
        this.gerritRestClient = gerritRestClient;
        this.accountsParser = accountsParser;
        this.sshKeysParser = sshKeysParser;
        this.changeInfosParser = changeInfosParser;
        this.name = name;
    }

    @Override
    public AccountInfo get() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath());
        return accountsParser.parseAccountInfo(result);
    }

    @Override
    public AccountDetailInfo detail() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/detail");
        return accountsParser.parseAccountDetailInfo(result);
    }

    @Override
    public boolean getActive() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/active");
        if(result == null) {
            return false;
        }
        String parsed = result.getAsString();
        if(parsed == null){
            return false;
        }
        return parsed.equals("ok");
    }

    @Override
    public void setActive(boolean active) throws RestApiException {
        String requestPath = getRequestPath() + "/active";
        if(active){
            gerritRestClient.putRequest(requestPath);
        }else {
            gerritRestClient.deleteRequest(requestPath);
        }
    }

    @Override
    public GeneralPreferencesInfo getPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/preferences");
        return accountsParser.parseGeneralPreferences(result);
    }

    @Override
    public GeneralPreferencesInfo setPreferences(GeneralPreferencesInfo input)
        throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(getRequestPath() + "/preferences", body);
        return accountsParser.parseGeneralPreferences(result);
    }

    @Override
    public DiffPreferencesInfo getDiffPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/preferences.diff");
        return accountsParser.parseDiffPreferences(result);
    }

    @Override
    public DiffPreferencesInfo setDiffPreferences(DiffPreferencesInfo input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(getRequestPath() + "/preferences.diff", body);
        return accountsParser.parseDiffPreferences(result);
    }

    @Override
    public EditPreferencesInfo getEditPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/preferences.edit");
        return accountsParser.parseEditPreferences(result);
    }

    @Override
    public EditPreferencesInfo setEditPreferences(EditPreferencesInfo input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(getRequestPath() + "/preferences.edit", body);
        return accountsParser.parseEditPreferences(result);
    }

    @Override
    public List<ProjectWatchInfo> getWatchedProjects() throws RestApiException{
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/watched.projects");
        return accountsParser.parseProjectWatchInfos(result);
    }

    @Override
    public List<ProjectWatchInfo> setWatchedProjects(List<ProjectWatchInfo> in) throws RestApiException{
        String body = gerritRestClient.getGson().toJson(in);
        JsonElement result = gerritRestClient.postRequest(getRequestPath() + "/watched.projects", body);
        return accountsParser.parseProjectWatchInfos(result);
    }

    @Override
    public void deleteWatchedProjects(List<ProjectWatchInfo> in) throws RestApiException{
        String body = gerritRestClient.getGson().toJson(in);
        gerritRestClient.postRequest(getRequestPath() + "/watched.projects:delete", body);
    }

    @Override
    public void starChange(String id) throws RestApiException {
        gerritRestClient.putRequest(createStarredUrl(id));
    }

    @Override
    public void unstarChange(String id) throws RestApiException {
        gerritRestClient.deleteRequest(createStarredUrl(id));
    }

    @Override
    public void setStars(String changeId, StarsInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(createStarsUrl(changeId), body);
    }

    @Override
    public SortedSet<String> getStars(String changeId) throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(createStarsUrl(changeId));
        return accountsParser.parseStarLabels(result);
    }

    @Override
    public List<ChangeInfo> getStarredChanges() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath() + "/starred.changes");
        return changeInfosParser.parseChangeInfos(response);
    }

    /**
     * Starred-endpoint added in Gerrit 2.8.
     */
    private String createStarredUrl(String id) {
        return getRequestPath() + "/starred.changes/" + id;
    }

    /**
     * Stars endpoint from 2.13 onward provides labels to stars
     */
    private String createStarsUrl(String id) {
        return getRequestPath() + "/stars.changes/" + id;
    }

    @Override
    public List<EmailInfo> getEmails() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath() + "/emails");
        return accountsParser.parseEmailInfos(response);
    }

    @Override
    public void deleteEmail(String email) throws RestApiException {
        email(email).delete();
    }

    public EmailApi createEmail(String email) throws RestApiException {
        EmailInput emailInput = new EmailInput();
        emailInput.email = email;
        return createEmail(emailInput);
    }

    @Override
    public EmailApi createEmail(EmailInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(getRequestPath() + "/emails/" + input.email, body);
        return email(input.email);
    }

    @Override
    public EmailApi email(String email) throws RestApiException {
        return new EmailApiRestClient(gerritRestClient, accountsParser, name, email);
    }

    @Override
    public void setStatus(String status) throws RestApiException {
        setStatus(new StatusInput(status));
    }

    public void setStatus(StatusInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(getRequestPath() + "/status", body);
    }

    @Override
    public void setDisplayName(String displayName) throws RestApiException {
        setDisplayName(new DisplayNameInput(displayName));
    }

    public void setDisplayName(DisplayNameInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(getRequestPath() + "/displayname", body);
    }

    @Override
    public BinaryResult downloadAvatar(int size) throws RestApiException {
        String request = getRequestPath() + "/avatar?s=" + size;
        try {
            HttpResponse response = gerritRestClient.request(request, null, GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to get avatar.", e);
        }
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

    @Override
    public void index() throws RestApiException {
        gerritRestClient.postRequest(getRequestPath() + "/index");
    }

    @Override
    public List<AccountExternalIdInfo> getExternalIds() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath() + "/external.ids");
        return accountsParser.parseAccountExternalIdInfos(result);
    }

    @Override
    public void deleteExternalIds(List<String> externalIds) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(externalIds);
        gerritRestClient.postRequest(getRequestPath() + "/external.ids:delete", body);
    }

    @Override
    public List<DeletedDraftCommentInfo> deleteDraftComments(DeleteDraftCommentsInput input)
        throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.postRequest(getRequestPath() + "/drafts.delete", body);
        return accountsParser.parseDeleteDraftCommentInfos(result);
    }

    @Override
    public String generateHttpPassword() throws RestApiException {
        HttpPasswordInput input = new HttpPasswordInput();
        input.generate = true;
        return setHttpPassword(input);
    }

    @Override
    public String setHttpPassword(String httpPassword) throws RestApiException {
        HttpPasswordInput input = new HttpPasswordInput();
        input.httpPassword = httpPassword;
        return setHttpPassword(input);
    }

    public String setHttpPassword(HttpPasswordInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(getRequestPath() + "/password.http", body);
        return result.getAsString();
    }

    public void deleteHttpPassword() throws RestApiException {
        gerritRestClient.deleteRequest(getRequestPath() + "/password.http");
    }

    private String getRequestPath() {
        return "/accounts/" + Url.encode(name);
    }
}
