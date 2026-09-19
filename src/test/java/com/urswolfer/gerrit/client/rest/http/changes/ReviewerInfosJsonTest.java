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
import com.google.gerrit.extensions.api.changes.AddReviewerResult;
import com.google.gerrit.extensions.api.changes.ReviewerInfo;
import com.google.gerrit.extensions.api.changes.ReviewerResult;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import org.testng.annotations.Test;

import java.util.List;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;

/**
 * @author EFregnan
 */
public class ReviewerInfosJsonTest extends AbstractJsonTest {
    private final GerritJson gerritJson = new GerritJson(getGson());

    @Test
    public void testParseReviewerInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/reviewer.json");
        List<ReviewerInfo> reviewerInfos = gerritJson.asList(jsonElement, ReviewerInfo.class);
        Truth.assertThat(reviewerInfos).hasSize(1);
        Truth.assertThat(reviewerInfos.get(0).approvals.get("Code-Review")).isEqualTo("+2");
    }

    @Test
    public void testParseReviewersInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/reviewers.json");
        List<ReviewerInfo> reviewerInfos = gerritJson.asList(jsonElement, ReviewerInfo.class);
        Truth.assertThat(reviewerInfos).hasSize(2);
        Truth.assertThat(reviewerInfos.get(1).approvals.get("My-Own-Label")).isEqualTo("-2");
    }

    @Test
    public void testParseSuggestReviewerInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/suggestreviewer.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = gerritJson.asList(jsonElement, SuggestedReviewerInfo.class);
        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
    }

    @Test
    public void testParseSuggestReviewersInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/suggestreviewers.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = gerritJson.asList(jsonElement, SuggestedReviewerInfo.class);
        Truth.assertThat(suggestedReviewerInfos).hasSize(2);
    }

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/addreviewer.json");
        AddReviewerResult addReviewerResult = gerritJson.as(jsonElement, AddReviewerResult.class);
        Truth.assertThat(addReviewerResult.input).isEqualTo("john.doe@example.com");
        Truth.assertThat(addReviewerResult.reviewers.size()).isEqualTo(1);
        Truth.assertThat(addReviewerResult.reviewers.get(0)._accountId).isEqualTo(1000096);
    }

    @Test
    public void testParseReviewerResult() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/addreviewer.json");
        ReviewerResult reviewerResult = gerritJson.as(jsonElement, ReviewerResult.class);
        Truth.assertThat(reviewerResult.input).isEqualTo("john.doe@example.com");
        Truth.assertThat(reviewerResult.reviewers.size()).isEqualTo(1);
        Truth.assertThat(reviewerResult.reviewers.get(0)._accountId).isEqualTo(1000096);
    }

}
