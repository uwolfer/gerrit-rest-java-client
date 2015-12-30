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

import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.projects.TagApi;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

/**
 * @author Pavel Bely
 */
public class TagApiRestClient extends TagApi.NotImplemented implements TagApi {
    private final GerritRestClient gerritRestClient;
    private final TagInfoParser tagInfoParser;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public TagApiRestClient(GerritRestClient gerritRestClient,
                            TagInfoParser tagInfoParser,
                            ProjectApiRestClient projectApiRestClient,
                            String name) {
        this.gerritRestClient = gerritRestClient;
        this.tagInfoParser = tagInfoParser;
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public TagInfo get() throws RestApiException {
        JsonElement jsonElement = gerritRestClient.getRequest(tagUrl());
        return Iterables.getOnlyElement(tagInfoParser.parseTagInfos(jsonElement));
    }

    protected String tagUrl() {
        return projectApiRestClient.projectsUrl() + "/tags/" + name;
    }
}
