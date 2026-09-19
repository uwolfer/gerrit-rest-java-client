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
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

import java.util.Optional;


/**
 * @author Jun Qiu
 */
public class ChangeEditApiRestClient extends ChangeEditApi.NotImplemented implements ChangeEditApi {

    private final GerritRestContext context;
    private final String id;

    private final GerritJson gerritJson;

    public ChangeEditApiRestClient(GerritRestContext context,
                                   String id) {
        this.context = context;
        this.gerritJson = context.json();
        this.id = id;
    }

    @Override
    public Optional<EditInfo> get() throws RestApiException {
        JsonElement result = context.get(getRequestPath()).asJson();
        if(result.isJsonNull()){
            return Optional.empty();
        }
        return Optional.of(gerritJson.as(result, EditInfo.class));
    }

    @Override
    public void delete() throws RestApiException {
        context.delete(getRequestPath()).send();
    }

    @Override
    public void rebase() throws RestApiException {
        context.post(getRequestPath() + ":rebase").send();
    }

    @Override
    public void publish() throws RestApiException{
        publish(new PublishChangeEditInput());
    }

    @Override
    public void publish(PublishChangeEditInput input) throws RestApiException {
        context.post(getRequestPath() + ":publish").body(input).send();
    }

    @Override
    public Optional<BinaryResult> getFile(String filePath) throws RestApiException {
        String request = getRequestPath() + "/" + filePath;
        return Optional.of(context.get(request).binary("Failed to get file content."));
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
        context.post(getRequestPath()).body(input).send();
    }

    @Override
    public void modifyFile(String filePath, FileContentInput input) throws RestApiException {
        String encodedPath = Url.encode(filePath);
        String request = getRequestPath() + "/" + encodedPath;
        context.request(HttpVerb.PUT_TEXT_PLAIN, request)
            .rawBody(input.binaryContent)
            .sendRaw("Failed to modify file.");
    }

    @Override
    public void deleteFile(String filePath) throws RestApiException {
        String encodedPath = Url.encode(filePath);
        context.delete(getRequestPath() + "/" + encodedPath).send();
    }

    @Override
    public String getCommitMessage() throws RestApiException{
        String request = getRequestPath() + ":message";
        return context.get(request).asString();
    }

    @Override
    public void modifyCommitMessage(String newCommitMessage) throws RestApiException {
        ChangeEditMessageInput input = new ChangeEditMessageInput();
        input.message = newCommitMessage;
        modifyCommitMessage(input);
    }

    public void modifyCommitMessage(ChangeEditMessageInput input) throws RestApiException {
        context.put(getRequestPath() + ":message").body(input).send();
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
