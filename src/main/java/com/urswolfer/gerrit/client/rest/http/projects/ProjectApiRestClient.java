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
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.gson.GsonFactory;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClient extends ProjectApi.NotImplemented implements ProjectApi {


    private final GerritRestContext context;
    private final GerritJson gerritJson;
    private final String name;

    public ProjectApiRestClient(GerritRestContext context,
                                String name) {
        this.context = context;
        this.gerritJson = context.json();
        this.name = name;
    }

    @Override
    public ProjectApi create() throws RestApiException {
        context.put(projectsUrl()).send();
        return this;
    }

    @Override
    public ProjectApi create(ProjectInput in) throws RestApiException {
        context.put(projectsUrl()).body(in).send();
        return this;
    }

    @Override
    public ProjectInfo get() {
        try {
            return context.get(projectsUrl()).as(ProjectInfo.class);
        } catch (RestApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String description() throws RestApiException {
        return context.get(projectsUrl()+"/description").asString();
    }

    @Override
    public ProjectAccessInfo access() throws RestApiException {
        return context.get(projectsUrl() + "/access").as(ProjectAccessInfo.class);
    }

    @Override
    public ProjectAccessInfo access(ProjectAccessInput p) throws RestApiException {
        return context.post(projectsUrl() + "/access").body(p).as(ProjectAccessInfo.class);
    }

    @Override
    public AccessCheckInfo checkAccess(AccessCheckInput in) throws RestApiException {
        return context.post(projectsUrl() + "/check.access").body(in).as(AccessCheckInfo.class);
    }

    @Override
    public ConfigInfo config() throws RestApiException {
        String request = projectsUrl() + "/config";
        JsonElement result = context.get(request).asJson();
        return GsonFactory.createForConfigInfo().fromJson(result, ConfigInfo.class);
    }

    @Override
    public ConfigInfo config(ConfigInput in) throws RestApiException {
        String request = projectsUrl() + "/config";
        JsonElement result = context.put(request).body(in).asJson();
        return GsonFactory.createForConfigInfo().fromJson(result, ConfigInfo.class);
    }

    @Override
    public void description(DescriptionInput in) throws RestApiException {
        context.put(projectsUrl() + "/description").body(in).send();
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
        return new BranchApiRestClient(context, this, ref);
    }

    private List<BranchInfo> getBranches(ListRefsRequest<BranchInfo> lbr) throws RestApiException {
        return context.get(projectsUrl() + branchesUrl(lbr)).asList(BranchInfo.class);
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
        return new TagApiRestClient(context, this, ref);
    }

    private List<TagInfo> getTagInfos(ListRefsRequest<TagInfo> lrr) throws RestApiException {
        return context.get(projectsUrl() + tagsUrl(lrr)).asList(TagInfo.class);
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
        JsonElement children = context.get(request).asJson();
        return new ArrayList<>(gerritJson.asSortedMap(children, ProjectInfo.class).values());
    }

    @Override
    public ChildProjectApi child(String name) {
        return new ChildProjectApiRestClient(context, projectsUrl(), name);
    }

    @Override
    public CommitApi commit(String commit) {
        return new CommitApiRestClient(context, this, commit);
    }

    @Override
    public String head() throws RestApiException {
        return context.get(projectsUrl()+"/HEAD").asString();
    }

    @Override
    public void head(String head) throws RestApiException {
        String request = projectsUrl() + "/HEAD";
        HeadInput input = new HeadInput();
        input.ref = head;
        context.put(request).body(input).send();
    }

    @Override
    public String parent() throws RestApiException {
        return context.get(projectsUrl()+"/parent").asString();
    }

    @Override
    public void parent(String parent) throws RestApiException {
        String request = projectsUrl() + "/parent";
        ParentInput input = new ParentInput();
        input.parent = parent;
        context.put(request).body(input).send();
    }

    @Override
    public void index(boolean indexChildren) throws RestApiException {
        String request = projectsUrl() + "/index";
        IndexProjectInput input = new IndexProjectInput();
        input.indexChildren = indexChildren;
        context.post(request).body(input).send();
    }

    @Override
    public void indexChanges() throws RestApiException {
        context.post(projectsUrl() + "/index.changes").send();
    }

    @Override
    public LabelApi label(String labelName) throws RestApiException {
        return new LabelApiRestClient(context, this, labelName);
    }

    @Override
    public void labels(BatchLabelInput input) throws RestApiException {
        context.post(projectsUrl() + "/labels").body(input).send();
    }

    protected String projectsUrl() {
        return "/projects/" + Url.encode(name);
    }

    private String branchesUrl(ListRefsRequest<BranchInfo> lbr) {
        return UrlQuery.of("/branches")
            .paramIfNonZero("n", lbr.getLimit())
            .paramIfNonZero("s", lbr.getStart())
            .paramIfNotEmpty("m", lbr.getSubstring())
            .paramIfNotEmpty("r", lbr.getRegex())
            .toUrl();
    }

    private String tagsUrl(ListRefsRequest<TagInfo> lrr) {
        if (!Strings.isNullOrEmpty(lrr.getSubstring()) || !Strings.isNullOrEmpty(lrr.getRegex())) {
            throw new NotImplementedException();
        }
        return UrlQuery.of("/tags")
            .paramIfNonZero("n", lrr.getLimit())
            .paramIfNonZero("s", lrr.getStart())
            .toUrl();
    }
}
