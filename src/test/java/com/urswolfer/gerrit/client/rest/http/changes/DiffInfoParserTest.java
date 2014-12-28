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

import com.google.gerrit.extensions.common.ChangeType;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.DiffInfoBuilder;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

/**
 * @author Thomas Forrer
 */
public class DiffInfoParserTest extends AbstractParserTest {

    private static final DiffInfo DIFF_INFO;

    static  {
        DIFF_INFO = new DiffInfoBuilder()
                .withMetaA(fileMeta(
                        "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java",
                        "text/x-java-source", 372))
                .withMetaB(fileMeta(
                        "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java",
                        "text/x-java-source", 578))
                .withChangeType(ChangeType.MODIFIED)
                .addDiffHeader("diff --git a/gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java b/gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java")
                .addDiffHeader("index 59b7670..9faf81c 100644")
                .addDiffHeader("--- a/gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java")
                .addDiffHeader("+++ b/gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java")
                .withContent()
                    .withAb("// Copyright (C) 2010 The Android Open Source Project")
                    .withAb("//")
                    .withAb("// Licensed under the Apache License, Version 2.0 (the \"License\");")
                    .withAb("// you may not use this file except in compliance with the License.")
                    .withAb("// You may obtain a copy of the License at")
                    .withAb("//")
                    .withAb("// http://www.apache.org/licenses/LICENSE-2.0")
                    .withAb("//")
                    .withAb("// Unless required by applicable law or agreed to in writing, software")
                    .withAb("// distributed under the License is distributed on an \"AS IS\" BASIS,")
                    .withAb("// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.")
                    .withAb("// See the License for the specific language governing permissions and")
                    .withAb("// limitations under the License.").done()
                .withContent()
                    .withB("//")
                    .withB("// Add some more lines in the header.").done()
                .withContent()
                    .withAb("")
                    .withAb("package com.google.gerrit.server.project;")
                    .withAb("")
                    .withAb("import com.google.common.collect.Maps;").done()
                .get();
    }

    private static DiffInfo.FileMeta fileMeta(String name, String contentType, int lines) {
        DiffInfo.FileMeta fileMeta = new DiffInfo.FileMeta();
        fileMeta.name = name;
        fileMeta.contentType = contentType;
        fileMeta.lines = lines;
        return fileMeta;
    }

    @Test
    public void testParseDiffInfo() throws Exception {
        DiffInfoParser parser = new DiffInfoParser(getGson());
        JsonElement jsonElement = getJsonElement("diff.json");
        DiffInfo diffInfo = parser.parseDiffInfo(jsonElement);
        GerritAssert.assertEquals(diffInfo, DIFF_INFO);
    }
}
