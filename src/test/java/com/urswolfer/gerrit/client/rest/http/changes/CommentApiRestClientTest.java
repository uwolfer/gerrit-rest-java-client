/*
 * Copyright 2013-2024 Urs Wolfer
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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.DeleteCommentInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.parsers.CommentsParser;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.junit.Test;

public class CommentApiRestClientTest {

    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final String COMMENT_ID = "TvcXrmjM";
    private static final String REVISION_ID = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";


    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + REVISION_ID + "/comments/" + COMMENT_ID, MOCK_JSON_ELEMENT)
            .get();
        CommentInfo commentInfo = EasyMock.createMock(CommentInfo.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseSingleCommentInfo(MOCK_JSON_ELEMENT)).andReturn(commentInfo);
        EasyMock.replay(commentsParser);

        RevisionApiRestClient revisionApiRestClient = EasyMock.createMock(RevisionApiRestClient.class);
        EasyMock.expect(revisionApiRestClient.getRequestPath()).andReturn(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/" + REVISION_ID);
        EasyMock.replay(revisionApiRestClient);

        CommentApiRestClient commentApiRestClient = new CommentApiRestClient(gerritRestClient, revisionApiRestClient,
            commentsParser, COMMENT_ID);

        CommentInfo result = commentApiRestClient.get();

        EasyMock.verify(gerritRestClient, commentsParser);
        Truth.assertThat(result).isEqualTo(commentInfo);
    }

    @Test
    public void testDelete() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + REVISION_ID + "/comments/" + COMMENT_ID + "/delete",
                "{\"reason\":\"Rejected by admin\"}",
                MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        CommentInfo commentInfo = EasyMock.createMock(CommentInfo.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);
        EasyMock.expect(commentsParser.parseSingleCommentInfo(MOCK_JSON_ELEMENT)).andReturn(commentInfo);
        EasyMock.replay(commentsParser);

        RevisionApiRestClient revisionApiRestClient = EasyMock.createMock(RevisionApiRestClient.class);
        EasyMock.expect(revisionApiRestClient.getRequestPath()).andReturn(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/" + REVISION_ID);
        EasyMock.replay(revisionApiRestClient);

        CommentApiRestClient commentApiRestClient = new CommentApiRestClient(gerritRestClient, revisionApiRestClient,
            commentsParser, COMMENT_ID);

        DeleteCommentInput input = new DeleteCommentInput();
        input.reason = "Rejected by admin";
        CommentInfo result = commentApiRestClient.delete(input);

        EasyMock.verify(gerritRestClient, commentsParser);
        Truth.assertThat(result).isEqualTo(commentInfo);
    }
}
