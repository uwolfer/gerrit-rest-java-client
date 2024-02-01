/*
 * Copyright 2013-2024 Urs Wolfer
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

import com.google.gerrit.extensions.api.projects.ChildProjectApi;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

public class ChildProjectApiRestClient extends ChildProjectApi.NotImplemented implements ChildProjectApi {

    private final GerritRestClient gerritRestClient;

    private final ProjectsParser projectsParser;
    private final String name;
    private final String parentUrl;

    public ChildProjectApiRestClient(GerritRestClient gerritRestClient, ProjectsParser projectsParser, String parentUrl, String name) {
        this.gerritRestClient = gerritRestClient;
        this.projectsParser = projectsParser;
        this.parentUrl = parentUrl;
        this.name = name;
    }

    @Override
    public ProjectInfo get() throws RestApiException {
        return get(false);
    }

    @Override
    public ProjectInfo get(boolean recursive) throws RestApiException {
        String requestUrl = childProjectUrl();
        if (recursive) {
            requestUrl = requestUrl + "?recursive";
        }
        JsonElement jsonElement = gerritRestClient.getRequest(requestUrl);
        return projectsParser.parseSingleProjectInfo(jsonElement);
    }

    protected String childProjectUrl() {
        return parentUrl + "/children/" + Url.encode(name);
    }

}
