/*
 * Copyright 2013-2017 Urs Wolfer
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
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.junit.Test;

/**
 * @author Urs Wolfer
 */
public class AddReviewerResultParserTest extends AbstractParserTest {
    private final AddReviewerResultParser addReviewerResultParser = new AddReviewerResultParser(getGson());

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("addreviewer.json");
        AddReviewerResult addReviewerResult = addReviewerResultParser.parseAddReviewerResult(jsonElement);
        Truth.assertThat(addReviewerResult.input).isEqualTo("john.doe@example.com");
        Truth.assertThat(addReviewerResult.reviewers.size()).isEqualTo(1);
        Truth.assertThat(addReviewerResult.reviewers.get(0)._accountId).isEqualTo(1000096);
    }
}
