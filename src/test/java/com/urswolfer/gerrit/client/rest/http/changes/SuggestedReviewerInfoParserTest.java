/*
 * Copyright 2013-2015 Urs Wolfer
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
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

import java.util.List;

public class SuggestedReviewerInfoParserTest extends AbstractParserTest {
    private final SuggestedReviewerInfoParser accountsParser = new SuggestedReviewerInfoParser(getGson());

    @Test
    public void testParseSuggestReviewerInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewer.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = accountsParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
    }

    @Test
    public void testParseSuggestReviewersInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewers.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = accountsParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(2);
    }
}
