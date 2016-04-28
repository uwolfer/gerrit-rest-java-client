/*
 * Copyright 2013-2016 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gson.JsonElement;
import org.easymock.EasyMock;

import java.util.List;

/**
 * @author Urs Wolfer
 */
public final class GroupsParserBuilder {
    private final GroupsParser groupsParser = EasyMock.createMock(GroupsParser.class);

    public GroupsParser get() {
        EasyMock.replay(groupsParser);
        return groupsParser;
    }

    public GroupsParserBuilder expectParseGroupInfos(JsonElement jsonElement, List<GroupInfo> result) {
        EasyMock.expect(groupsParser.parseGroupInfos(jsonElement))
                .andReturn(result).once();
        return this;
    }

    public GroupsParserBuilder expectParseGroupInfo(JsonElement jsonElement, GroupInfo result) {
        EasyMock.expect(groupsParser.parseGroupInfo(jsonElement))
                .andReturn(result).once();
        return this;
    }

    public GroupsParserBuilder expectParseGroupMembers(JsonElement jsonElement, List<AccountInfo> result) {
        EasyMock.expect(groupsParser.parseGroupMembers(jsonElement))
                .andReturn(result).once();
        return this;
    }
}
