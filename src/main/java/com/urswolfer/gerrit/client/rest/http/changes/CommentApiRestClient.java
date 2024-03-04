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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.CommentApi;
import com.google.gerrit.extensions.api.changes.DeleteCommentInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.CommentsParser;

public class CommentApiRestClient extends CommentApi.NotImplemented implements CommentApi {

    private final GerritRestClient gerritRestClient;
    private final RevisionApiRestClient revisionApiRestClient;
    private final CommentsParser commentsParser;
    private final String id;

    public CommentApiRestClient(GerritRestClient gerritRestClient,
                              RevisionApiRestClient revisionApiRestClient,
                              CommentsParser commentsParser,
                              String id) {
        this.gerritRestClient = gerritRestClient;
        this.revisionApiRestClient = revisionApiRestClient;
        this.commentsParser = commentsParser;
        this.id = id;
    }

    @Override
    public CommentInfo get() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath());
        return commentsParser.parseSingleCommentInfo(response);
    }

    @Override
    public CommentInfo delete(DeleteCommentInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement response = gerritRestClient.postRequest(getRequestPath() + "/delete", body);
        return commentsParser.parseSingleCommentInfo(response);
    }

    protected String getRequestPath() {
        return revisionApiRestClient.getRequestPath() + "/comments/" + id;
    }
}
