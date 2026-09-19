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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.DraftInput;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

/**
 * @author Urs Wolfer
 */
public class DraftsApiRestClientTest extends AbstractJsonTest {
    @Test
    public void testGettingDraftById() throws Exception {
        String draftId = "89233d9c_56013406";
        String revisionId = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", draftId);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/"
                + revisionId + "/drafts/" + draftId, jsonObject)
            .get();

        GerritRestContext context = restContext(gerritRestClient);
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(context, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        RevisionApiRestClient revisionApiRestClient = new RevisionApiRestClient(context, changeApiRestClient, revisionId);

        CommentInfo commentInfo = revisionApiRestClient.draft(draftId).get();

        Truth.assertThat(commentInfo.id).isEqualTo(draftId);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGettingDraftByCommentInfo() throws Exception {
        String draftId = "89233d9c_56013406";
        String revisionId = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().get();

        CommentInfo expectedCommentInfo = new CommentInfo();
        expectedCommentInfo.id = draftId;

        GerritRestContext context = restContext(gerritRestClient);
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(context, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        RevisionApiRestClient revisionApiRestClient = new RevisionApiRestClient(context, changeApiRestClient, revisionId);
        DraftApiRestClient draftApiRestClient = new DraftApiRestClient(context, changeApiRestClient, revisionApiRestClient, expectedCommentInfo);

        CommentInfo commentInfo = draftApiRestClient.get();

        // this draft is handed to the client directly, so no parsing is involved
        Truth.assertThat(commentInfo).isSameInstanceAs(expectedCommentInfo);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testUpdateDraft() throws Exception {
        String draftId = "89233d9c_56013406";
        String revisionId = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";
        JsonElement jsonElement = new JsonObject();
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + revisionId + "/drafts/" + draftId, "{}", jsonElement)
            .get();

        GerritRestContext context = restContext(gerritRestClient);
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(context, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        RevisionApiRestClient revisionApiRestClient = new RevisionApiRestClient(context, changeApiRestClient, revisionId);
        DraftApiRestClient draftApiRestClient = new DraftApiRestClient(context, changeApiRestClient, revisionApiRestClient, "89233d9c_56013406");

        draftApiRestClient.update(new DraftInput());

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteDraft() throws Exception {
        String draftId = "89233d9c_56013406";
        String revisionId = "ec047590bc7fb8db7ae03ebac336488bfc1c5e12";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/" +
                "revisions/" + revisionId + "/drafts/" + draftId)
            .get();

        GerritRestContext context = restContext(gerritRestClient);
        ChangeApiRestClient changeApiRestClient = new ChangeApiRestClient(context, null, "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        RevisionApiRestClient revisionApiRestClient = new RevisionApiRestClient(context, changeApiRestClient, revisionId);
        DraftApiRestClient draftApiRestClient = new DraftApiRestClient(context, changeApiRestClient, revisionApiRestClient, "89233d9c_56013406");

        draftApiRestClient.delete();

        EasyMock.verify(gerritRestClient);
    }
}
