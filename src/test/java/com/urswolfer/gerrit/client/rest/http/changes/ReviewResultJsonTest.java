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
import com.google.gerrit.extensions.api.changes.ReviewResult;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import org.junit.Test;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;

/**
 * @author Urs Wolfer
 */
public class ReviewResultJsonTest extends AbstractJsonTest {
    private final GerritJson gerritJson = new GerritJson(getGson());

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/review.json");
        ReviewResult reviewResult = gerritJson.as(jsonElement, ReviewResult.class);
        Truth.assertThat(reviewResult.labels.size()).isEqualTo(1);
        Truth.assertThat(reviewResult.reviewers).isNull();
    }
}
