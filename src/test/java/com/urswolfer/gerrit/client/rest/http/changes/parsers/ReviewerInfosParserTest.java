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

package com.urswolfer.gerrit.client.rest.http.changes.parsers;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.AddReviewerResult;
import com.google.gerrit.extensions.api.changes.ReviewerInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author EFregnan
 */
public class ReviewerInfosParserTest extends AbstractParserTest {
    private final ReviewerInfosParser reviewerInfoParser = new ReviewerInfosParser(getGson());

    @Test
    public void testParseReviewerInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("reviewer.json");
        List<ReviewerInfo> reviewerInfos = reviewerInfoParser.parseReviewerInfos(jsonElement);
        Truth.assertThat(reviewerInfos).hasSize(1);
        Truth.assertThat(reviewerInfos.get(0).approvals.get("Code-Review")).isEqualTo("+2");
    }

    @Test
    public void testParseReviewersInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("reviewers.json");
        List<ReviewerInfo> reviewerInfos = reviewerInfoParser.parseReviewerInfos(jsonElement);
        Truth.assertThat(reviewerInfos).hasSize(2);
        Truth.assertThat(reviewerInfos.get(1).approvals.get("My-Own-Label")).isEqualTo("-2");
    }

    @Test
    public void testParseSuggestReviewerInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewer.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = reviewerInfoParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
    }

    @Test
    public void testParseSuggestReviewersInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewers.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = reviewerInfoParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(2);
    }

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("addreviewer.json");
        AddReviewerResult addReviewerResult = reviewerInfoParser.parseAddReviewerResult(jsonElement);
        Truth.assertThat(addReviewerResult.input).isEqualTo("john.doe@example.com");
        Truth.assertThat(addReviewerResult.reviewers.size()).isEqualTo(1);
        Truth.assertThat(addReviewerResult.reviewers.get(0)._accountId).isEqualTo(1000096);
    }

}
