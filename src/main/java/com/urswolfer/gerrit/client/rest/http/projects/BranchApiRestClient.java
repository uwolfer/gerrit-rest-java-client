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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.BranchInput;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlEncoding;

/**
 * @author Ingo Rissmann
 */
public class BranchApiRestClient extends BranchApi.NotImplemented implements BranchApi {
    private final GerritRestContext context;
    private final GerritJson gerritJson;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public BranchApiRestClient(GerritRestContext context,
                               ProjectApiRestClient projectApiRestClient,
                               String name) {
        this.context = context;
        this.gerritJson = context.json();
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public BranchApi create(BranchInput in) throws RestApiException {
        context.put(branchUrl()).body(in).send();
        return this;
    }

    @Override
    public BranchInfo get() throws RestApiException {
        JsonElement jsonElement = context.get(branchUrl()).asJson();
        return Iterables.getOnlyElement(gerritJson.asList(jsonElement, BranchInfo.class));
    }

    @Override
    public void delete() throws RestApiException {
        context.delete(branchUrl()).send();
    }

    @Override
    public BinaryResult file(String path) throws RestApiException {
        String encodedPath = Url.encode(path);
        String request = branchUrl() + "/files/" + encodedPath + "/content";
        return context.get(request).binary("Failed to get file content.");
    }

    protected String branchUrl() {
        return projectApiRestClient.projectsUrl() + "/branches/" + UrlEncoding.pathSegment(name);
    }
}
