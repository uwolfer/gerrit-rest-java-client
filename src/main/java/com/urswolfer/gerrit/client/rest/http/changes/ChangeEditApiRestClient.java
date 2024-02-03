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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.ChangeEditApi;
import com.google.gerrit.extensions.api.changes.FileContentInput;
import com.google.gerrit.extensions.api.changes.PublishChangeEditInput;
import com.google.gerrit.extensions.common.EditInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.CommitInfosParser;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Optional;

import static com.urswolfer.gerrit.client.rest.RestClient.HttpVerb.GET;

/**
 * @author Jun Qiu
 */
public class ChangeEditApiRestClient extends ChangeEditApi.NotImplemented implements ChangeEditApi {

    private final GerritRestClient gerritRestClient;
    private final String id;

    private final CommitInfosParser commitInfosParser;

    public ChangeEditApiRestClient(GerritRestClient gerritRestClient, CommitInfosParser commitInfosParser, String id) {
        this.gerritRestClient = gerritRestClient;
        this.id = id;
        this.commitInfosParser = commitInfosParser;
    }

    @Override
    public Optional<EditInfo> get() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(getRequestPath());
        if(result.isJsonNull()){
            return Optional.empty();
        }
        return Optional.of(commitInfosParser.parseEditInfo(result));
    }

    @Override
    public void delete() throws RestApiException {
        gerritRestClient.deleteRequest(getRequestPath());
    }

    @Override
    public void rebase() throws RestApiException {
        String request = getRequestPath() + ":rebase";
        gerritRestClient.postRequest(request);
    }

    @Override
    public void publish() throws RestApiException{
        publish(new PublishChangeEditInput());
    }

    @Override
    public void publish(PublishChangeEditInput input) throws RestApiException {
        String request = getRequestPath() + ":publish";
        String json = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(request,json);
    }

    @Override
    public Optional<BinaryResult> getFile(String filePath) throws RestApiException {
        String request = getRequestPath() + "/" + filePath;
        try {
            HttpResponse response = gerritRestClient.request(request, null, GET);
            return Optional.of(BinaryResultUtils.createBinaryResult(response));
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to get file content.", e);
        }
    }

    @Override
    public void renameFile(String oldFilePath, String newFilePath) throws RestApiException {
        ChangeEditInput input = new ChangeEditInput();
        input.old_path = oldFilePath;
        input.new_path = newFilePath;
        changeFile(input);
    }

    @Override
    public void restoreFile(String filePath) throws RestApiException {
        ChangeEditInput input = new ChangeEditInput();
        input.restore_path = filePath;
        changeFile(input);
    }

    private void changeFile(ChangeEditInput input) throws RestApiException {
        String json = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(getRequestPath(),json);
    }

    @Override
    public void modifyFile(String filePath, FileContentInput input) throws RestApiException {
        String encodedPath = Url.encode(filePath);
        String request = getRequestPath() + "/" + encodedPath;
        try {
            gerritRestClient.request(request, input.binary_content, HttpVerb.PUT_TEXT_PLAIN);
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to modify file.", e);
        }
    }

    @Override
    public void deleteFile(String filePath) throws RestApiException {
        String encodedPath = Url.encode(filePath);
        String request = getRequestPath() + "/" + encodedPath;
        gerritRestClient.deleteRequest(request);
    }

    @Override
    public String getCommitMessage() throws RestApiException{
        String request = getRequestPath() + ":message";
        JsonElement result = gerritRestClient.getRequest(request);
        return result.getAsString();
    }

    @Override
    public void modifyCommitMessage(String newCommitMessage) throws RestApiException {
        ChangeEditMessageInput input = new ChangeEditMessageInput();
        input.message = newCommitMessage;
        modifyCommitMessage(input);
    }

    public void modifyCommitMessage(ChangeEditMessageInput input) throws RestApiException {
        String request = getRequestPath() + ":message";
        String json = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(request,json);
    }

    protected String getRequestPath() { return "/changes/" + id + "/edit"; }

    protected static class ChangeEditInput {
        public String restore_path;
        public String old_path;
        public String new_path;
    }

    public static class ChangeEditMessageInput {
        public String message;
    }
}
