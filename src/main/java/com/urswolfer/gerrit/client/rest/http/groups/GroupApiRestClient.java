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

import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author Shawn Stafford
 */
public class GroupApiRestClient extends GroupApi.NotImplemented implements GroupApi {

    /** Base REST URL for managing Group data */
    private static final String BASE_URL = "/groups";

    private final GerritJson gerritJson;
    private final GerritRestContext context;
    private final String groupId;

    public GroupApiRestClient(GerritRestContext context,
                              String id)
    {
        this.context = context;
        this.gerritJson = context.json();
        this.groupId = id;
    }

    public static String getBaseRequestPath() {
        return BASE_URL;
    }

    public static String getRequestPath(String id) {
        return BASE_URL + "/" + Url.encode(id);
    }

    /**
     * The group endpoints return either an array or an object keyed by group name. In the second
     * shape the name exists only as that key, so it has to be copied onto the parsed group.
     */
    static List<GroupInfo> parseGroupInfos(GerritJson gerritJson, JsonElement result) {
        if (result.isJsonArray()) {
            return gerritJson.asList(result, GroupInfo.class);
        }
        SortedMap<String, GroupInfo> groupsByName = gerritJson.asSortedMap(result, GroupInfo.class);
        for (Map.Entry<String, GroupInfo> entry : groupsByName.entrySet()) {
            entry.getValue().name = entry.getKey();
        }
        return new ArrayList<>(groupsByName.values());
    }

    protected String getRequestPath() {
        return getRequestPath(groupId);
    }

    @Override
    public GroupInfo get() throws RestApiException {
        return context.get(getRequestPath()).as(GroupInfo.class);
    }

    @Override
    public GroupInfo detail() throws RestApiException {
        return context.get(getRequestPath() + "/detail").as(GroupInfo.class);
    }

    @Override
    public String name() throws RestApiException {
        return context.get(getRequestPath() + "/name").asString();
    }

    @Override
    public void name(String name) throws RestApiException {
        String restPath = getRequestPath() + "/name";
        context.put(restPath).rawBody(name).send();
    }

    @Override
    public GroupInfo owner() throws RestApiException {
        return context.get(getRequestPath() + "/owner").as(GroupInfo.class);
    }

    @Override
    public void owner(String owner) throws RestApiException {
        String restPath = getRequestPath() + "/owner";
        context.put(restPath).rawBody(owner).send();
    }

    @Override
    public String description() throws RestApiException {
        return context.get(getRequestPath() + "/description").asString();
    }

    @Override
    public void description(String description) throws RestApiException {
        String restPath = getRequestPath() + "/description";
        context.put(restPath).rawBody(description).send();
    }

    @Override
    public List<AccountInfo> members() throws RestApiException {
        return members(false);
    }

    @Override
    public List<AccountInfo> members(boolean recursive) throws RestApiException {
        String restPath = getRequestPath() + "/members";
        if (recursive) {
            restPath += "?recursive";
        }
        return context.get(restPath).asList(AccountInfo.class);
    }

    @Override
    public void addMembers(String... members) throws RestApiException {
        String restPath = getRequestPath() + "/members";

        // Create an object which can be used to create the json for:
        // { members: [ "member1", "member2" ] }
        Map<String, List<String>> memberMap =
            Collections.singletonMap("members", Arrays.asList(members));
        context.post(restPath).body(memberMap).send();
    }

    @Override
    public List<GroupInfo> includedGroups() throws RestApiException {
        String restPath = getRequestPath() + "/groups/";
        JsonElement result = context.get(restPath).asJson();
        return parseGroupInfos(gerritJson, result);
    }

    @Override
    public void addGroups(String... groups) throws RestApiException {
        String restPath = getRequestPath() + BASE_URL;

        // Create an object which can be used to create the json for:
        // { groups: [ "group1", "group2 ] }
        Map<String, List<String>> groupMap =
            Collections.singletonMap("groups", Arrays.asList(groups));
        String json = gerritJson.toJson(groupMap);

        context.post(restPath).rawBody(json).send();
    }

    @Override
    public void removeGroups(String... groups) throws RestApiException {
        String restPath = getRequestPath() + "/groups.delete";
        Map<String, List<String>> groupMap =
            Collections.singletonMap("groups", Arrays.asList(groups));
        String json = gerritJson.toJson(groupMap);

        context.post(restPath).rawBody(json).send();
    }

    @Override
    public void removeMembers(String... members) throws RestApiException {
        String restPath = getRequestPath() + "/members.delete";
        Map<String, List<String>> memberMap =
            Collections.singletonMap("members", Arrays.asList(members));
        context.post(restPath).body(memberMap).send();
    }
}
