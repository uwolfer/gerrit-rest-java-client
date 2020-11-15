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

import org.testng.annotations.*;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;

public class IncludedInCommitInfosParserTest extends AbstractParserTest {
    private final IncludedInInfoParser includedInInfoParser = new IncludedInInfoParser(getGson());

    @Test
    public void testParseEditInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("includedin.json");
        IncludedInInfo includedInInfo = includedInInfoParser.parseIncludedInInfos(jsonElement);
        Truth.assertThat(includedInInfo.branches).hasSize(4);
        Truth.assertThat(includedInInfo.tags).hasSize(3);
        Truth.assertThat(includedInInfo.branches).containsAllOf("integration/master", "integration/releases/2.12", "master", "releases/2.12");
        Truth.assertThat(includedInInfo.tags).containsAllOf("2017-12_v2.12", "2017-12_v2.13", "v2.13.1_xyz");
    }
}
