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

import com.google.gerrit.extensions.api.projects.ProjectApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.api.projects.Projects;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Urs Wolfer
 */
public class ProjectsRestClient extends Projects.NotImplemented implements Projects {

    private final GerritRestContext context;
    private final GerritJson gerritJson;

    public ProjectsRestClient(GerritRestContext context) {
        this.context = context;
        this.gerritJson = context.json();
    }

    @Override
    public ListRequest list() {
        return new ListRequest() {
            @Override
            public SortedMap<String, ProjectInfo> getAsMap() throws RestApiException {
                return ProjectsRestClient.this.list(this);
            }
        };
    }

    @Override
    public ProjectApi name(String name) throws RestApiException {
        return new ProjectApiRestClient(context, name);
    }

    private SortedMap<String, ProjectInfo> list(ListRequest listParameter) throws RestApiException {
        ListRequest.FilterType filterType = listParameter.getFilterType();
        String url = UrlQuery.of("/projects/")
            .flagIf(listParameter.getDescription(), "d")
            .flagIf(listParameter.getShowTree(), "t")
            .paramIfNotEmptyLiteralPlus("p", listParameter.getPrefix())
            .paramIfPositive("n", listParameter.getLimit())
            .paramIfPositive("S", listParameter.getStart())
            .paramsLiteralPlus("b", listParameter.getBranches())
            .paramIf(filterType != null && filterType != ListRequest.FilterType.ALL, "type", filterType)
            .toUrl();

        JsonElement result = context.get(url).asJson();
        if (result == null) {
            return new TreeMap<>();
        }
        return context.json().asSortedMap(result, ProjectInfo.class);
    }

    @Override
    public ProjectApi create(String name) throws RestApiException {
        ProjectInput projectInput = new ProjectInput();
        projectInput.name = name;
        return create(projectInput);
    }

    @Override
    public ProjectApi create(ProjectInput in) throws RestApiException {
        if (in.name == null) {
            throw new IllegalArgumentException("Name must be set in project creation input.");
        }

        String url = String.format("/projects/%s", Url.encode(in.name));
        JsonElement result = context.put(url).body(in, ProjectInput.class).asJson();
        ProjectInfo info = gerritJson.as(result, ProjectInfo.class);
        return new ProjectApiRestClient(context, info.name);
    }
}
