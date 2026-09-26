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

import com.google.common.base.Strings;
import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.api.groups.GroupInput;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Shawn Stafford
 */
public class GroupsRestClient extends Groups.NotImplemented implements Groups {

    private final GerritRestContext context;
    private final GerritJson gerritJson;

    public GroupsRestClient(GerritRestContext context) {
        this.context = context;
        this.gerritJson = context.json();
    }

    @Override
    public GroupApi id(String id) throws RestApiException {
        return new GroupApiRestClient(context, id);
    }

    @Override
    public GroupApi create(String name) throws RestApiException {
        GroupInput groupInput = new GroupInput();
        groupInput.name = name;
        return create(groupInput);
    }

    @Override
    public GroupApi create(GroupInput input) throws RestApiException {
        String restPath = GroupApiRestClient.getRequestPath(input.name);
        JsonElement result = context.put(restPath).body(input).asJson();
        GroupInfo info = gerritJson.as(result, GroupInfo.class);
        return new GroupApiRestClient(context, info.id);
    }

    @Override
    public ListRequest list() {
        return new ListRequest() {
            @Override
            public SortedMap<String, GroupInfo> getAsMap() throws RestApiException {
                SortedMap<String, GroupInfo> map = new TreeMap<>();
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

        // suggest: when set and n is not, n defaults to 10; the project or p option can name the
        // current project for context-dependent suggestions; not compatible with visible-to-all,
        // owned, user, match, q or S
        String url = UrlQuery.of(GroupApiRestClient.getBaseRequestPath() + "/")
            .paramIfPositive("n", listParameter.getLimit())
            .paramIfPositive("S", listParameter.getStart())
            .flagIf(listParameter.getOwned(), "owned")
            .paramIfNotEmpty("suggest", listParameter.getSuggest())
            .paramIfNotEmpty("r", listParameter.getRegex())
            .paramIfNotEmpty("owned-by", listParameter.getOwnedBy())
            .toUrl();
        return groupInfos(url);
    }

    @Override
    public QueryRequest query() {
        return new QueryRequest() {
            @Override
            public List<GroupInfo> get() throws RestApiException {
                return GroupsRestClient.this.query(this);
            }
        };
    }

    /**
     * this method may does not support Gerrit versions lower than 3.2.0
     */
    protected List<GroupInfo> query(QueryRequest queryRequest) throws RestApiException {
        if (!queryRequest.getOptions().isEmpty()) {
            throw new NotImplementedException();
        }

        String url = UrlQuery.of(GroupApiRestClient.getBaseRequestPath() + "/")
            .paramIfNotEmpty("query", queryRequest.getQuery())
            .paramIfPositive("limit", queryRequest.getLimit())
            .paramIfPositive("start", queryRequest.getStart())
            .toUrl();
        return groupInfos(url);
    }

    /**
     * Both listing endpoints answer with nothing at all when there is nothing to list.
     */
    private List<GroupInfo> groupInfos(String url) throws RestApiException {
        JsonElement result = context.get(url).asJson();
        if (result == null) {
            return Collections.emptyList();
        }
        return GroupApiRestClient.parseGroupInfos(context.json(), result);
    }

    @Override
    public QueryRequest query(String query) {
        return query().withQuery(query);
    }
}
