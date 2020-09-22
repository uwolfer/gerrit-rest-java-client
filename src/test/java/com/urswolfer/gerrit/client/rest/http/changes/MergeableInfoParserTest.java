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

import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.MergeableInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

/**
 * @author EFregnan
 */
public class MergeableInfoParserTest extends AbstractParserTest {

    private static final MergeableInfo MERGEABLE_INFO = new MergeableInfoBuilder()
        .withSubmitType(SubmitType.MERGE_IF_NECESSARY)
        .withStrategy("recursive")
        .withMergeable(true)
        .get();

    private final MergeableInfoParser mergeableInfoParser = new MergeableInfoParser(getGson());

    @Test
    public void testParseMergeableInfo() throws Exception {
        MergeableInfo mergeableInfo = parseMergeable();
        GerritAssert.assertEquals(mergeableInfo, MERGEABLE_INFO);
    }

    private MergeableInfo parseMergeable() throws Exception {
        JsonElement jsonElement = getJsonElement("mergeable.json");
        return mergeableInfoParser.parseMergeableInfo(jsonElement);
    }

}
