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

import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.*;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;

import java.util.*;

/**
 * @author Urs Wolfer
 */
public class ChangeApiRestClient extends ChangeApi.NotImplemented implements ChangeApi {

    private static final String REVIEWERS = "/reviewers";
    private static final String ASSIGNEE = "/assignee";

    private final GerritRestContext context;
    private final ChangesRestClient changesRestClient;
    private final GerritJson gerritJson;
    private final String id;

    public ChangeApiRestClient(GerritRestContext context,
                               ChangesRestClient changesRestClient,
                               String id) {
        this.context = context;
        this.gerritJson = context.json();
        this.changesRestClient = changesRestClient;
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
        return revision(Integer.toString(id));
    }

    @Override
    public RevisionApi revision(String id) throws RestApiException {
        return new RevisionApiRestClient(context, this, id);
    }

    @Override
    public void abandon() throws RestApiException {
        abandon(new AbandonInput());
    }

    @Override
    public void abandon(AbandonInput abandonInput) throws RestApiException {
        context.post(getRequestPath() + "/abandon").body(abandonInput).send();
    }

    @Override
    public void restore() throws RestApiException {
        restore(new RestoreInput());
    }

    @Override
    public void restore(RestoreInput restoreInput) throws RestApiException {
        context.post(getRequestPath() + "/restore").body(restoreInput).send();
    }

    @Override
    public void move(String destinationBranch) throws RestApiException {
        MoveInput moveInput = new MoveInput();
        moveInput.destinationBranch = destinationBranch;
        move(moveInput);
    }

    @Override
    public void move(MoveInput moveInput) throws RestApiException {
        context.post(getRequestPath() + "/move").body(moveInput).send();
    }

    @Override
    public ChangeApi revert() throws RestApiException {
        return revert(new RevertInput());
    }

    @Override
    public ChangeApi revert(RevertInput revertInput) throws RestApiException {
        ChangeInfo newChangeInfo = context.post(getRequestPath() + "/revert")
            .body(revertInput)
            .as(ChangeInfo.class);
        return new ChangeApiRestClient(context, changesRestClient, newChangeInfo.id);
    }

    @Override
    public RevertSubmissionInfo revertSubmission() throws RestApiException {
        return revertSubmission(new RevertInput());
    }

    @Override
    public RevertSubmissionInfo revertSubmission(RevertInput in) throws RestApiException{
        String request = getRequestPath() + "/revert_submission";
        JsonElement revertedChanges = context.post(request).body(in).asJson();
        RevertSubmissionInfo revertSubmissionInfo = new RevertSubmissionInfo();
        revertSubmissionInfo.revertChanges = gerritJson.asList(revertedChanges, ChangeInfo.class);
        return revertSubmissionInfo;
    }

    @Override
    public ChangeInfo createMergePatchSet(MergePatchSetInput in) throws RestApiException {
        return context.post(getRequestPath() + "/merge").body(in).as(ChangeInfo.class);
    }

    @Override
    public void publish() throws RestApiException {
        context.post(getRequestPath() + "/publish").send();
    }

    @Override
    public void delete() throws RestApiException {
        context.delete(getRequestPath()).send();
    }

    @Override
    public String topic() throws RestApiException {
        return context.get(getRequestPath() + "/topic").asString();
    }

    @Override
    public void topic(String topic) throws RestApiException {
        String request = getRequestPath() + "/topic";
        Map<String, String> topicInput = Collections.singletonMap("topic", topic);
        context.put(request).body(topicInput).send();
    }

    @Override
    public IncludedInInfo includedIn() throws RestApiException {
        return context.get(getRequestPath() + "/in").as(IncludedInInfo.class);
    }

    /** @deprecated use {@link #reviewers()} instead. */
    @Deprecated
    @Override
    public List<ReviewerInfo> listReviewers() throws RestApiException {
        return reviewers();
    }

    @Override
    public List<ReviewerInfo> reviewers() throws RestApiException {
        return context.get(getRequestPath() + REVIEWERS).asList(ReviewerInfo.class);
    }

    @Override
    public AddReviewerResult addReviewer(AddReviewerInput in) throws RestApiException {
        return context.post(getRequestPath() + REVIEWERS).body(in).as(AddReviewerResult.class);
    }

    @Override
    public ReviewerResult addReviewer(ReviewerInput in) throws RestApiException {
        return context.post(getRequestPath() + REVIEWERS).body(in).as(ReviewerResult.class);
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
        return context.get(getRequestPath() + String.format("/suggest_reviewers?%s", queryPart)).asList(SuggestedReviewerInfo.class);
    }

    @Override
    public ChangeInfo get(EnumSet<ListChangesOption> options) throws RestApiException {
        String url = UrlQuery.of(getRequestPath()).params("o", options).toUrl();
        return context.get(url).as(ChangeInfo.class);
    }

    @Override
    public ChangeInfo get() throws RestApiException {
        return get(ListChangesOptionByVersion.allSupported(context.serverVersion()));
    }

    @Override
    public ChangeInfo info() throws RestApiException {
        return get(EnumSet.noneOf(ListChangesOption.class));
    }

    @Override
    public EditInfo getEdit() throws RestApiException {
        String request = getRequestPath() + "/edit";
        JsonElement jsonElement = context.get(request).asJson();
        return Iterables.getOnlyElement(gerritJson.asList(jsonElement, EditInfo.class));
    }

    @Override
    public ChangeEditApi edit() throws RestApiException {
        return new ChangeEditApiRestClient(context, id);
    }

    @Override
    public void setMessage(String message) throws RestApiException {
        CommitMessageInput commitMessageInput = new CommitMessageInput();
        commitMessageInput.message = message;
        setMessage(commitMessageInput);
    }

    @Override
    public void setMessage(CommitMessageInput in) throws RestApiException {
        context.post(getRequestPath() + "/message").body(in).send();
    }

    @Override
    public void setHashtags(HashtagsInput input) throws RestApiException {
        context.post(getRequestPath() + "/hashtags").body(input).send();
    }

    @Override
    public Set<String> getHashtags() throws RestApiException {
        return context.get(getRequestPath() + "/hashtags").asSet(String.class);
    }

    @Override
    public AccountInfo setAssignee(AssigneeInput input) throws RestApiException {
        return context.put(getRequestPath() + ASSIGNEE).body(input).as(AccountInfo.class);
    }

    @Override
    public AccountInfo getAssignee() throws RestApiException {
        return context.get(getRequestPath() + ASSIGNEE).as(AccountInfo.class);
    }

    @Override
    public List<AccountInfo> getPastAssignees() throws RestApiException {
        return context.get(getRequestPath() + "/past_assignees").asList(AccountInfo.class);
    }

    @Override
    public AccountInfo deleteAssignee() throws RestApiException {
        String request = getRequestPath() + ASSIGNEE;
        JsonElement jsonElement= context.delete(request).asJson();
        return gerritJson.as(jsonElement, AccountInfo.class);
    }


    @Override
    public ChangeInfo check() throws RestApiException {
        return context.get(getRequestPath() + "/check").as(ChangeInfo.class);
    }

    @Override
    public ChangeInfo check(FixInput in) throws RestApiException {
        return context.post(getRequestPath() + "/check").body(in).as(ChangeInfo.class);
    }

    @Override
    public Map<String, List<CommentInfo>> comments() throws RestApiException {
      return context.get(getRequestPath() + "/comments").asSortedMapOfLists(CommentInfo.class);
    }

    @Override
    public Map<String, List<RobotCommentInfo>> robotComments() throws RestApiException {
        return context.get(getRequestPath() + "/robotcomments").asSortedMapOfLists(RobotCommentInfo.class);
    }

    @Override
    public Map<String, List<CommentInfo>> drafts() throws RestApiException {
        return context.get(getRequestPath() + "/drafts").asSortedMapOfLists(CommentInfo.class);
    }

    @Override
    public void index() throws RestApiException {
      context.post(getRequestPath() + "/index").send();
    }

    @Override
    public List<ChangeInfo> submittedTogether() throws RestApiException {
        return context.get(getRequestPath() + "/submitted_together").asList(ChangeInfo.class);
    }

    @Override
    public List<ChangeMessageInfo> messages() throws RestApiException {
        return context.get(getRequestPath() + "/messages").asList(ChangeMessageInfo.class);
    }

    @Override
    public void ignore(boolean ignore) throws RestApiException {
        String path = ignore ? "/ignore" : "/unignore";
        context.put(getRequestPath() + path).send();
    }

    @Override
    public void rebase(RebaseInput in) throws RestApiException {
        context.post(getRequestPath() + "/rebase").body(in).send();
    }

    protected String getRequestPath() {
        return "/changes/" + id;
    }
}
