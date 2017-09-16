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

import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Shawn Stafford
 */
public class GroupApiRestClient extends GroupApi.NotImplemented implements GroupApi {

    /** Base REST URL for managing Group data */
    private static final String BASE_URL = "/groups";

    private final GroupsParser groupsParser;
    private final GerritRestClient gerritRestClient;
    private final String groupId;

    public GroupApiRestClient(
        GerritRestClient gerritRestClient,
        GroupsParser groupsParser,
        String id)
    {
        this.gerritRestClient = gerritRestClient;
        this.groupsParser = groupsParser;
        this.groupId = id;
    }

    public static String getBaseRequestPath() {
        return BASE_URL;
    }

    public static String getRequestPath(String id) {
        return BASE_URL + "/" + Url.encode(id);
    }

    protected String getRequestPath() {
        return getRequestPath(groupId);
    }

    @Override
    public GroupInfo get() throws RestApiException {
        String restPath = getRequestPath();
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupInfo(result);
    }

    @Override
    public GroupInfo detail() throws RestApiException {
        String restPath = getRequestPath() + "/detail";
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupInfo(result);
    }

    @Override
    public String name() throws RestApiException {
        String restPath = getRequestPath() + "/name";
        return gerritRestClient.getRequest(restPath).getAsString();
    }

    @Override
    public void name(String name) throws RestApiException {
        String restPath = getRequestPath() + "/name";
        gerritRestClient.putRequest(restPath, name);
    }

    @Override
    public GroupInfo owner() throws RestApiException {
        String restPath = getRequestPath() + "/owner";
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupInfo(result);
    }

    @Override
    public void owner(String owner) throws RestApiException {
        String restPath = getRequestPath() + "/owner";
        gerritRestClient.putRequest(restPath, owner);
    }

    @Override
    public String description() throws RestApiException {
        String restPath = getRequestPath() + "/description";
        return gerritRestClient.getRequest(restPath).getAsString();
    }

    @Override
    public void description(String description) throws RestApiException {
        String restPath = getRequestPath() + "/description";
        gerritRestClient.putRequest(restPath, description);
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
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupMembers(result);
    }

    @Override
    public void addMembers(String... members) throws RestApiException {
        String restPath = getRequestPath() + "/members";

        // Create an object which can be used to create the json for:
        // { members: [ "member1", "member2" ] }
        Map<String, List<String>> memberMap =
            Collections.singletonMap("members", Arrays.asList(members));
        String json = gerritRestClient.getGson().toJson(memberMap);
        gerritRestClient.postRequest(restPath, json);
    }

    @Override
    public List<GroupInfo> includedGroups() throws RestApiException {
        String restPath = getRequestPath() + "/groups/";
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupInfos(result);
    }

    @Override
    public void addGroups(String... groups) throws RestApiException {
        String restPath = getRequestPath() + "/groups";

        // Create an object which can be used to create the json for:
        // { groups: [ "group1", "group2 ] }
        Map<String, List<String>> groupMap =
            Collections.singletonMap("groups", Arrays.asList(groups));
        String json = gerritRestClient.getGson().toJson(groupMap);

        gerritRestClient.postRequest(restPath, json);
    }

    @Override
    public void removeGroups(String... groups) throws RestApiException {
        String restPath = getRequestPath() + "/groups.delete";
        Map<String, List<String>> groupMap =
            Collections.singletonMap("groups", Arrays.asList(groups));
        String json = gerritRestClient.getGson().toJson(groupMap);

        gerritRestClient.postRequest(restPath, json);
    }
}
