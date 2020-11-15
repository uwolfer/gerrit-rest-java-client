/*
 * Copyright 2013-2020 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.ActionInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gerrit.extensions.common.EditInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Parser for commit-related entities and review actions.
 * This class contains the following parsers: ActionInfo parser, DiffInfo parser, CommitInfo parser, and EditInfo parser.
 *
 * @author EFregnan
 */
public class CommitInfosParser {

    private static final Type ACTION_TYPE = new TypeToken<TreeMap<String, ActionInfo>>() {}.getType();
    private static final Type COMMIT_INFO_TYPE = new TypeToken<List<CommitInfo>>() {}.getType();
    private static final Type EDIT_INFO_TYPE = new TypeToken<List<EditInfo>>() {}.getType();

    private final Gson gson;

    public CommitInfosParser(Gson gson) {
        this.gson = gson;
    }

    public TreeMap<String, ActionInfo> parseActionInfos(JsonElement result) {
        return gson.fromJson(result, ACTION_TYPE);
    }

    public DiffInfo parseDiffInfo(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, DiffInfo.class);
    }

    public List<CommitInfo> parseCommitInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result, CommitInfo.class));
        }
        return gson.fromJson(result, COMMIT_INFO_TYPE);
    }

    public CommitInfo parseSingleCommentInfo(JsonObject result) {
        return gson.fromJson(result, CommitInfo.class);
    }

    public List<EditInfo> parseEditInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result,  EditInfo.class));
        }
        return gson.fromJson(result, EDIT_INFO_TYPE);
    }
}
