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
import com.google.gerrit.extensions.api.access.ProjectAccessInfo;
import com.google.gerrit.extensions.api.access.ProjectAccessInput;
import com.google.gerrit.extensions.api.config.AccessCheckInfo;
import com.google.gerrit.extensions.api.config.AccessCheckInput;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ChildProjectApi;
import com.google.gerrit.extensions.api.projects.CommitApi;
import com.google.gerrit.extensions.api.projects.ConfigInfo;
import com.google.gerrit.extensions.api.projects.ConfigInput;
import com.google.gerrit.extensions.api.projects.DescriptionInput;
import com.google.gerrit.extensions.api.projects.HeadInput;
import com.google.gerrit.extensions.api.projects.IndexProjectInput;
import com.google.gerrit.extensions.api.projects.LabelApi;
import com.google.gerrit.extensions.api.projects.ParentInput;
import com.google.gerrit.extensions.api.projects.ProjectApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.api.projects.TagApi;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.common.BatchLabelInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.projects.parsers.ProjectCommitInfoParser;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.List;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClient extends ProjectApi.NotImplemented implements ProjectApi {
    private final GerritRestClient gerritRestClient;
    private final ProjectsParser projectsParser;
    private final BranchInfoParser branchInfoParser;
    private final TagInfoParser tagInfoParser;
    private final ProjectCommitInfoParser projectCommitInfoParser;
    private final String name;

    public ProjectApiRestClient(GerritRestClient gerritRestClient,
                                ProjectsParser projectsParser,
                                BranchInfoParser branchInfoParser,
                                TagInfoParser tagInfoParser,
                                ProjectCommitInfoParser projectCommitInfoParser,
                                String name) {
        this.gerritRestClient = gerritRestClient;
        this.projectsParser = projectsParser;
        this.branchInfoParser = branchInfoParser;
        this.tagInfoParser = tagInfoParser;
        this.projectCommitInfoParser = projectCommitInfoParser;
        this.name = name;
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
    public ProjectInfo get() {
        try {
            JsonElement jsonElement = gerritRestClient.getRequest(projectsUrl());
            return projectsParser.parseSingleProjectInfo(jsonElement);
        } catch (RestApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(projectsUrl()+"/description");
        return result.getAsString();
    }

    @Override
    public ProjectAccessInfo access() throws RestApiException {
        String request = projectsUrl() + "/access";
        JsonElement result = gerritRestClient.getRequest(request);
        return projectsParser.parseProjectAccessInfo(result);
    }

    @Override
    public ProjectAccessInfo access(ProjectAccessInput p) throws RestApiException {
        String request = projectsUrl() + "/access";
        String params = projectsParser.generateProjectAccessInput(p);
        JsonElement result = gerritRestClient.postRequest(request, params);
        return projectsParser.parseProjectAccessInfo(result);
    }

    @Override
    public AccessCheckInfo checkAccess(AccessCheckInput in) throws RestApiException {
        String request = projectsUrl() + "/check.access";
        String params = gerritRestClient.getGson().toJson(in);
        JsonElement result = gerritRestClient.postRequest(request, params);
        return projectsParser.parseAccessCheckInfo(result);
    }

    @Override
    public ConfigInfo config() throws RestApiException {
        String request = projectsUrl() + "/config";
        JsonElement result = gerritRestClient.getRequest(request);
        return projectsParser.parseConfigInfo(result);
    }

    @Override
    public ConfigInfo config(ConfigInput in) throws RestApiException {
        String request = projectsUrl() + "/config";
        String body = gerritRestClient.getGson().toJson(in);
        JsonElement result = gerritRestClient.putRequest(request, body);
        return projectsParser.parseConfigInfo(result);
    }

    @Override
    public void description(DescriptionInput in) throws RestApiException {
        String request = projectsUrl() + "/description";
        String body = gerritRestClient.getGson().toJson(in);
        gerritRestClient.putRequest(request, body);
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

    @Override
    public ListRefsRequest<TagInfo> tags() {
        return new ListRefsRequest<TagInfo>() {
            @Override
            public List<TagInfo> get() throws RestApiException {
                return ProjectApiRestClient.this.getTagInfos(this);
            }
        };
    }

    @Override
    public TagApi tag(String ref) throws RestApiException {
        return new TagApiRestClient(gerritRestClient, tagInfoParser, this, ref);
    }

    private List<TagInfo> getTagInfos(ListRefsRequest<TagInfo> lrr) throws RestApiException {
        String request = projectsUrl() + tagsUrl(lrr);
        JsonElement tags = gerritRestClient.getRequest(request);
        return tagInfoParser.parseTagInfos(tags);
    }

    @Override
    public List<ProjectInfo> children() throws RestApiException {
        return children(false);
    }

    @Override
    public List<ProjectInfo> children(boolean recursive) throws RestApiException {
        String request = projectsUrl() + "/children";
        if(recursive){
            request = request + "?recursive";
        }
        JsonElement children = gerritRestClient.getRequest(request);
        return projectsParser.parseProjectInfosList(children);
    }

    @Override
    public ChildProjectApi child(String name) {
        return new ChildProjectApiRestClient(gerritRestClient, projectsParser, projectsUrl(), name);
    }

    @Override
    public CommitApi commit(String commit) {
        return new CommitApiRestClient(gerritRestClient, this, projectCommitInfoParser, commit);
    }

    @Override
    public String head() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(projectsUrl()+"/HEAD");
        return result.getAsString();
    }

    @Override
    public void head(String head) throws RestApiException {
        String request = projectsUrl() + "/HEAD";
        HeadInput input = new HeadInput();
        input.ref = head;
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(request, body);
    }

    @Override
    public String parent() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest(projectsUrl()+"/parent");
        return result.getAsString();
    }

    @Override
    public void parent(String parent) throws RestApiException {
        String request = projectsUrl() + "/parent";
        ParentInput input = new ParentInput();
        input.parent = parent;
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(request, body);
    }

    @Override
    public void index(boolean indexChildren) throws RestApiException {
        String request = projectsUrl() + "/index";
        IndexProjectInput input = new IndexProjectInput();
        input.indexChildren = indexChildren;
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(request, body);
    }

    @Override
    public void indexChanges() throws RestApiException {
        String request = projectsUrl() + "/index.changes";
        gerritRestClient.postRequest(request);
    }

    @Override
    public LabelApi label(String labelName) throws RestApiException {
        return new LabelApiRestClient(gerritRestClient, this, labelName);
    }

    @Override
    public void labels(BatchLabelInput input) throws RestApiException {
        String request = projectsUrl() + "/labels";
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(request, body);
    }

    protected String projectsUrl() {
        return "/projects/" + Url.encode(name);
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

    private String tagsUrl(ListRefsRequest<TagInfo> lrr) {
        String query = "";

        if (lrr.getLimit() != 0) {
            query = UrlUtils.appendToUrlQuery(query, "n=" + lrr.getLimit());
        }
        if (lrr.getStart() != 0) {
            query = UrlUtils.appendToUrlQuery(query, "s=" + lrr.getStart());
        }
        if (!Strings.isNullOrEmpty(lrr.getSubstring()) || !Strings.isNullOrEmpty(lrr.getRegex())) {
            throw new NotImplementedException();
        }

        String url = "/tags";
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }
        return url;
    }
}
