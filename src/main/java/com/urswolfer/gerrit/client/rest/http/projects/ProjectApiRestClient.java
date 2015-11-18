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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ProjectApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.List;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClient extends ProjectApi.NotImplemented implements ProjectApi {
    private final GerritRestClient gerritRestClient;
    private final ProjectsParser projectsParser;
    private final BranchInfoParser branchInfoParser;
    private final String name;

    public ProjectApiRestClient(GerritRestClient gerritRestClient,
                                ProjectsParser projectsParser,
                                BranchInfoParser branchInfoParser,
                                String name) {
        this.gerritRestClient = gerritRestClient;
        this.projectsParser = projectsParser;
        this.branchInfoParser = branchInfoParser;
        this.name = name;
    }

    @Override
    public ProjectInfo get() {
        try {
            JsonElement jsonElement = gerritRestClient.getRequest(projectsUrl());
            return projectsParser.parseSingleProjectInfo(jsonElement);
        } catch (RestApiException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public ProjectApi create() throws RestApiException {
        gerritRestClient.putRequest(projectsUrl());
        return this;
    }

    @Override
    public ProjectApi create(ProjectInput in) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(in);
        gerritRestClient.putRequest(projectsUrl(), body);
        return this;
    }

    @Override
    public ListRefsRequest<BranchInfo> branches() {
        return new ListRefsRequest<BranchInfo>() {
            @Override
            public List<BranchInfo> get() throws RestApiException {
                return ProjectApiRestClient.this.getBranches(this);
            }
        };
    }

    @Override
    public BranchApi branch(String ref) throws RestApiException {
        return new BranchApiRestClient(gerritRestClient, branchInfoParser, this, ref);
    }

    private List<BranchInfo> getBranches(ListRefsRequest<BranchInfo> lbr) throws RestApiException {
        String request = projectsUrl() + branchesUrl(lbr);
        JsonElement branches = gerritRestClient.getRequest(request);
        return branchInfoParser.parseBranchInfos(branches);
    }

    protected String projectsUrl() {
        return "/projects/" + name;
    }

    private String branchesUrl(ListRefsRequest<BranchInfo> lbr) {
        String query = "";

        if (lbr.getLimit() != 0) {
            query = UrlUtils.appendToUrlQuery(query, "n=" + lbr.getLimit());
        }
        if (lbr.getStart() != 0) {
            query = UrlUtils.appendToUrlQuery(query, "s=" + lbr.getStart());
        }
        if (!Strings.isNullOrEmpty(lbr.getSubstring())) {
            query = UrlUtils.appendToUrlQuery(query, "m=" + lbr.getSubstring());
        }
        if (!Strings.isNullOrEmpty(lbr.getRegex())) {
            query = UrlUtils.appendToUrlQuery(query, "r=" + lbr.getRegex());
        }

        String url = "/branches";
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }
        return url;
    }
}
