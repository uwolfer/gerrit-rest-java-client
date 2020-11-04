/*
 * Copyright 2013-2020 Urs Wolfer
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

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.api.changes.AddReviewerResult;
import com.google.gerrit.extensions.api.changes.ReviewerInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Parser for all information related to reviewers.
 *
 * @author EFregnan
 */
public class ReviewerInfosParser {

    private static final Type REVIEWER_INFO = new TypeToken<List<ReviewerInfo>>() {}.getType();
    private static final Type SUGGESTED_REVIEWER_INFO = new TypeToken<List<SuggestedReviewerInfo>>() {}.getType();

    private final Gson gson;

    public ReviewerInfosParser(Gson gson) {
        this.gson = gson;
    }

    public List<ReviewerInfo> parseReviewerInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result,  ReviewerInfo.class));
        }
        return gson.fromJson(result, REVIEWER_INFO);
    }

    public List<SuggestedReviewerInfo> parseSuggestReviewerInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result, SuggestedReviewerInfo.class));
        }
        return gson.fromJson(result, SUGGESTED_REVIEWER_INFO);
    }

    public AddReviewerResult parseAddReviewerResult(JsonElement result) {
        return gson.fromJson(result, AddReviewerResult.class);
    }
}
