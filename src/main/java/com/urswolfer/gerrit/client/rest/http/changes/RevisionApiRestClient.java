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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.*;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Urs Wolfer
 */
public class RevisionApiRestClient extends RevisionApi.NotImplemented implements RevisionApi {

    private final GerritRestClient gerritRestClient;
    private final ChangeApiRestClient changeApiRestClient;
    private final CommentsParser commentsParser;
    private final FileInfoParser fileInfoParser;
    private final DiffInfoParser diffInfoParser;
    private final ReviewResultParser reviewResultParser;
    private final CommitInfoParser commitInfoParser;
    private final MergeableInfoParser mergeableInfoParser;
    private final ActionInfoParser actionInfoParser;
    private final ReviewInfoParser reviewInfoParser;
    private final String revision;

    public RevisionApiRestClient(GerritRestClient gerritRestClient,
                                 ChangeApiRestClient changeApiRestClient,
                                 CommentsParser commentsParser,
                                 FileInfoParser fileInfoParser,
                                 DiffInfoParser diffInfoParser,
                                 ReviewResultParser reviewResultParser,
                                 CommitInfoParser commitInfoParser,
                                 MergeableInfoParser mergeableInfoParser,
                                 ActionInfoParser actionInfoParser,
                                 ReviewInfoParser reviewInfoParser,
                                 String revision) {
        this.gerritRestClient = gerritRestClient;
        this.changeApiRestClient = changeApiRestClient;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.diffInfoParser = diffInfoParser;
        this.reviewResultParser = reviewResultParser;
        this.commitInfoParser = commitInfoParser;
        this.mergeableInfoParser = mergeableInfoParser;
        this.actionInfoParser = actionInfoParser;
        this.reviewInfoParser = reviewInfoParser;
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
        String json = gerritRestClient.getGson().toJson(reviewInput);
        JsonElement reviewResult = gerritRestClient.postRequest(request, json);
        return reviewResultParser.parseReviewResult(reviewResult);
    }

    @Override
    public void submit() throws RestApiException {
        submit(new SubmitInput());
    }

    @Override
    public void submit(SubmitInput submitInput) throws RestApiException {
        String request = changeApiRestClient.getRequestPath() + "/submit";
        String json = gerritRestClient.getGson().toJson(submitInput);
        gerritRestClient.postRequest(request, json);
    }

    @Override
    public void publish() throws RestApiException {
        String request = getRequestPath() + "/publish";
        gerritRestClient.postRequest(request);
    }

    @Override
    public ChangeApi cherryPick(CherryPickInput in) throws RestApiException {
        String request = getRequestPath() + "/cherrypick";
        String json = gerritRestClient.getGson().toJson(in);
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
        String json = gerritRestClient.getGson().toJson(in);
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
        return mergeableInfoParser.parseMergeableInfo(jsonElement);
    }

    /**
     * Support starting from Gerrit 2.7.
     */
    @Override
    public TreeMap<String, List<CommentInfo>> comments() throws RestApiException {
        return comments("comments");
    }

    @Override
    public TreeMap<String, List<CommentInfo>> drafts() throws RestApiException {
        return comments("drafts");
    }

    @Override
    public Set<String> reviewed() throws RestApiException {
        String request = getRequestPath() + "/files?reviewed";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return reviewInfoParser.parseFileInfos(jsonElement);
    }

    private TreeMap<String, List<CommentInfo>> comments(String type) throws RestApiException {
        String request = getRequestPath() + '/' + type + '/';
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseCommentInfos(jsonElement);
    }

    @Override
    public Map<String, List<RobotCommentInfo>> robotComments() throws RestApiException {
        String request = getRequestPath() + "/robotcomments/";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseRobotCommentInfos(jsonElement);
    }

    @Override
    public DraftApi createDraft(DraftInput in) throws RestApiException {
        String request = getRequestPath() + "/drafts";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement jsonElement = gerritRestClient.putRequest(request, json);
        CommentInfo commentInfo = commentsParser.parseSingleCommentInfo(jsonElement.getAsJsonObject());
        return new DraftApiRestClient(gerritRestClient, changeApiRestClient, this, commentsParser, commentInfo);
    }

    @Override
    public DraftApi draft(String id) throws RestApiException {
        return new DraftApiRestClient(gerritRestClient, changeApiRestClient, this, commentsParser, id);
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
        return fileInfoParser.parseFileInfos(jsonElement);
    }

    @Override
    public FileApi file(String path) {
        return new FileApiRestClient(gerritRestClient, this, diffInfoParser, path);
    }

    @Override
    public CommitInfo commit(boolean addLinks) throws RestApiException {
        String request = getRequestPath() + "/commit" + (addLinks ? "?links" : "");
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commitInfoParser.parseSingleCommentInfo(jsonElement.getAsJsonObject());
    }

    @Override
    public BinaryResult patch() throws RestApiException {
        String request = getRequestPath() + "/patch";
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw RestApiException.wrap("Failed to get patch.", e);
        }
    }

    @Override
    public Map<String, ActionInfo> actions() throws RestApiException {
        String request = getRequestPath() + "/actions";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return actionInfoParser.parseActionInfos(jsonElement);
    }

    @Override
    public SubmitType submitType() throws RestApiException {
        String request = getRequestPath() + "/submit_type";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritRestClient.getGson().fromJson(jsonElement, new TypeToken<SubmitType>() {}.getType());
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
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            BinaryResult result = BinaryResultUtils.createBinaryResult(response);
            return result;
        } catch (IOException e) {
            throw RestApiException.wrap("Request failed.", e);
        }
    }

    @Override
    public SubmitType testSubmitType(TestSubmitRuleInput in) throws RestApiException {
        String request = getRequestPath() + "/test.submit_type";
        String json = gerritRestClient.getGson().toJson(in);
        JsonElement jsonElement = gerritRestClient.postRequest(request,json);
        return gerritRestClient.getGson().fromJson(jsonElement, new TypeToken<SubmitType>() {}.getType());
    }

    @Override
    public String description() throws RestApiException {
        String request = getRequestPath() + "/description";
        return gerritRestClient.getRequest(request).getAsString();
    }

    protected String getRequestPath() {
        return changeApiRestClient.getRequestPath() + "/revisions/" + revision;
    }
}
