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
import com.google.gerrit.extensions.api.changes.FixInput;
import com.google.gerrit.extensions.api.changes.RestoreInput;
import com.google.gerrit.extensions.api.changes.MoveInput;
import com.google.gerrit.extensions.api.changes.RevertInput;
import com.google.gerrit.extensions.api.changes.RevisionApi;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.ReviewerInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gerrit.extensions.common.EditInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * @author Urs Wolfer
 */
public class ChangeApiRestClient extends ChangeApi.NotImplemented implements ChangeApi {

    private final GerritRestClient gerritRestClient;
    private final ChangesRestClient changesRestClient;
    private final ChangesParser changesParser;
    private final CommentsParser commentsParser;
    private final FileInfoParser fileInfoParser;
    private final DiffInfoParser diffInfoParser;
    private final SuggestedReviewerInfoParser suggestedReviewerInfoParser;
    private final ReviewerInfoParser reviewerInfoParser;
    private final EditInfoParser editInfoParser;
    private final String id;

    public ChangeApiRestClient(GerritRestClient gerritRestClient,
                               ChangesRestClient changesRestClient,
                               ChangesParser changesParser,
                               CommentsParser commentsParser,
                               FileInfoParser fileInfoParser,
                               DiffInfoParser diffInfoParser,
                               SuggestedReviewerInfoParser suggestedReviewerInfoParser,
                               ReviewerInfoParser reviewerInfoParser,
                               EditInfoParser editInfoParser,
                               String id) {
        this.gerritRestClient = gerritRestClient;
        this.changesRestClient = changesRestClient;
        this.changesParser = changesParser;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.diffInfoParser = diffInfoParser;
        this.suggestedReviewerInfoParser = suggestedReviewerInfoParser;
        this.reviewerInfoParser = reviewerInfoParser;
        this.editInfoParser = editInfoParser;
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public RevisionApi current() throws RestApiException {
        return revision("current");
    }

    @Override
    public RevisionApi revision(int id) throws RestApiException {
        return revision("" + id);
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
    public void restore() throws RestApiException {
        restore(new RestoreInput());
    }

    @Override
    public void restore(RestoreInput restoreInput) throws RestApiException {
        String request = getRequestPath() + "/restore";
        String json = gerritRestClient.getGson().toJson(restoreInput);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public void move(String destinationBranch) throws RestApiException {
        MoveInput moveInput = new MoveInput();
        moveInput.destinationBranch = destinationBranch;
        move(moveInput);
    }

    @Override
    public void move(MoveInput moveInput) throws RestApiException {
        String request = getRequestPath() + "/move";
        String json = gerritRestClient.getGson().toJson(moveInput);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public ChangeApi revert() throws RestApiException {
        return revert(new RevertInput());
    }

    @Override
    public ChangeApi revert(RevertInput revertInput) throws RestApiException {
        String request = getRequestPath() + "/revert";
        String json = gerritRestClient.getGson().toJson(revertInput);
        gerritRestClient.postRequest(request, json);
        return new ChangeApiRestClient(gerritRestClient,
            changesRestClient,
            changesParser,
            commentsParser,
            fileInfoParser,
            diffInfoParser,
            suggestedReviewerInfoParser,
            reviewerInfoParser,
            editInfoParser,
            id);
    }

    @Override
    public void publish() throws RestApiException {
        String request = getRequestPath() + "/publish";
        gerritRestClient.postRequest(request);
    }

    @Override
    public void delete() throws RestApiException {
        String request = getRequestPath();
        gerritRestClient.deleteRequest(request);
    }

    @Override
    public String topic() throws RestApiException {
        String request = getRequestPath() + "/topic";
        return gerritRestClient.getRequest(request).getAsString();
    }

    @Override
    public void topic(String topic) throws RestApiException {
        String request = getRequestPath() + "/topic";
        Map<String, String> topicInput = Collections.singletonMap("topic", topic);
        String json = gerritRestClient.getGson().toJson(topicInput);
        gerritRestClient.putRequest(request, json);
    }

    @Override
    public List<ReviewerInfo> listReviewers() throws RestApiException {
        String request = getRequestPath() + "/reviewers";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return reviewerInfoParser.parseReviewerInfos(jsonElement);
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
    public SuggestedReviewersRequest suggestReviewers() throws RestApiException {
        return new SuggestedReviewersRequest() {
            @Override
            public List<SuggestedReviewerInfo> get() throws RestApiException {
                return ChangeApiRestClient.this.suggestReviewers(this);
            }
        };
    }

    @Override
    public SuggestedReviewersRequest suggestReviewers(String query) throws RestApiException {
        return suggestReviewers().withQuery(query).withLimit(-1); // a limit must be added because of a Gerrit bug; see: https://gerrit-review.googlesource.com/#/c/60242/
    }

    private List<SuggestedReviewerInfo> suggestReviewers(SuggestedReviewersRequest r) throws RestApiException {
        String encodedQuery = Url.encode(r.getQuery());
        return getSuggestedReviewers(String.format("q=%s&n=%s", encodedQuery, r.getLimit()));
    }

    private List<SuggestedReviewerInfo> getSuggestedReviewers(String queryPart) throws RestApiException {
        String request = getRequestPath() + String.format("/suggest_reviewers?%s", queryPart);
        JsonElement suggestedReviewers = gerritRestClient.getRequest(request);
        return suggestedReviewerInfoParser.parseSuggestReviewerInfos(suggestedReviewers);
    }

    @Override
    public ChangeInfo get(EnumSet<ListChangesOption> options) throws RestApiException {
        return Iterables.getOnlyElement(changesRestClient.query(id).withOptions(options).get());
    }

    @Override
    public ChangeInfo get() throws RestApiException {
        return get(EnumSet.allOf(ListChangesOption.class));
    }

    @Override
    public ChangeInfo info() throws RestApiException {
        return get(EnumSet.noneOf(ListChangesOption.class));
    }

    @Override
    public EditInfo getEdit() throws RestApiException {
        String request = getRequestPath() + "/edit";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return Iterables.getOnlyElement(editInfoParser.parseEditInfos(jsonElement));
    }

    @Override
    public ChangeInfo check() throws RestApiException {
        String request = getRequestPath() + "/check";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return Iterables.getOnlyElement(changesParser.parseChangeInfos(jsonElement));
    }

    @Override
    public ChangeInfo check(FixInput in) throws RestApiException {
        String request = getRequestPath() + "/check";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement jsonElement = gerritRestClient.postRequest(request, json);
        return Iterables.getOnlyElement(changesParser.parseChangeInfos(jsonElement));
    }

    @Override
    public Map<String, List<CommentInfo>> comments() throws RestApiException {
      String request = getRequestPath() + "/comments";
      JsonElement jsonElement = gerritRestClient.getRequest(request);
      return commentsParser.parseCommentInfos(jsonElement);
    }

    @Override
    public void index() throws RestApiException {
      String request = getRequestPath() + "/index";
      gerritRestClient.postRequest(request);
    }

    protected String getRequestPath() {
        return "/changes/" + id;
    }
}
