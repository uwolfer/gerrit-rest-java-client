/*
 * Copyright 2013-2026 Urs Wolfer
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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

/**
 * @author Shawn Stafford
 */
public class GroupApiRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testGetGroupInfo() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo", EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        GroupInfo groupInfo = groupApiRestClient.get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetGroupDetail() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/detail", EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        GroupInfo groupInfo = groupApiRestClient.detail();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetGroupOwner() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/owner", EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        GroupInfo groupInfo = groupApiRestClient.owner();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetGroupOwner() throws Exception {
        String owner = "joe";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/groups/foo/owner", owner, EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        groupApiRestClient.owner(owner);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetGroupName() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/name", jsonObject)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, groupName);

        String name = groupApiRestClient.name();

        EasyMock.verify(gerritRestClient);
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
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, groupName);

        groupApiRestClient.name(newGroupName);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetGroupDescription() throws Exception {
        String groupName = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(groupName);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/description", jsonObject)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, groupName);

        String description = groupApiRestClient.description();

        EasyMock.verify(gerritRestClient);
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
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, groupName);

        groupApiRestClient.description(description);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetGroupMembers() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/members", EMPTY_JSON_OBJECT)
            .expectGet("/groups/foo/members?recursive", EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        List<AccountInfo> members = groupApiRestClient.members();
        List<AccountInfo> membersRecursive = groupApiRestClient.members(true);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetIncludedGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/foo/groups/", EMPTY_JSON_OBJECT)
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        List<GroupInfo> groupInfos = groupApiRestClient.includedGroups();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAddMembers() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/groups/foo/members", "{\"members\":[\"joe\",\"peter\"]}")
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        groupApiRestClient.addMembers("joe", "peter");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testAddGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/groups/foo/groups", "{\"groups\":[\"g1\",\"g2\"]}")
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        groupApiRestClient.addGroups("g1", "g2");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRemoveGroups() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/groups/foo/groups.delete", "{\"groups\":[\"g1\",\"g2\"]}")
            .get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");

        groupApiRestClient.removeGroups("g1", "g2");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testRemoveMembers() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/groups/foo/members.delete", "{\"members\":[\"joe\",\"peter\"]}").get();
        GroupApiRestClient groupApiRestClient = new GroupApiRestClient(gerritRestClient, gerritJson, "foo");
        groupApiRestClient.removeMembers("joe", "peter");
        EasyMock.verify(gerritRestClient);
    }
}
