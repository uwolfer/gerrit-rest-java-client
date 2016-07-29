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
import com.google.gerrit.extensions.common.EditInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

import java.util.List;

public class EditInfoParserTest extends AbstractParserTest {
    private final EditInfoParser editInfoParser = new EditInfoParser(getGson());

    @Test
    public void testParseEditInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("edit.json");
        List<EditInfo> editInfos = editInfoParser.parseEditInfos(jsonElement);
        Truth.assertThat(editInfos).hasSize(1);
        Truth.assertThat(editInfos.get(0).baseRevision.equals("184ebe53805e102605d11f6b143486d15c23a09c"));
        Truth.assertThat(editInfos.get(0).fetch.get("git").url.equals("git://localhost/gerrit"));
        Truth.assertThat(editInfos.get(0).files.size() == 2);
    }

}
