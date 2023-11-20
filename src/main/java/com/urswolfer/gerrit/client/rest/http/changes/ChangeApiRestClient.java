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
import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.*;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.*;
import com.urswolfer.gerrit.client.rest.http.config.ServerRestClient;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.*;

/**
 * @author Urs Wolfer
 */
public class ChangeApiRestClient extends ChangeApi.NotImplemented implements ChangeApi {

    private final GerritRestClient gerritRestClient;
    private final ChangesRestClient changesRestClient;
    private final ChangeInfosParser changeInfosParser;
    private final CommentsParser commentsParser;
    private final FileInfoParser fileInfoParser;
    private final ReviewResultParser reviewResultParser;
    private final ReviewerInfosParser reviewerInfosParser;
    private final CommitInfosParser commitInfosParser;
    private final AccountsParser accountsParser;
    private final MergeableInfoParser mergeableInfoParser;
    private final ReviewInfoParser reviewInfoParser;
    private final String id;
    private final ServerRestClient serverRestClient;

    public ChangeApiRestClient(GerritRestClient gerritRestClient,
                               ChangesRestClient changesRestClient,
                               ChangeInfosParser changeInfosParser,
                               CommentsParser commentsParser,
                               FileInfoParser fileInfoParser,
                               ReviewResultParser reviewResultParser,
                               ReviewerInfosParser reviewerInfosParser,
                               CommitInfosParser commitInfosParser,
                               AccountsParser accountsParser,
                               MergeableInfoParser mergeableInfoParser,
                               ReviewInfoParser reviewInfoParser,
                               String id) {
        this.gerritRestClient = gerritRestClient;
        this.changesRestClient = changesRestClient;
        this.changeInfosParser = changeInfosParser;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.reviewResultParser = reviewResultParser;
        this.reviewerInfosParser = reviewerInfosParser;
        this.commitInfosParser = commitInfosParser;
        this.accountsParser = accountsParser;
        this.mergeableInfoParser = mergeableInfoParser;
        this.reviewInfoParser = reviewInfoParser;
        this.id = id;
        this.serverRestClient = new ServerRestClient(gerritRestClient);
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
        return revision(Integer.toString(id));
    }

    @Override
    public RevisionApi revision(String id) throws RestApiException {
        return new RevisionApiRestClient(gerritRestClient, this, commentsParser, fileInfoParser, reviewResultParser, commitInfosParser, mergeableInfoParser, reviewInfoParser, id);
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
        ChangeInfo newChangeInfo = changeInfosParser
            .parseSingleChangeInfo(gerritRestClient.postRequest(request, json));
        return new ChangeApiRestClient(gerritRestClient,
            changesRestClient,
            changeInfosParser,
            commentsParser,
            fileInfoParser,
            reviewResultParser,
            reviewerInfosParser,
            commitInfosParser,
            accountsParser,
            mergeableInfoParser,
            reviewInfoParser,
            newChangeInfo.id);
    }

    @Override
    public RevertSubmissionInfo revertSubmission() throws RestApiException {
        return revertSubmission(new RevertInput());
    }

    @Override
    public RevertSubmissionInfo revertSubmission(RevertInput in) throws RestApiException{
        String request = getRequestPath() + "/revert_submission";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement revertedChanges = gerritRestClient.postRequest(request, json);
        RevertSubmissionInfo revertSubmissionInfo = new RevertSubmissionInfo();
        revertSubmissionInfo.revertChanges = changeInfosParser.parseChangeInfos(revertedChanges);
        return revertSubmissionInfo;
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
    public IncludedInInfo includedIn() throws RestApiException {
        String request = getRequestPath() + "/in";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return changeInfosParser.parseIncludedInInfos(jsonElement);
    }

    @Deprecated
    @Override
    public List<ReviewerInfo> listReviewers() throws RestApiException {
        return reviewers();
    }

    @Override
    public List<ReviewerInfo> reviewers() throws RestApiException {
        String request = getRequestPath() + "/reviewers";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return reviewerInfosParser.parseReviewerInfos(jsonElement);
    }

    @Override
    public AddReviewerResult addReviewer(AddReviewerInput in) throws RestApiException {
        String request = getRequestPath() + "/reviewers";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement reviewerResult = gerritRestClient.postRequest(request, json);
        return reviewerInfosParser.parseAddReviewerResult(reviewerResult);
    }

    @Override
    public AddReviewerResult addReviewer(String in) throws RestApiException {
        AddReviewerInput input = new AddReviewerInput();
        input.reviewer = in;
        return addReviewer(input);
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
        return reviewerInfosParser.parseSuggestReviewerInfos(suggestedReviewers);
    }

    @Override
    public ChangeInfo get(EnumSet<ListChangesOption> options) throws RestApiException {
        String query = "";
        for (ListChangesOption option : options) {
            query = UrlUtils.appendToUrlQuery(query, "o=" + option);
        }
        String url = getRequestPath();
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }
        JsonElement jsonElement = gerritRestClient.getRequest(url);
        return changeInfosParser.parseSingleChangeInfo(jsonElement);
    }

    @Override
    public ChangeInfo get() throws RestApiException {
        return get(ListChangesOptionByVersion.allSupported(serverRestClient.getVersionCached()));
    }

    @Override
    public ChangeInfo info() throws RestApiException {
        return get(EnumSet.noneOf(ListChangesOption.class));
    }

    @Override
    public EditInfo getEdit() throws RestApiException {
        String request = getRequestPath() + "/edit";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return Iterables.getOnlyElement(commitInfosParser.parseEditInfos(jsonElement));
    }

    @Override
    public ChangeEditApi edit() throws RestApiException {
        return new ChangeEditApiRestClient(gerritRestClient, id);
    }

    @Override
    public void setMessage(String message) throws RestApiException {
        CommitMessageInput commitMessageInput = new CommitMessageInput();
        commitMessageInput.message = message;
        setMessage(commitMessageInput);
    }

    @Override
    public void setMessage(CommitMessageInput in) throws RestApiException {
        String request = getRequestPath() + "/message";
        String json = gerritRestClient.getGson().toJson(in);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public void setHashtags(HashtagsInput input) throws RestApiException {
        String request = getRequestPath() + "/hashtags";
        String json = gerritRestClient.getGson().toJson(input);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public Set<String> getHashtags() throws RestApiException {
        String request = getRequestPath() + "/hashtags";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return changeInfosParser.parseHashtags(jsonElement);
    }

    @Override
    public AccountInfo setAssignee(AssigneeInput input) throws RestApiException {
        String request = getRequestPath() + "/assignee";
        String json = gerritRestClient.getGson().toJson(input);
        JsonElement jsonElement= gerritRestClient.putRequest(request, json);
        return accountsParser.parseAccountInfo(jsonElement);
    }

    @Override
    public AccountInfo getAssignee() throws RestApiException {
        String request = getRequestPath() + "/assignee";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return accountsParser.parseAccountInfo(jsonElement);
    }

    @Override
    public List<AccountInfo> getPastAssignees() throws RestApiException {
        String request = getRequestPath() + "/past_assignees";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return accountsParser.parseAccountInfos(jsonElement);
    }

    public AccountInfo deleteAssignee() throws RestApiException {
        String request = getRequestPath() + "/assignee";
        JsonElement jsonElement= gerritRestClient.deleteRequest(request);
        return accountsParser.parseAccountInfo(jsonElement);
    }


    @Override
    public ChangeInfo check() throws RestApiException {
        String request = getRequestPath() + "/check";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return changeInfosParser.parseSingleChangeInfo(jsonElement);
    }

    @Override
    public ChangeInfo check(FixInput in) throws RestApiException {
        String request = getRequestPath() + "/check";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement jsonElement = gerritRestClient.postRequest(request, json);
        return changeInfosParser.parseSingleChangeInfo(jsonElement);
    }

    @Override
    public Map<String, List<CommentInfo>> comments() throws RestApiException {
      String request = getRequestPath() + "/comments";
      JsonElement jsonElement = gerritRestClient.getRequest(request);
      return commentsParser.parseCommentInfos(jsonElement);
    }

    @Override
    public Map<String, List<RobotCommentInfo>> robotComments() throws RestApiException {
        String request = getRequestPath() + "/robotcomments";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseRobotCommentInfos(jsonElement);
    }

    @Override
    public Map<String, List<CommentInfo>> drafts() throws RestApiException {
        String request = getRequestPath() + "/drafts";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseCommentInfos(jsonElement);
    }

    @Override
    public void index() throws RestApiException {
      String request = getRequestPath() + "/index";
      gerritRestClient.postRequest(request);
    }

    @Override
    public List<ChangeInfo> submittedTogether() throws RestApiException {
        String url = getRequestPath() + "/submitted_together";
        JsonElement jsonElement = gerritRestClient.getRequest(url);
        return changeInfosParser.parseChangeInfos(jsonElement);
    }

    @Override
    public List<ChangeMessageInfo> messages() throws RestApiException {
        String request = getRequestPath() + "/messages";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseChangeMessageInfos(jsonElement);
    }

    @Override
    public void ignore(boolean ignore) throws RestApiException {
        String path = ignore ? "/ignore" : "/unignore";
        String request = getRequestPath() + path;
        gerritRestClient.putRequest(request);
    }

    @Override
    public void rebase(RebaseInput in) throws RestApiException {
        String url = getRequestPath() + "/rebase";
        String json = gerritRestClient.getGson().toJson(in);
        gerritRestClient.postRequest(url, json);
    }

    protected String getRequestPath() {
        return "/changes/" + id;
    }
}
