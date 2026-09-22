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

import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeInput;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;

import java.util.List;

/**
 * @author Urs Wolfer
 */
public class ChangesRestClient extends Changes.NotImplemented implements Changes {

    private final GerritRestContext context;
    private final GerritJson gerritJson;

    public ChangesRestClient(GerritRestContext context) {
        this.context = context;
        this.gerritJson = context.json();
    }

    @Override
    public QueryRequest query() {
        return new QueryRequest() {
            @Override
            public List<ChangeInfo> get() throws RestApiException {
                return ChangesRestClient.this.get(this);
            }
        };
    }

    @Override
    public QueryRequest query(String query) {
        return query().withQuery(query);
    }

    private List<ChangeInfo> get(QueryRequest queryRequest) throws RestApiException {
        String url = UrlQuery.of("/changes/")
            .paramIfNotEmpty("q", queryRequest.getQuery())
            .paramIfPositive("n", queryRequest.getLimit())
            .paramIfPositive("S", queryRequest.getStart())
            // sortkey is for server versions before 2.9, which paged change lists with it
            .paramIfNotEmpty("N", queryRequest.getSortkey())
            .params("o", queryRequest.getOptions())
            .toUrl();

        return context.get(url).asList(ChangeInfo.class);
    }

    @Override
    public ChangeApi id(int id) throws RestApiException {
        return id(Integer.toString(id));
    }

    @Override
    public ChangeApi id(String id) throws RestApiException {
        return new ChangeApiRestClient(context, this, id);
    }

    @Override
    public ChangeApi id(String project, int id) throws RestApiException {
        return id(String.format("%s~%s", Url.encode(project), id));
    }

    @Override
    public ChangeApi id(String project, String branch, String id) throws RestApiException {
        return id(String.format("%s~%s~%s", Url.encode(project), Url.encode(branch), id));
    }

    @Override
    public ChangeApi create(ChangeInput in) throws RestApiException {
        if (in.branch == null) {
            throw new IllegalArgumentException("Branch must be set in change creation input.");
        }

        String url = "/changes/";
        JsonElement result = context.post(url).body(in, ChangeInput.class).asJson();
        ChangeInfo info = gerritJson.as(result, ChangeInfo.class);
        return id(info._number);
    }
}
