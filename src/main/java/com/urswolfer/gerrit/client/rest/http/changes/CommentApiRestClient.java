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

import com.google.gerrit.extensions.api.changes.CommentApi;
import com.google.gerrit.extensions.api.changes.DeleteCommentInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

public class CommentApiRestClient extends CommentApi.NotImplemented implements CommentApi {

    private final GerritRestClient gerritRestClient;
    private final RevisionApiRestClient revisionApiRestClient;
    private final GerritJson gerritJson;
    private final String id;

    public CommentApiRestClient(GerritRestClient gerritRestClient,
                                GerritJson gerritJson,
                                RevisionApiRestClient revisionApiRestClient,
                                String id) {
        this.gerritRestClient = gerritRestClient;
        this.gerritJson = gerritJson;
        this.revisionApiRestClient = revisionApiRestClient;
        this.id = id;
    }

    @Override
    public CommentInfo get() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath());
        return gerritJson.as(response, CommentInfo.class);
    }

    @Override
    public CommentInfo delete(DeleteCommentInput input) throws RestApiException {
        String body = gerritJson.toJson(input);
        JsonElement response = gerritRestClient.postRequest(getRequestPath() + "/delete", body);
        return gerritJson.as(response, CommentInfo.class);
    }

    protected String getRequestPath() {
        return revisionApiRestClient.getRequestPath() + "/comments/" + id;
    }
}
