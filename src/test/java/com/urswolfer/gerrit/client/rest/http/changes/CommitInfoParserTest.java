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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.junit.Test;

import java.util.List;

/**
 * @author Chenglong Sun
 */
public class CommitInfoParserTest extends AbstractParserTest {
    private final CommitInfoParser commitInfoParser = new CommitInfoParser(getGson());

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("commit.json");
        List<CommitInfo> commitInfos = commitInfoParser.parseCommitInfos(jsonElement);
        Truth.assertThat(commitInfos).hasSize(1);
        Truth.assertThat(commitInfos.get(0).message).isEqualTo("Use an EventBus to manage star icons  Image widgets that need to ...");
    }
}
