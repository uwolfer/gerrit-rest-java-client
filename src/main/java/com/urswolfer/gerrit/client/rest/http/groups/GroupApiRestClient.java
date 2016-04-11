package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.gerrit.extensions.api.groups.GroupApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupAuditEventInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.common.GroupOptionsInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;


/**
 * Extend the Gerrit implementation in order to add custom functionality
 * that is not currently supported in the API.
 *
 * @author Shawn Stafford
 */
public class GroupApiRestClient implements GroupApi {

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
        return BASE_URL + "/" + id;
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
    public GroupOptionsInfo options() throws RestApiException {
        throw new NotImplementedException();
    }

    @Override
    public void options(GroupOptionsInfo options) throws RestApiException {
        throw new NotImplementedException();
    }

    @Override
    public List<AccountInfo> members() throws RestApiException {
        return members(false);
    }

    @Override
    public List<AccountInfo> members(boolean recursive) throws RestApiException {
        String restPath = null;
        if (recursive) {
            restPath = getRequestPath() + "/members/?recursive";
        } else {
            restPath = getRequestPath() + "/members";
        }
        JsonElement result = gerritRestClient.getRequest(restPath);
        return groupsParser.parseGroupMembers(result);
    }

    @Override
    public void addMembers(String... members) throws RestApiException {
        String restPath = getRequestPath() + "/members";

        // Create an object which can be used to create the json for:
        // { members: [ "member1", "member2" ] }
        Map<String, List<String>> memberMap = new HashMap<String, List<String>>();
        memberMap.put("members", Arrays.asList(members));
        String json = gerritRestClient.getGson().toJson(memberMap);

        JsonElement result = gerritRestClient.postRequest(restPath, json);
    }

    @Override
    public void removeMembers(String... members) throws RestApiException {
        throw new NotImplementedException();
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
        Map<String, List<String>> groupMap = new HashMap<String, List<String>>();
        groupMap.put("groups", Arrays.asList(groups));
        String json = gerritRestClient.getGson().toJson(groupMap);

        gerritRestClient.postRequest(restPath, json);
    }

    @Override
    public void removeGroups(String... groups) throws RestApiException {
        throw new NotImplementedException();
    }

    @Override
    public List<? extends GroupAuditEventInfo> auditLog() throws RestApiException {
        throw new NotImplementedException();
    }

}
