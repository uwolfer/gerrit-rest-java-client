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

import static com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest.restContext;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.DeleteCommentInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.junit.Test;

public class CommentApiRestClientTest {
    private static final String COMMENT_ID = "TvcXrmjM";
    private static final String REVISION_ID = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";

    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + REVISION_ID + "/comments/" + COMMENT_ID, JsonParser.parseString("{\"message\":\"a comment\"}"))
            .get();
        CommentInfo commentInfo = EasyMock.createMock(CommentInfo.class);

        RevisionApiRestClient revisionApiRestClient = EasyMock.createMock(RevisionApiRestClient.class);
        EasyMock.expect(revisionApiRestClient.getRequestPath()).andReturn(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/" + REVISION_ID);
        EasyMock.replay(revisionApiRestClient);

        CommentApiRestClient commentApiRestClient = new CommentApiRestClient(restContext(gerritRestClient), revisionApiRestClient, COMMENT_ID);

        CommentInfo result = commentApiRestClient.get();

        Truth.assertThat(result.message).isEqualTo("a comment");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDelete() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + REVISION_ID + "/comments/" + COMMENT_ID + "/delete",
                "{\"reason\":\"Rejected by admin\"}",
                JsonParser.parseString("{\"message\":\"a comment\"}"))
            .get();
        CommentInfo commentInfo = EasyMock.createMock(CommentInfo.class);

        RevisionApiRestClient revisionApiRestClient = EasyMock.createMock(RevisionApiRestClient.class);
        EasyMock.expect(revisionApiRestClient.getRequestPath()).andReturn(
            "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/" + REVISION_ID);
        EasyMock.replay(revisionApiRestClient);

        CommentApiRestClient commentApiRestClient = new CommentApiRestClient(restContext(gerritRestClient), revisionApiRestClient, COMMENT_ID);

        DeleteCommentInput input = new DeleteCommentInput();
        input.reason = "Rejected by admin";
        CommentInfo result = commentApiRestClient.delete(input);

        Truth.assertThat(result.message).isEqualTo("a comment");
        EasyMock.verify(gerritRestClient);
    }
}
