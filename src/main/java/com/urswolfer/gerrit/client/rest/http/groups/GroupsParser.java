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

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

/**
 * @author Shawn Stafford
 */
public class GroupsParser {
    private static final Type GROUP_MAP_TYPE = new TypeToken<SortedMap<String, GroupInfo>>() {}.getType();
    private static final Type GROUP_LIST_TYPE = new TypeToken<List<GroupInfo>>() {}.getType();
    private static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<AccountInfo>>() {}.getType();

    private final Gson gson;

    public GroupsParser(Gson gson) {
        this.gson = gson;
    }

    public GroupInfo parseGroupInfo(JsonElement result) {
        return gson.fromJson(result, GroupInfo.class);
    }

    public AccountInfo parseGroupMember(JsonElement result) {
        return gson.fromJson(result, AccountInfo.class);
    }

    public List<GroupInfo> parseGroupInfos(JsonElement result) {
        if (result.isJsonArray()) {
            return gson.fromJson(result, GROUP_LIST_TYPE);
        } else {
            SortedMap<String, GroupInfo> map = gson.fromJson(result, GROUP_MAP_TYPE);
            return new ArrayList<GroupInfo>(map.values());
        }
    }

    public List<AccountInfo> parseGroupMembers(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(parseGroupMember(result));
        } else {
            return gson.fromJson(result, ACCOUNT_LIST_TYPE);
        }
    }

}
