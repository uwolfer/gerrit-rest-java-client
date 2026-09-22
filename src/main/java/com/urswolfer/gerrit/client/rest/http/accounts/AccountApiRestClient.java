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
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClient extends AccountApi.NotImplemented implements AccountApi {

    private final GerritRestContext context;
    private final String name;

    public AccountApiRestClient(GerritRestContext context,
                                String name) {
        this.context = context;
        this.name = name;
    }

    @Override
    public AccountInfo get() throws RestApiException {
        return context.get(getRequestPath()).as(AccountInfo.class);
    }

    @Override
    public AccountDetailInfo detail() throws RestApiException {
        return context.get(getRequestPath() + "/detail").as(AccountDetailInfo.class);
    }

    @Override
    public boolean getActive() throws RestApiException {
        JsonElement result = context.get(getRequestPath() + "/active").asJson();
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
            context.put(requestPath).send();
        }else {
            context.delete(requestPath).send();
        }
    }

    @Override
    public GeneralPreferencesInfo getPreferences() throws RestApiException {
        return context.get(getRequestPath() + "/preferences").as(GeneralPreferencesInfo.class);
    }

    @Override
    public GeneralPreferencesInfo setPreferences(GeneralPreferencesInfo input)
        throws RestApiException {
        return context.put(getRequestPath() + "/preferences").body(input).as(GeneralPreferencesInfo.class);
    }

    @Override
    public DiffPreferencesInfo getDiffPreferences() throws RestApiException {
        return context.get(getRequestPath() + "/preferences.diff").as(DiffPreferencesInfo.class);
    }

    @Override
    public DiffPreferencesInfo setDiffPreferences(DiffPreferencesInfo input) throws RestApiException {
        return context.put(getRequestPath() + "/preferences.diff").body(input).as(DiffPreferencesInfo.class);
    }

    @Override
    public EditPreferencesInfo getEditPreferences() throws RestApiException {
        return context.get(getRequestPath() + "/preferences.edit").as(EditPreferencesInfo.class);
    }

    @Override
    public EditPreferencesInfo setEditPreferences(EditPreferencesInfo input) throws RestApiException {
        return context.put(getRequestPath() + "/preferences.edit").body(input).as(EditPreferencesInfo.class);
    }

    @Override
    public List<ProjectWatchInfo> getWatchedProjects() throws RestApiException{
        return context.get(getRequestPath() + "/watched.projects").asList(ProjectWatchInfo.class);
    }

    @Override
    public List<ProjectWatchInfo> setWatchedProjects(List<ProjectWatchInfo> in) throws RestApiException{
        return context.post(getRequestPath() + "/watched.projects").body(in).asList(ProjectWatchInfo.class);
    }

    @Override
    public void deleteWatchedProjects(List<ProjectWatchInfo> in) throws RestApiException{
        context.post(getRequestPath() + "/watched.projects:delete").body(in).send();
    }

    @Override
    public void starChange(String id) throws RestApiException {
        context.put(createStarredUrl(id)).send();
    }

    @Override
    public void unstarChange(String id) throws RestApiException {
        context.delete(createStarredUrl(id)).send();
    }

    @Override
    public void setStars(String changeId, StarsInput input) throws RestApiException {
        context.post(createStarsUrl(changeId)).body(input).send();
    }

    @Override
    public SortedSet<String> getStars(String changeId) throws RestApiException {
        return context.get(createStarsUrl(changeId)).asSortedSet(String.class);
    }

    @Override
    public List<ChangeInfo> getStarredChanges() throws RestApiException {
        return context.get(getRequestPath() + "/starred.changes").asList(ChangeInfo.class);
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
        return context.get(getRequestPath() + "/emails").asList(EmailInfo.class);
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
        context.put(getRequestPath() + "/emails/" + input.email).body(input).send();
        return email(input.email);
    }

    @Override
    public EmailApi email(String email) throws RestApiException {
        return new EmailApiRestClient(context, name, email);
    }

    @Override
    public void setStatus(String status) throws RestApiException {
        setStatus(new StatusInput(status));
    }

    public void setStatus(StatusInput input) throws RestApiException {
        context.put(getRequestPath() + "/status").body(input).send();
    }

    @Override
    public void setDisplayName(String displayName) throws RestApiException {
        setDisplayName(new DisplayNameInput(displayName));
    }

    public void setDisplayName(DisplayNameInput input) throws RestApiException {
        context.put(getRequestPath() + "/displayname").body(input).send();
    }

    @Override
    public BinaryResult downloadAvatar(int size) throws RestApiException {
        String request = getRequestPath() + "/avatar?s=" + size;
        return context.get(request).binary("Failed to get avatar.");
    }

    @Override
    public SshKeyInfo addSshKey(String key) throws RestApiException {
        return context.request(HttpVerb.POST_TEXT_PLAIN, getRequestPath() + "/sshkeys")
            .rawBody(key)
            .as(SshKeyInfo.class);
    }

    @Override
    public List<SshKeyInfo> listSshKeys() throws RestApiException {
        return context.get(getRequestPath() + "/sshkeys").asList(SshKeyInfo.class);
    }

    @Override
    public void deleteSshKey(int seq) throws RestApiException {
        context.delete(getRequestPath()+ "/sshkeys/" + seq).send();
    }

    @Override
    public void index() throws RestApiException {
        context.post(getRequestPath() + "/index").send();
    }

    @Override
    public List<AccountExternalIdInfo> getExternalIds() throws RestApiException {
        return context.get(getRequestPath() + "/external.ids").asList(AccountExternalIdInfo.class);
    }

    @Override
    public void deleteExternalIds(List<String> externalIds) throws RestApiException {
        context.post(getRequestPath() + "/external.ids:delete").body(externalIds).send();
    }

    @Override
    public List<DeletedDraftCommentInfo> deleteDraftComments(DeleteDraftCommentsInput input)
        throws RestApiException {
        return context.post(getRequestPath() + "/drafts.delete").body(input).asList(DeletedDraftCommentInfo.class);
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
        JsonElement result = context.put(getRequestPath() + "/password.http").body(input).asJson();
        return result.getAsString();
    }

    public void deleteHttpPassword() throws RestApiException {
        context.delete(getRequestPath() + "/password.http").send();
    }

    private String getRequestPath() {
        return "/accounts/" + Url.encode(name);
    }
}
