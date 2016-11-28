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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.api.groups.GroupInput;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Shawn Stafford
 */
public class GroupsRestClient extends Groups.NotImplemented implements Groups {

    private final GerritRestClient gerritRestClient;
    private final GroupsParser groupsParser;

    public GroupsRestClient(GerritRestClient gerritRestClient, GroupsParser groupsParser) {
        this.gerritRestClient = gerritRestClient;
        this.groupsParser = groupsParser;
    }

    @Override
    public GroupApi id(String id) throws RestApiException {
        return new GroupApiRestClient(gerritRestClient, groupsParser, id);
    }

    @Override
    public GroupApi create(String name) throws RestApiException {
        GroupInput groupInput = new GroupInput();
        groupInput.name = name;
        return create(groupInput);
    }

    @Override
    public GroupApi create(GroupInput input) throws RestApiException {
        String restPath = GroupApiRestClient.getBaseRequestPath() + "/" + Url.encode(input.name);
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(restPath, body);
        GroupInfo info = groupsParser.parseGroupInfo(result);
        return new GroupApiRestClient(gerritRestClient, groupsParser, info.id);
    }

    @Override
    public ListRequest list() {
        return new ListRequest() {
            @Override
            public SortedMap<String, GroupInfo> getAsMap() throws RestApiException {
                SortedMap<String, GroupInfo> map = new TreeMap<String, GroupInfo>();
                List<GroupInfo> list = GroupsRestClient.this.list(this);
                if (list != null) {
                    for (GroupInfo group : list) {
                        map.put(group.id, group);
                    }
                }
                return map;
            }
        };
    }

    private List<GroupInfo> list(ListRequest listParameter) throws RestApiException {
        String query = "";
        if (listParameter.getLimit() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "n=" + listParameter.getLimit());
        }
        if (listParameter.getStart() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "S=" + listParameter.getStart());
        }
        if (listParameter.getOwned()) {
            query = UrlUtils.appendToUrlQuery(query, "owned");
        }
        if (!Strings.isNullOrEmpty(listParameter.getSuggest())) {
            // 1. If this option is set and n is not set, then n defaults to 10
            // 2. When using this option, the project or p option can be used to
            //    name the current project, to allow context-dependent suggestions
            // 3. Not compatible with visible-to-all, owned, user, match, q, or S
            query = UrlUtils.appendToUrlQuery(query, "suggest=" + listParameter.getSuggest());
        }

        if (listParameter.getVisibleToAll()) {
            throw new NotImplementedException();
        }
        if (!listParameter.getOptions().isEmpty()) {
            throw new NotImplementedException();
        }
        if (!listParameter.getProjects().isEmpty()) {
            throw new NotImplementedException();
        }
        if (!listParameter.getGroups().isEmpty()) {
            throw new NotImplementedException();
        }
        if (!Strings.isNullOrEmpty(listParameter.getUser())) {
            throw new NotImplementedException();
        }
        if (!Strings.isNullOrEmpty(listParameter.getSubstring())) {
            throw new NotImplementedException();
        }

        String url = GroupApiRestClient.getBaseRequestPath() + "/";
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }
        JsonElement result = gerritRestClient.getRequest(url);
        if (result == null) {
            return Collections.emptyList();
        } else {
            return groupsParser.parseGroupInfos(result);
        }
    }
}
