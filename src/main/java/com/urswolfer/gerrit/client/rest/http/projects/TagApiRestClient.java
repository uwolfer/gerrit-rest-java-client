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
import com.google.gerrit.extensions.api.projects.*;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

/**
 * @author Pavel Bely
 */
public class TagApiRestClient extends TagApi.NotImplemented implements TagApi {
    private final GerritRestContext context;
    private final GerritJson gerritJson;
    private final ProjectApiRestClient projectApiRestClient;
    private final String name;

    public TagApiRestClient(GerritRestContext context,
                            ProjectApiRestClient projectApiRestClient,
                            String name) {
        this.context = context;
        this.gerritJson = context.json();
        this.projectApiRestClient = projectApiRestClient;
        this.name = name;
    }

    @Override
    public TagApi create(TagInput in) throws RestApiException {
        context.put(tagUrl()).body(in).send();
        return this;
    }

    @Override
    public TagInfo get() throws RestApiException {
        JsonElement jsonElement = context.get(tagUrl()).asJson();
        return Iterables.getOnlyElement(gerritJson.asList(jsonElement, TagInfo.class));
    }

    @Override
    public void delete() throws RestApiException{
        context.delete(tagUrl()).send();
    }

    protected String tagUrl() {
        return projectApiRestClient.projectsUrl() + "/tags/" + name;
    }
}
