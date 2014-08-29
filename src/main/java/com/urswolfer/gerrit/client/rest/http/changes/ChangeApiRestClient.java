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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.changes.AbandonInput;
import com.google.gerrit.extensions.api.changes.AddReviewerInput;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.RevisionApi;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ListChangesOption;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

import java.util.EnumSet;

/**
 * @author Urs Wolfer
 */
public class ChangeApiRestClient extends ChangeApi.NotImplemented implements ChangeApi {

    private final GerritRestClient gerritRestClient;
    private final ChangesRestClient changesRestClient;
    private final CommentsParser commentsParser;
    private final FileInfoParser fileInfoParser;
    private final DiffInfoParser diffInfoParser;
    private final String id;

    public ChangeApiRestClient(GerritRestClient gerritRestClient,
                               ChangesRestClient changesRestClient,
                               CommentsParser commentsParser,
                               FileInfoParser fileInfoParser,
                               DiffInfoParser diffInfoParser,
                               String triplet) {
        this.gerritRestClient = gerritRestClient;
        this.changesRestClient = changesRestClient;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.diffInfoParser = diffInfoParser;
        this.id = triplet;
    }

    public ChangeApiRestClient(GerritRestClient gerritRestClient,
                               ChangesRestClient changesRestClient,
                               CommentsParser commentsParser,
                               FileInfoParser fileInfoParser,
                               DiffInfoParser diffInfoParser,
                               int id) {
        this.changesRestClient = changesRestClient;
        this.gerritRestClient = gerritRestClient;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.diffInfoParser = diffInfoParser;
        this.id = "" + id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RevisionApi current() throws RestApiException {
        return new RevisionApiRestClient(gerritRestClient, this, commentsParser, fileInfoParser, diffInfoParser, "current");
    }

    @Override
    public RevisionApi revision(int id) throws RestApiException {
        return new RevisionApiRestClient(gerritRestClient, this, commentsParser, fileInfoParser, diffInfoParser, "" + id);
    }

    @Override
    public RevisionApi revision(String id) throws RestApiException {
        return new RevisionApiRestClient(gerritRestClient, this, commentsParser, fileInfoParser, diffInfoParser, id);
    }

    @Override
    public void abandon() throws RestApiException {
        abandon(new AbandonInput());
    }

    @Override
    public void abandon(AbandonInput abandonInput) throws RestApiException {
        String request = getRequestPath() + "/abandon";
        String json = gerritRestClient.getGson().toJson(abandonInput);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public void addReviewer(AddReviewerInput in) throws RestApiException {
        String request = getRequestPath() + "/reviewers";
        String json = gerritRestClient.getGson().toJson(in);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public void addReviewer(String in) throws RestApiException {
        AddReviewerInput input = new AddReviewerInput();
        input.reviewer = in;
        addReviewer(input);
    }

    @Override
    public ChangeInfo get(EnumSet<ListChangesOption> options) throws RestApiException {
        return Iterables.getFirst(changesRestClient.query(id).withOptions(options).get(), null);
    }

    @Override
    public ChangeInfo get() throws RestApiException {
        return get(EnumSet.allOf(ListChangesOption.class));
    }

    @Override
    public ChangeInfo info() throws RestApiException {
        return get(EnumSet.noneOf(ListChangesOption.class));
    }

    protected String getRequestPath() {
        return "/changes/" + id;
    }
}
