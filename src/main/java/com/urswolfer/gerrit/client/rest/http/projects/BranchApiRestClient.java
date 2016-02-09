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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.BranchInput;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * @author Ingo Rissmann
 */
public class BranchApiRestClient extends BranchApi.NotImplemented implements BranchApi {
    private final GerritRestClient gerritRestClient;
    private final BranchInfoParser branchInfoParser;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public BranchApiRestClient(GerritRestClient gerritRestClient,
                               BranchInfoParser branchInfoParser,
                               ProjectApiRestClient projectApiRestClient,
                               String name) {
        this.gerritRestClient = gerritRestClient;
        this.branchInfoParser = branchInfoParser;
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public BranchApi create(BranchInput in) throws RestApiException {
        String json = gerritRestClient.getGson().toJson(in);
        gerritRestClient.putRequest(branchUrl(), json);
        return this;
    }

    @Override
    public BranchInfo get() throws RestApiException {
        JsonElement jsonElement = gerritRestClient.getRequest(branchUrl());
        return Iterables.getOnlyElement(branchInfoParser.parseBranchInfos(jsonElement));
    }

    @Override
    public void delete() throws RestApiException {
        gerritRestClient.deleteRequest(branchUrl());
    }

    @Override
    public BinaryResult file(String path) throws RestApiException {
        String encodedPath = Url.encode(path);
        String request = branchUrl() + "/files/" + encodedPath + "/content";
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw new RestApiException("Failed to get file content.", e);
        }
    }

    protected String branchUrl() {
        return projectApiRestClient.projectsUrl() + "/branches/" + name;
    }
}
