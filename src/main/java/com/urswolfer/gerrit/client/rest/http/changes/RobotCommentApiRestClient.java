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

import com.google.gerrit.extensions.api.changes.RobotCommentApi;
import com.google.gerrit.extensions.common.RobotCommentInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

public class RobotCommentApiRestClient extends RobotCommentApi.NotImplemented implements RobotCommentApi {

    private final GerritRestClient gerritRestClient;
    private final RevisionApiRestClient revisionApiRestClient;
    private final GerritJson gerritJson;
    private final String id;

    public RobotCommentApiRestClient(GerritRestContext context,
                                     RevisionApiRestClient revisionApiRestClient,
                                     String id) {
        this.gerritRestClient = context.restClient();
        this.gerritJson = context.json();
        this.revisionApiRestClient = revisionApiRestClient;
        this.id = id;
    }

    @Override
    public RobotCommentInfo get() throws RestApiException {
        JsonElement response = gerritRestClient.getRequest(getRequestPath());
        return gerritJson.as(response, RobotCommentInfo.class);
    }

    protected String getRequestPath() {
        return revisionApiRestClient.getRequestPath() + "/robotcomments/" + id;
    }
}
