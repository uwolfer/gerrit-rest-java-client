package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.api.groups.GroupInput;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.UrlUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extend the Gerrit implementation in order to add custom functionality
 * that is not currently supported in the API.
 *
 * @author Shawn Stafford
 */
public class GroupsRestClient implements Groups {

    private final GerritRestClient gerritRestClient;
    private final GroupsParser groupsParser;

    public GroupsRestClient(GerritRestClient gerritRestClient, GroupsParser groupsParser) {
        this.gerritRestClient = gerritRestClient;
        this.groupsParser = groupsParser;
    }

    private String getRequestPath() {
        return "/groups";
    }

    /**
     * Look up a group by ID.
     * <p>
     * <strong>Note:</strong> This method eagerly reads the group. Methods that
     * mutate the group do not necessarily re-read the group. Therefore, calling a
     * getter method on an instance after calling a mutation method on that same
     * instance is not guaranteed to reflect the mutation. It is not recommended
     * to store references to {@code groupApi} instances.
     *
     * @param id any identifier supported by the REST API, including group name or
     *     UUID.
     * @return API for accessing the group.
     * @throws RestApiException if an error occurred.
     */
    public GroupApi id(String id) throws RestApiException {
        return new GroupApiRestClient(gerritRestClient, groupsParser, id);
    }

    /** Create a new group with the given name and default options. */
    public GroupApi create(String name) throws RestApiException {
        String restPath = GroupApiRestClient.getBaseRequestPath() + "/" + name;
        JsonElement result = gerritRestClient.putRequest(restPath);
        GroupInfo info = groupsParser.parseGroupInfo(result);
        return new GroupApiRestClient(gerritRestClient, groupsParser, info.id);
    }

    /** Create a new group. */
    public GroupApi create(GroupInput input) throws RestApiException {
        String restPath = GroupApiRestClient.getBaseRequestPath() + "/" + input.name;
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest(restPath, body);
        GroupInfo info = groupsParser.parseGroupInfo(result);
        return new GroupApiRestClient(gerritRestClient, groupsParser, info.id);
    }

    /** @return new request for listing groups. */
    public ListRequest list() {
        return new ListRequest() {
            @Override
            public SortedMap<String, GroupInfo> getAsMap() throws RestApiException {
                SortedMap<String, GroupInfo> map = new TreeMap<String, GroupInfo>();
                List<GroupInfo> list = GroupsRestClient.this.list(this);
                if (list != null) {
                    Iterator listIter = list.iterator();
                    while (listIter.hasNext()) {
                        GroupInfo group = (GroupInfo) listIter.next();
                        map.put(group.id, group);
                    }
                }
                return map;
            }
        };
    }

    private List<GroupInfo> list(ListRequest listParameter) throws RestApiException {
        List<GroupInfo> list = null;

        // Construct the query parameters
        String query = "";
        if (listParameter.getLimit() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "n=" + listParameter.getLimit());
        }
        if (listParameter.getStart() > 0) {
            query = UrlUtils.appendToUrlQuery(query, "S=" + listParameter.getLimit());
        }
        if (listParameter.getOwned()) {
            query = UrlUtils.appendToUrlQuery(query, "owned");
        }
        if ((listParameter.getSuggest() != null) && (listParameter.getSuggest().length() > 0)) {
            // 1. If this option is set and n is not set, then n defaults to 10
            // 2. When using this option, the project or p option can be used to
            //    name the current project, to allow context-dependent suggestions
            // 3. Not compatible with visible-to-all, owned, user, match, q, or S
            query = UrlUtils.appendToUrlQuery(query, "suggest=" + listParameter.getSuggest());
        }

        // TODO if (listParameter.getVisibleToAll()) {}
        // Not sure how to handle the visible-to-all parameter, or even how to
        // determine whether to throw a NotImplementedException

        // TODO Implement the other query parameters supported by Groups.ListRequest
        if ((listParameter.getOptions() != null) && (listParameter.getOptions().size() > 0)) {
            throw new NotImplementedException();
        }
        if ((listParameter.getProjects() != null) && (listParameter.getProjects().size() > 0)) {
            throw new NotImplementedException();
        }
        if ((listParameter.getGroups() != null) && (listParameter.getGroups().size() > 0)) {
            throw new NotImplementedException();
        }
        if (listParameter.getUser() != null) {
            throw new NotImplementedException();
        }
        if (listParameter.getSubstring() != null) {
            throw new NotImplementedException();
        }

        String url = GroupApiRestClient.getBaseRequestPath() + "/";
        if (!Strings.isNullOrEmpty(query)) {
            url += '?' + query;
        }
        JsonElement result = gerritRestClient.getRequest(url);
        if (result == null) {
            list = Collections.emptyList();
        } else {
            list = groupsParser.parseGroupInfos(result);
        }

        return list;
    }
}
