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

import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author EFregnan
 */
public class HashtagsParserTest extends AbstractParserTest {
    private static final Set<String> HASHTAGS = new LinkedHashSet<>();

    static {
       HASHTAGS.add("first");
       HASHTAGS.add("last");
    }

    private HashtagsParser hashtagsParser = new HashtagsParser(getGson());

    @Test
    public void testParseHashtags() throws Exception {
        Set<String> hashtags = parseHashtags();
        GerritAssert.assertEquals(hashtags, HASHTAGS);
    }

    private Set<String> parseHashtags() throws Exception {
        JsonElement jsonElement = getJsonElement("hashtags.json");
        return hashtagsParser.parseHashtags(jsonElement);
    }
}
