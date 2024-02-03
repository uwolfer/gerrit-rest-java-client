/*
 * Copyright 2013-2021 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes.parsers;

import com.google.common.collect.Maps;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.*;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author EFregnan
 */
public class CommitInfosParserTest extends AbstractParserTest{

    private static final TreeMap<String, ActionInfo> ACTION_INFOS = Maps.newTreeMap();

    static {
        ACTION_INFOS.put("submit",
            new ActionInfoBuilder()
                .withMethod("POST")
                .withLabel("Submit")
                .withTitle("Submit patch set 1 into master")
                .withEnabled(true)
                .get()
        );
        ACTION_INFOS.put("cherrypick",
            new ActionInfoBuilder()
                .withMethod("POST")
                .withLabel("Cherry Pick")
                .withTitle("Cherry pick change")
                .withEnabled(true)
                .get()
            );
    }

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

    private CommitInfosParser commitInfosParser = new CommitInfosParser(getGson());

    @Test
    public void testParseActionInfos() throws Exception {
        SortedMap<String, ActionInfo> actions = parseActions();
        GerritAssert.assertEquals(actions, ACTION_INFOS);
    }

    private SortedMap<String, ActionInfo> parseActions() throws Exception {
        JsonElement jsonElement = getJsonElement("actions.json");
        return commitInfosParser.parseActionInfos(jsonElement);
    }

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("commit.json");
        List<CommitInfo> commitInfos = commitInfosParser.parseCommitInfos(jsonElement);
        Truth.assertThat(commitInfos).hasSize(1);
        Truth.assertThat(commitInfos.get(0).message).isEqualTo("Use an EventBus to manage star icons  Image widgets that need to ...");
    }

    @Test
    public void testParseDiffInfo() throws Exception {
        CommitInfosParser parser = new CommitInfosParser(getGson());
        JsonElement jsonElement = getJsonElement("diff.json");
        DiffInfo diffInfo = parser.parseDiffInfo(jsonElement);
        GerritAssert.assertEquals(diffInfo, DIFF_INFO);
    }

    @Test
    public void testParseEditInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("edit.json");
        List<EditInfo> editInfos = commitInfosParser.parseEditInfos(jsonElement);
        Truth.assertThat(editInfos).hasSize(1);
        Truth.assertThat(editInfos.get(0).baseRevision).isEqualTo("184ebe53805e102605d11f6b143486d15c23a09c");
        Truth.assertThat(editInfos.get(0).fetch.get("git").url).isEqualTo("git://localhost/gerrit");
        Truth.assertThat(editInfos.get(0).files.size()).isEqualTo(2);
    }

    @Test
    public void testParseEditInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("changeEditInfo.json");
        EditInfo request = commitInfosParser.parseEditInfo(jsonElement);
        Truth.assertThat(request.baseRevision).isEqualTo("c35558e0925e6985c91f3a16921537d5e572b7a3");
        Truth.assertThat(request.commit.subject).isEqualTo("Use an EventBus to manage star icons");
        Truth.assertThat(request.commit.message).isEqualTo("Use an EventBus to manage star icons\n\nImage widgets that need to ...");
        Truth.assertThat(request.ref).isEqualTo("refs/users/01/1000001/edit-76482/1");
    }
}
