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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.common.GroupOptionsInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Shawn Stafford
 */
public class GroupsParserTest extends AbstractParserTest {
    private final GroupsParser groupsParser = new GroupsParser(getGson());

    private final GroupInfo testGroup;

    public GroupsParserTest() {
        testGroup = new GroupInfo();
        testGroup.id = "6a1e70e1a88782771a91808c8af9bbb7a9871389";
        testGroup.name = "Administrators";
        testGroup.url = "#/admin/groups/uuid-6a1e70e1a88782771a91808c8af9bbb7a9871389";
        testGroup.description = "Gerrit Site Administrators";
        testGroup.groupId = 1;
        testGroup.owner = "Administrators";
        testGroup.ownerId = "6a1e70e1a88782771a91808c8af9bbb7a9871389";

        testGroup.options = new GroupOptionsInfo();
    }

    @Test
    public void testParseGroupInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("group.json");

        GroupInfo groupInfo = groupsParser.parseGroupInfo(jsonElement);

        GerritAssert.assertEquals(groupInfo, testGroup);
    }

    @Test
    public void testParseGroupInfoWithNullJsonElement() throws Exception {
        GroupInfo groupInfo = groupsParser.parseGroupInfo(null);

        Truth.assertThat(groupInfo).isNull();
    }

    @Test
    public void testParseGroupInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("groups.json");
        List<GroupInfo> groupInfos = groupsParser.parseGroupInfos(jsonElement);
        Truth.assertThat(groupInfos).hasSize(6);
    }

    @Test
    public void testParseGroupInfosArray() throws Exception {
        JsonElement jsonElement = getJsonElement("included_groups.json");
        List<GroupInfo> groupInfos = groupsParser.parseGroupInfos(jsonElement);
        Truth.assertThat(groupInfos).hasSize(6);
    }

    @Test
    public void testParseGroupMember() throws Exception {
        AccountInfo expectedAccountInfo = new AccountInfo(1000006);
        expectedAccountInfo.email = "peter@example.com";
        expectedAccountInfo.username = "peter";
        expectedAccountInfo.name = "Peter";

        JsonElement jsonElement = getJsonElement("group_member.json");
        AccountInfo accountInfo = groupsParser.parseGroupMember(jsonElement);
        GerritAssert.assertEquals(accountInfo, expectedAccountInfo);
    }

    @Test
    public void testParseGroupMembers() throws Exception {
        JsonElement jsonElement = getJsonElement("group_members.json");
        List<AccountInfo> accountInfos = groupsParser.parseGroupMembers(jsonElement);
        Truth.assertThat(accountInfos).hasSize(2);
    }
}
