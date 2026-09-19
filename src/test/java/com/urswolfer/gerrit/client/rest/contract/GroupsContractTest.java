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

package com.urswolfer.gerrit.client.rest.contract;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * Pins what {@code gerritApi.groups()} puts on the wire and what it makes of the response.
 *
 * <p>See {@link FakeGerritServer} for why these tests go through the public API instead of mocking
 * the client internals.
 */
public class GroupsContractTest {

    private static final String GROUP_ID = "6a1e70e1a88782771a91808c8af9bbb7a9871389";

    /**
     * {@code /groups/} returns a map keyed by group name; the client has to copy that key into
     * {@code GroupInfo.name}, which the response body itself does not carry.
     */
    @Test
    public void listCopiesTheMapKeyIntoTheGroupName() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/groups/", "groups/groups.json");

        Map<String, GroupInfo> groups = server.api().groups().list().getAsMap();

        Truth.assertThat(groups).containsKey(GROUP_ID);
        Truth.assertThat(groups.get(GROUP_ID).name).isEqualTo("Administrators");
        Truth.assertThat(server.trace()).containsExactly("GET /groups/");
        server.verify();
    }

    @Test
    public void listWithLimitAndStart() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/groups/?n=2&S=1", "groups/groups.json");

        server.api().groups().list().withLimit(2).withStart(1).getAsMap();

        server.verify();
    }

    @Test
    public void get() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/groups/" + GROUP_ID, "groups/group.json");

        Truth.assertThat(server.api().groups().id(GROUP_ID).get()).isNotNull();
        server.verify();
    }

    @Test
    public void members() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/groups/" + GROUP_ID + "/members", "groups/group_members.json");

        List<AccountInfo> members = server.api().groups().id(GROUP_ID).members();

        Truth.assertThat(members).hasSize(2);
        Truth.assertThat(members.get(0).name).isEqualTo("Joe");
        server.verify();
    }

    @Test
    public void addMembers() throws Exception {
        String path = "/groups/" + GROUP_ID + "/members";
        FakeGerritServer server = new FakeGerritServer().stubEmpty("POST", path);

        server.api().groups().id(GROUP_ID).addMembers("joe", "peter");

        Truth.assertThat(server.request("POST", path).getBody())
            .isEqualTo("{\"members\":[\"joe\",\"peter\"]}");
        server.verify();
    }

    @Test
    public void includedGroups() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/groups/" + GROUP_ID + "/groups/", "groups/included_groups.json");

        Truth.assertThat(server.api().groups().id(GROUP_ID).includedGroups()).isNotEmpty();
        server.verify();
    }
}
