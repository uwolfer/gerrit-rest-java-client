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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.DraftApi;
import com.google.gerrit.extensions.api.changes.DraftInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

/**
 * @author Urs Wolfer
 */
public class DraftApiRestClient extends DraftApi.NotImplemented implements DraftApi {

    private final GerritRestClient gerritRestClient;
    private final ChangeApiRestClient changeApiRestClient;
    private final RevisionApiRestClient revisionApiRestClient;
    private final GerritJson gerritJson;
    private final CommentInfo commentInfo;
    private final String id;

    public DraftApiRestClient(GerritRestContext context,
                              ChangeApiRestClient changeApiRestClient,
                              RevisionApiRestClient revisionApiRestClient,
                              CommentInfo commentInfo) {
        this.gerritRestClient = context.restClient();
        this.gerritJson = context.json();
        this.changeApiRestClient = changeApiRestClient;
        this.revisionApiRestClient = revisionApiRestClient;
        this.commentInfo = commentInfo;
        this.id = null;
    }

    public DraftApiRestClient(GerritRestContext context,
                              ChangeApiRestClient changeApiRestClient,
                              RevisionApiRestClient revisionApiRestClient,
                              String id) {
        this.gerritRestClient = context.restClient();
        this.gerritJson = context.json();
        this.changeApiRestClient = changeApiRestClient;
        this.revisionApiRestClient = revisionApiRestClient;
        this.id = id;
        this.commentInfo = null;
    }

    @Override
    public CommentInfo update(DraftInput in) throws RestApiException {
        String json = gerritJson.toJson(in);
        JsonElement jsonElement = gerritRestClient.putRequest(getUrl(), json);
        return gerritJson.as(jsonElement.getAsJsonObject(), CommentInfo.class);
    }

    @Override
    public void delete() throws RestApiException {
        gerritRestClient.deleteRequest(getUrl());
    }

    @Override
    public CommentInfo get() throws RestApiException {
        if (commentInfo != null) {
            return commentInfo;
        }
        JsonElement jsonElement = gerritRestClient.getRequest(getUrl());
        return gerritJson.as(jsonElement.getAsJsonObject(), CommentInfo.class);
    }

    private String getUrl() {
        return "/changes/" + changeApiRestClient.id() + "/revisions/" + revisionApiRestClient.revision()
                + "/drafts/" + (commentInfo != null ? commentInfo.id : id);
    }
}
