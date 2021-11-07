/*
 * Copyright 2013-2021 Urs Wolfer
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

import com.google.gerrit.extensions.api.projects.LabelApi;
import com.google.gerrit.extensions.common.LabelDefinitionInput;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

/**
 * @author Réda Housni Alaoui
 */
public class LabelApiRestClient extends LabelApi.NotImplemented implements LabelApi {

    private final GerritRestClient gerritRestClient;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public LabelApiRestClient(GerritRestClient gerritRestClient, ProjectApiRestClient projectApiRestClient, String name) {
        this.gerritRestClient = gerritRestClient;
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public LabelApi create(LabelDefinitionInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        gerritRestClient.putRequest(labelUrl(), body);
        return this;
    }

    protected String labelUrl() {
        return projectApiRestClient.projectsUrl() + "/labels/" + Url.encode(name);
    }
}
