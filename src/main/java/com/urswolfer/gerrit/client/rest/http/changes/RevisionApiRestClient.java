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

import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.FileInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
    private final String revision;

    public RevisionApiRestClient(GerritRestClient gerritRestClient,
                                 ChangeApiRestClient changeApiRestClient,
                                 CommentsParser commentsParser,
                                 FileInfoParser fileInfoParser,
                                 DiffInfoParser diffInfoParser,
                                 String revision) {
        this.gerritRestClient = gerritRestClient;
        this.changeApiRestClient = changeApiRestClient;
        this.commentsParser = commentsParser;
        this.fileInfoParser = fileInfoParser;
        this.diffInfoParser = diffInfoParser;
        this.revision = revision;
    }

    public String revision() {
        return revision;
    }

    @Override
    public void review(ReviewInput reviewInput) throws RestApiException {
        String request = getRequestPath() + "/review";
        String json = gerritRestClient.getGson().toJson(reviewInput);
        gerritRestClient.postRequest(request, json);
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
    public void setReviewed(String path, boolean reviewed) throws RestApiException {
        String encodedPath = Url.encode(path);
        String url =  String.format("/changes/%s/revisions/%s/files/%s/reviewed", changeApiRestClient.id(), revision, encodedPath);
        if (reviewed) {
            gerritRestClient.putRequest(url);
        } else {
            gerritRestClient.deleteRequest(url);
        }
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

    private TreeMap<String, List<CommentInfo>> comments(String type) throws RestApiException {
        String request = getRequestPath() + '/' + type + '/';
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return commentsParser.parseCommentInfos(jsonElement);
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
        String request = getRequestPath() + "/files";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return fileInfoParser.parseFileInfos(jsonElement);
    }

    @Override
    public FileApi file(String path) {
        return new FileApiRestClient(gerritRestClient, this, diffInfoParser, path);
    }

    @Override
    public BinaryResult patch() throws RestApiException {
        String request = getRequestPath() + "/patch";
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw new RestApiException("Failed to get patch.", e);
        }
    }

    protected String getRequestPath() {
        return changeApiRestClient.getRequestPath() + "/revisions/" + revision;
    }
}
