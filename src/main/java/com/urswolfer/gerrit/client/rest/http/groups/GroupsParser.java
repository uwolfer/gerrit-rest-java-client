package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Extend the Gerrit implementation in order to add custom functionality
 * that is not currently supported in the API.
 *
 * @author Shawn Stafford
 */
public class GroupsParser {
    private static final Type GROUP_MAP_TYPE = new TypeToken<SortedMap<String, GroupInfo>>() {}.getType();
    private static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<AccountInfo>>() {}.getType();

    private final Gson gson;

    public GroupsParser(Gson gson) {
        this.gson = gson;
    }

    public GroupInfo parseGroupInfo(JsonElement result) throws RestApiException {
        return gson.fromJson(result, GroupInfo.class);
    }

    public AccountInfo parseGroupMember(JsonElement result) throws RestApiException {
        return gson.fromJson(result, AccountInfo.class);
    }

    public List<GroupInfo> parseGroupInfos(JsonElement result) throws RestApiException {
        SortedMap<String, GroupInfo> map = gson.fromJson(result, GROUP_MAP_TYPE);
        return new ArrayList(map.values());
    }

    public List<AccountInfo> parseGroupMembers(JsonElement result) throws RestApiException {
        List<AccountInfo> list = null;
        if (!result.isJsonArray()) {
            // Put the single element in a list
            list = Collections.singletonList(parseGroupMember(result));
        } else {
            list = gson.fromJson(result, ACCOUNT_LIST_TYPE);
        }
        return list;
    }

}
