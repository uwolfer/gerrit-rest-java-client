/*
 * Copyright 2013-2014 Urs Wolfer
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

import com.google.common.collect.Maps;
import com.google.gerrit.extensions.common.FileInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.FileInfoBuilder;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Thomas Forrer
 */
public class FileInfoParserTest extends AbstractParserTest {
    private static final Map<String, FileInfo> FILE_INFO_MAP = Maps.newLinkedHashMap();

    static {
        FILE_INFO_MAP.put(
                "/COMMIT_MSG",
                new FileInfoBuilder().withStatus('A').withLinesInserted(7).get()
        );
        FILE_INFO_MAP.put(
                "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java",
                new FileInfoBuilder().withLinesInserted(5).withLinesDeleted(3).get()
        );
    }

    private final FileInfoParser fileInfoParser = new FileInfoParser(getGson());

    @Test
    public void testParseFileInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("files.json");

        Map<String, FileInfo> fileInfoMap = fileInfoParser.parseFileInfos(jsonElement);
        GerritAssert.assertEquals(fileInfoMap, FILE_INFO_MAP);
    }
}
