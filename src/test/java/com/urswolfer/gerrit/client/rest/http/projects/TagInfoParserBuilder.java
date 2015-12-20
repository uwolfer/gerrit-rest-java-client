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

import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gson.JsonElement;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @author Pavel Bely
 */
public class TagInfoParserBuilder {
    private final TagInfoParser tagInfoParser = EasyMock.createMock(TagInfoParser.class);

    public TagInfoParser get() {
        EasyMock.replay(tagInfoParser);
        return tagInfoParser;
    }

    public TagInfoParserBuilder expectParseTagInfos(JsonElement jsonElement, List<TagInfo> result) {
        EasyMock.expect(tagInfoParser.parseTagInfos(jsonElement))
                .andReturn(result).once();
        return this;
    }
}
