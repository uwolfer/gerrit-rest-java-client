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
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static com.urswolfer.gerrit.client.rest.RestClient.HttpVerb.GET;

/**
 * @author Urs Wolfer
 */
public class RevisionApiRestClient extends RevisionApi.NotImplemented implements RevisionApi {

    private final GerritRestContext context;
    private final GerritRestClient gerritRestClient;
    private final ChangeApiRestClient changeApiRestClient;
    private final GerritJson gerritJson;
    private final String revision;

    public RevisionApiRestClient(GerritRestContext context,
                                 ChangeApiRestClient changeApiRestClient,
                                 String revision) {
        this.context = context;
        this.gerritRestClient = context.restClient();
        this.gerritJson = context.json();
        this.changeApiRestClient = changeApiRestClient;
        this.revision = revision;
    }

    public String revision() {
        return revision;
    }

    @Override
    public void delete() throws RestApiException {
        String request = getRequestPath();
        gerritRestClient.deleteRequest(request);
    }

    @Override
    public ReviewResult review(ReviewInput reviewInput) throws RestApiException {
        String request = getRequestPath() + "/review";
        String json = gerritJson.toJson(reviewInput);
        JsonElement reviewResult = gerritRestClient.postRequest(request, json);
        return gerritJson.as(reviewResult, ReviewResult.class);
    }

    @Override
    public ChangeInfo submit() throws RestApiException {
        return submit(new SubmitInput());
    }

    @Override
    public ChangeInfo submit(SubmitInput submitInput) throws RestApiException {
        String request = changeApiRestClient.getRequestPath() + "/submit";
        String json = gerritJson.toJson(submitInput);
        JsonElement result = gerritRestClient.postRequest(request, json);
        return gerritJson.as(result, ChangeInfo.class);
    }

    @Override
    public void publish() throws RestApiException {
        String request = getRequestPath() + "/publish";
        gerritRestClient.postRequest(request);
    }

    @Override
    public ChangeApi cherryPick(CherryPickInput in) throws RestApiException {
        String request = getRequestPath() + "/cherrypick";
        String json = gerritJson.toJson(in);
        gerritRestClient.postRequest(request, json);
        return changeApiRestClient;
    }

    @Override
    public ChangeApi rebase() throws RestApiException {
        return rebase(new RebaseInput());
    }

    @Override
    public ChangeApi rebase(RebaseInput in) throws RestApiException {
        String request = getRequestPath() + "/rebase";
        String json = gerritJson.toJson(in);
        gerritRestClient.postRequest(request, json);
        return changeApiRestClient;
    }

    @Override
    public void setReviewed(String path, boolean reviewed) throws RestApiException {
        String encodedPath = Url.encode(path);
        String url = String.format("/changes/%s/revisions/%s/files/%s/reviewed", changeApiRestClient.id(), revision, encodedPath);
        if (reviewed) {
            gerritRestClient.putRequest(url);
        } else {
            gerritRestClient.deleteRequest(url);
        }
    }

    @Override
    public MergeableInfo mergeable() throws RestApiException {
        String request = getRequestPath() + "/mergeable";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.as(jsonElement, MergeableInfo.class);
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
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return parseReviewedFiles(jsonElement);
    }

    private SortedMap<String, List<CommentInfo>> comments(String type) throws RestApiException {
        String request = getRequestPath() + '/' + type + '/';
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.asSortedMapOfLists(jsonElement, CommentInfo.class);
    }

    @Override
    public Map<String, List<RobotCommentInfo>> robotComments() throws RestApiException {
        String request = getRequestPath() + "/robotcomments/";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.asSortedMapOfLists(jsonElement, RobotCommentInfo.class);
    }

    @Override
    public DraftApi createDraft(DraftInput in) throws RestApiException {
        String request = getRequestPath() + "/drafts";
        String json = gerritJson.toJson(in);
        JsonElement jsonElement = gerritRestClient.putRequest(request, json);
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
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.asMap(jsonElement, FileInfo.class);
    }

    @Override
    public FileApi file(String path) {
        return new FileApiRestClient(context, this, path);
    }

    @Override
    public CommitInfo commit(boolean addLinks) throws RestApiException {
        String request = getRequestPath() + "/commit" + (addLinks ? "?links" : "");
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.as(jsonElement.getAsJsonObject(), CommitInfo.class);
    }

    @Override
    public BinaryResult patch() throws RestApiException {
        String request = getRequestPath() + "/patch";
        try {
            HttpResponse response = gerritRestClient.request(request, null, GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to get patch.", e);
        }
    }

    @Override
    public Map<String, ActionInfo> actions() throws RestApiException {
        String request = getRequestPath() + "/actions";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.asSortedMap(jsonElement, ActionInfo.class);
    }

    @Override
    public SubmitType submitType() throws RestApiException {
        String request = getRequestPath() + "/submit_type";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritJson.as(jsonElement, new TypeToken<SubmitType>() {}.getType());
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

        try {
            HttpResponse response = gerritRestClient.request(request, null, GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw RestApiException.wrap("Request failed.", e);
        }
    }

    @Override
    public SubmitType testSubmitType(TestSubmitRuleInput in) throws RestApiException {
        String request = getRequestPath() + "/test.submit_type";
        String json = gerritJson.toJson(in);
        JsonElement jsonElement = gerritRestClient.postRequest(request,json);
        return gerritJson.as(jsonElement, new TypeToken<SubmitType>() {}.getType());
    }

    @Override
    public String description() throws RestApiException {
        String request = getRequestPath() + "/description";
        return gerritRestClient.getRequest(request).getAsString();
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
