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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import com.urswolfer.gerrit.client.rest.http.common.TagInfoBuilder;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Pavel Bely
 */
public class TagInfoParserTest extends AbstractParserTest {
    private static final List<TagInfo> TAG_INFO_LIST = Lists.newArrayList();

    static {
        TAG_INFO_LIST.add(new TagInfoBuilder()
                .withRef("refs/tags/v0.0.1")
                .withRevision("a6e054e9c220e4fa16802ccab074a00d384067fc")
                .get());
        TAG_INFO_LIST.add(new TagInfoBuilder()
                .withRef("refs/tags/v0.0.2")
                .withRevision("fd608fbe625a2b456d9f15c2b1dc41f252057dd7")
                .get());
        TAG_INFO_LIST.add(new TagInfoBuilder()
                .withRef("refs/tags/v0.0.3")
                .withRevision("8bbde7aacf771a9afb6992434f1ae413e010c6d8")
                .get());
    }

    private final TagInfoParser tagInfoParser = new TagInfoParser(getGson());

    @Test
    public void testParseTagInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("tags.json");

        List<TagInfo> tagInfos = tagInfoParser.parseTagInfos(jsonElement);

        Truth.assertThat(tagInfos.size()).isEqualTo(3);
        for (int i = 0; i < tagInfos.size(); i++) {
            TagInfo actual = tagInfos.get(i);
            TagInfo expected = TAG_INFO_LIST.get(i);
            GerritAssert.assertEquals(actual, expected);
        }
    }

    @Test
    public void testParseTagInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("tag.json");

        List<TagInfo> tagInfos = tagInfoParser.parseTagInfos(jsonElement);

        Truth.assertThat(tagInfos.size()).isEqualTo(1);
        GerritAssert.assertEquals(tagInfos.get(0), new TagInfoBuilder()
            .withRef("refs/tags/v0.0.1")
            .withRevision("a6e054e9c220e4fa16802ccab074a00d384067fc")
            .get());
    }
}
