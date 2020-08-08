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
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpResponse;

/**
 * @author Jun Qiu
 */
public class ChangeEditApiRestClient extends ChangeEditApi.NotImplemented implements ChangeEditApi {

    private final GerritRestClient gerritRestClient;
    private final String id;

    public ChangeEditApiRestClient(GerritRestClient gerritRestClient, String id) {
        this.gerritRestClient = gerritRestClient;
        this.id = id;
    }

    @Override
    public Optional<BinaryResult> getFile(String filePath) throws RestApiException {
        String request = getRequestPath() + "/" + filePath;
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return Optional.of(BinaryResultUtils.createBinaryResult(response));
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to get file content.", e);
        }
    }

    @Override
    public void modifyFile(String filePath, FileContentInput input) throws RestApiException {
        String request = getRequestPath() + "/" + filePath;
        try {
            gerritRestClient.request(request, input.binary_content, HttpVerb.PUT_TEXT_PLAIN);
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to modify file.", e);
        }
    }

    @Override
    public void publish(PublishChangeEditInput input) throws RestApiException {
	String request = getRequestPath() + ":publish";
        String json = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(request,json);
    }

    protected String getRequestPath() { return "/changes/" + id + "/edit"; }
}
