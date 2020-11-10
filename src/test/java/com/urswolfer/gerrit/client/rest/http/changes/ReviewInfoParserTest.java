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

import java.util.Set;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;

/**
 * @author Leonard Brünings
 */
public class ReviewInfoParserTest extends AbstractParserTest {
    private static final Set<String> REVIEW_INFO = Sets.newLinkedHashSet(Lists.newArrayList(
            "/COMMIT_MSG",
            "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java"
    ));

    private final ReviewInfoParser reviewInfoParser = new ReviewInfoParser(getGson());

    @Test
    public void testParseReviewInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("files-reviewed.json");

        Set<String> reviewed = reviewInfoParser.parseFileInfos(jsonElement);
        GerritAssert.assertEquals(reviewed, REVIEW_INFO);
    }
}
