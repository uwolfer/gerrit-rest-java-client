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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.*;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;


/**
 * @author Urs Wolfer
 */
public class RevisionApiRestClient extends RevisionApi.NotImplemented implements RevisionApi {

    private final GerritRestContext context;
    private final ChangeApiRestClient changeApiRestClient;
    private final GerritJson gerritJson;
    private final String revision;

    public RevisionApiRestClient(GerritRestContext context,
                                 ChangeApiRestClient changeApiRestClient,
                                 String revision) {
        this.context = context;
        this.gerritJson = context.json();
        this.changeApiRestClient = changeApiRestClient;
        this.revision = revision;
    }

    public String revision() {
        return revision;
    }

    @Override
    public void delete() throws RestApiException {
        context.delete(getRequestPath()).send();
    }

    @Override
    public ReviewResult review(ReviewInput reviewInput) throws RestApiException {
        return context.post(getRequestPath() + "/review").body(reviewInput).as(ReviewResult.class);
    }

    @Override
    public ChangeInfo submit() throws RestApiException {
        return submit(new SubmitInput());
    }

    @Override
    public ChangeInfo submit(SubmitInput submitInput) throws RestApiException {
        return context.post(changeApiRestClient.getRequestPath() + "/submit").body(submitInput).as(ChangeInfo.class);
    }

    @Override
    public void publish() throws RestApiException {
        context.post(getRequestPath() + "/publish").send();
    }

    @Override
    public ChangeApi cherryPick(CherryPickInput in) throws RestApiException {
        context.post(getRequestPath() + "/cherrypick").body(in).send();
        return changeApiRestClient;
    }

    @Override
    public ChangeApi rebase() throws RestApiException {
        return rebase(new RebaseInput());
    }

    @Override
    public ChangeApi rebase(RebaseInput in) throws RestApiException {
        context.post(getRequestPath() + "/rebase").body(in).send();
        return changeApiRestClient;
    }

    @Override
    public void setReviewed(String path, boolean reviewed) throws RestApiException {
        String encodedPath = Url.encode(path);
        String url = String.format("/changes/%s/revisions/%s/files/%s/reviewed", changeApiRestClient.id(), revision, encodedPath);
        if (reviewed) {
            context.put(url).send();
        } else {
            context.delete(url).send();
        }
    }

    @Override
    public MergeableInfo mergeable() throws RestApiException {
        return context.get(getRequestPath() + "/mergeable").as(MergeableInfo.class);
    }

    /**
     * Support starting from Gerrit 2.7.
     */
    @Override
    public SortedMap<String, List<CommentInfo>> comments() throws RestApiException {
        return comments("comments");
    }

    @Override
    public SortedMap<String, List<CommentInfo>> drafts() throws RestApiException {
        return comments("drafts");
    }

    @Override
    public Set<String> reviewed() throws RestApiException {
        String request = getRequestPath() + "/files?reviewed";
        JsonElement jsonElement = context.get(request).asJson();
        return parseReviewedFiles(jsonElement);
    }

    private SortedMap<String, List<CommentInfo>> comments(String type) throws RestApiException {
        return context.get(getRequestPath() + '/' + type + '/').asSortedMapOfLists(CommentInfo.class);
    }

    @Override
    public Map<String, List<RobotCommentInfo>> robotComments() throws RestApiException {
        return context.get(getRequestPath() + "/robotcomments/").asSortedMapOfLists(RobotCommentInfo.class);
    }

    @Override
    public DraftApi createDraft(DraftInput in) throws RestApiException {
        String request = getRequestPath() + "/drafts";
        JsonElement jsonElement = context.put(request).body(in).asJson();
        CommentInfo commentInfo = gerritJson.as(jsonElement.getAsJsonObject(), CommentInfo.class);
        return new DraftApiRestClient(context, changeApiRestClient, this, commentInfo);
    }

    @Override
    public DraftApi draft(String id) throws RestApiException {
        return new DraftApiRestClient(context, changeApiRestClient, this, id);
    }

    @Override
    public CommentApi comment(String id) throws RestApiException {
        return new CommentApiRestClient(context, this, id);
    }

    @Override
    public RobotCommentApi robotComment(String id) throws RestApiException {
        return new RobotCommentApiRestClient(context, this, id);
    }


    @Override
    public Map<String, FileInfo> files() throws RestApiException {
        return files(0);
    }

    @Override
    public Map<String, FileInfo> files(int parentNum) throws RestApiException {
        String request = getRequestPath() + "/files";
        if (parentNum > 0) {
            request += "?parent=" + parentNum;
        }
        return context.get(request).asMap(FileInfo.class);
    }

    @Override
    public FileApi file(String path) {
        return new FileApiRestClient(context, this, path);
    }

    @Override
    public CommitInfo commit(boolean addLinks) throws RestApiException {
        String request = getRequestPath() + "/commit" + (addLinks ? "?links" : "");
        JsonElement jsonElement = context.get(request).asJson();
        return gerritJson.as(jsonElement.getAsJsonObject(), CommitInfo.class);
    }

    @Override
    public BinaryResult patch() throws RestApiException {
        String request = getRequestPath() + "/patch";
        return context.get(request).binary("Failed to get patch.");
    }

    @Override
    public Map<String, ActionInfo> actions() throws RestApiException {
        return context.get(getRequestPath() + "/actions").asSortedMap(ActionInfo.class);
    }

    @Override
    public SubmitType submitType() throws RestApiException {
        return context.get(getRequestPath() + "/submit_type").as(new TypeToken<SubmitType>() {}.getType());
    }

    @Override
    public BinaryResult submitPreview() throws RestApiException {
        return submitPreview(null);
    }

    @Override
    public BinaryResult submitPreview(String format) throws RestApiException {
        String request = getRequestPath() + "/preview_submit";
        if (!Strings.isNullOrEmpty(format)) {
            request += "?format=" + format;
        }

        return context.get(request).binary("Request failed.");
    }

    @Override
    public SubmitType testSubmitType(TestSubmitRuleInput in) throws RestApiException {
        return context.post(getRequestPath() + "/test.submit_type").body(in).as(new TypeToken<SubmitType>() {}.getType());
    }

    @Override
    public String description() throws RestApiException {
        return context.get(getRequestPath() + "/description").asString();
    }

    /**
     * Gerrit emits a trailing comma in this list and Gson turns that into a null element.
     *
     * @see <a href="https://gerrit-review.googlesource.com/Documentation/rest-api-changes.html#list-files">list-files</a>
     * @see <a href="https://github.com/google/gson/issues/494">gson#494</a>
     */
    private Set<String> parseReviewedFiles(JsonElement jsonElement) {
        Set<String> reviewed = gerritJson.asSet(jsonElement, String.class);
        reviewed.remove(null);
        return reviewed;
    }

    protected String getRequestPath() {
        return changeApiRestClient.getRequestPath() + "/revisions/" + revision;
    }
}
