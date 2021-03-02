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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeInput;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.*;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.List;

/**
 * @author Urs Wolfer
 */
public class ChangesRestClient extends Changes.NotImplemented implements Changes {

    private final GerritRestClient gerritRestClient;
    private final ChangeInfosParser changeInfosParser;
    private final CommentsParser commentsParser;
    private final FileInfoParser fileInfoParser;
    private final ReviewerInfosParser reviewerInfosParser;
    private final ReviewResultParser reviewResultParser;
    private final CommitInfosParser commitInfosParser;
    private final AccountsParser accountsParser;
    private final MergeableInfoParser mergeableInfoParser;
    private final ReviewInfoParser reviewInfoParser;

    public ChangesRestClient(GerritRestClient gerritRestClient,
                             ChangeInfosParser changeInfosParser,
                             CommentsParser commentsParser,
                             FileInfoParser fileInfoParser,
                             ReviewerInfosParser reviewerInfosParser,
                             ReviewResultParser reviewResultParser,
                             CommitInfosParser commitInfosParser,
                             AccountsParser accountsParser,
                             MergeableInfoParser mergeableInfoParser,
                             ReviewInfoParser reviewInfoParser) {
        this.gerritRestClient = gerritRestClient;
        this.changeInfosParser = changeInfosParser;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.reviewerInfosParser = reviewerInfosParser;
        this.reviewResultParser = reviewResultParser;
        this.commitInfosParser = commitInfosParser;
        this.accountsParser = accountsParser;
        this.mergeableInfoParser = mergeableInfoParser;
        this.reviewInfoParser = reviewInfoParser;
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
        String query = "";

        if (!Strings.isNullOrEmpty(queryRequest.getQuery())) {
            query = UrlUtils.appendToUrlQuery(query, "q=" + queryRequest.getQuery());
        }
        if (queryRequest.getLimit() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "n=" + queryRequest.getLimit());
        }
        if (queryRequest.getStart() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "S=" + queryRequest.getStart());
        }
        // server version < 2.9, needed for change list paging
        if (!Strings.isNullOrEmpty(queryRequest.getSortkey())) {
            query = UrlUtils.appendToUrlQuery(query, "N=" + queryRequest.getSortkey());
        }
        for (ListChangesOption option : queryRequest.getOptions()) {
            query = UrlUtils.appendToUrlQuery(query, "o=" + option);
        }

        String url = "/changes/";
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }

        JsonElement jsonElement = gerritRestClient.getRequest(url);
        return changeInfosParser.parseChangeInfos(jsonElement);
    }

    @Override
    public ChangeApi id(int id) throws RestApiException {
        return id(Integer.toString(id));
    }

    @Override
    public ChangeApi id(String id) throws RestApiException {
        return new ChangeApiRestClient(gerritRestClient, this, changeInfosParser, commentsParser,
            fileInfoParser, reviewResultParser, reviewerInfosParser, commitInfosParser,
            accountsParser, mergeableInfoParser, reviewInfoParser, id);
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
        String changeInput = changeInfosParser.generateChangeInput(in);
        JsonElement result = gerritRestClient.postRequest(url, changeInput);
        ChangeInfo info = changeInfosParser.parseSingleChangeInfo(result);
        return id(info._number);
    }
}
