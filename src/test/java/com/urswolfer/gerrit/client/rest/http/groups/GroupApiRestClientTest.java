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
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Shawn Stafford
 */
public class GroupApiRestClientTest {

    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final GroupInfo MOCK_GROUP_INFO = EasyMock.createMock(GroupInfo.class);
    private static final AccountInfo MOCK_ACCOUNT_INFO = EasyMock.createMock(AccountInfo.class);

    @Test
    public void testGetGroupInfo() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfo(MOCK_JSON_ELEMENT, MOCK_GROUP_INFO)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        GroupInfo groupInfo = groupApiRestClient.get();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(groupInfo).isEqualTo(MOCK_GROUP_INFO);
    }

    @Test
    public void testGetGroupDetail() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/detail", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfo(MOCK_JSON_ELEMENT, MOCK_GROUP_INFO)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        GroupInfo groupInfo = groupApiRestClient.detail();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(groupInfo).isEqualTo(MOCK_GROUP_INFO);
    }

    @Test
    public void testGetGroupOwner() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/owner", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfo(MOCK_JSON_ELEMENT, MOCK_GROUP_INFO)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        GroupInfo groupInfo = groupApiRestClient.owner();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(groupInfo).isEqualTo(MOCK_GROUP_INFO);
    }

    @Test
    public void testSetGroupOwner() throws Exception {
        String owner = "joe";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/groups/foo/owner", owner, MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        groupApiRestClient.owner(owner);

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testGetGroupName() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/name", jsonObject)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, groupName);

        String name = groupApiRestClient.name();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(name).isEqualTo(groupName);
    }

    @Test
    public void testSetGroupName() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        String newGroupName = "bar";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/groups/foo/name", newGroupName, jsonObject)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, groupName);

        groupApiRestClient.name(newGroupName);

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testGetGroupDescription() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/description", jsonObject)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, groupName);

        String description = groupApiRestClient.description();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(description).isEqualTo(groupName);
    }

    @Test
    public void testSetGroupDescription() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        String description = "bar";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/groups/foo/description", description, jsonObject)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, groupName);

        groupApiRestClient.description(description);

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testGetGroupMembers() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/members", MOCK_JSON_ELEMENT)
            .expectGet("/groups/foo/members?recursive", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupMembers(MOCK_JSON_ELEMENT, Collections.singletonList(MOCK_ACCOUNT_INFO))
            .expectParseGroupMembers(MOCK_JSON_ELEMENT, Collections.singletonList(MOCK_ACCOUNT_INFO))
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        List<AccountInfo> members = groupApiRestClient.members();
        List<AccountInfo> membersRecursive = groupApiRestClient.members(true);

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(members.get(0)).isEqualTo(MOCK_ACCOUNT_INFO);
        Truth.assertThat(membersRecursive.get(0)).isEqualTo(MOCK_ACCOUNT_INFO);
    }

    @Test
    public void testGetIncludedGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/groups/", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfos(MOCK_JSON_ELEMENT, Collections.singletonList(MOCK_GROUP_INFO))
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        List<GroupInfo> groupInfos = groupApiRestClient.includedGroups();

        EasyMock.verify(gerritRestClient, groupsParser);
        Truth.assertThat(groupInfos.get(0)).isEqualTo(MOCK_GROUP_INFO);
    }

    @Test
    public void testAddMembers() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPost("/groups/foo/members", "{\"members\":[\"joe\",\"peter\"]}")
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        groupApiRestClient.addMembers("joe", "peter");

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testAddGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPost("/groups/foo/groups", "{\"groups\":[\"g1\",\"g2\"]}")
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        groupApiRestClient.addGroups("g1", "g2");

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testRemoveGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPost("/groups/foo/groups.delete", "{\"groups\":[\"g1\",\"g2\"]}")
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder().get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, groupsParser, "foo");

        groupApiRestClient.removeGroups("g1", "g2");

        EasyMock.verify(gerritRestClient, groupsParser);
    }
}
