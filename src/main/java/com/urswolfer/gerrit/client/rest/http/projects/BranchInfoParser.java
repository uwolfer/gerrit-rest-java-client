/*
 * Copyright 2013-2014 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.common.BranchInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Tim Coulson
 */
public class BranchInfoParser {
    private final Gson gson;
    private static final Type TYPE = new TypeToken<List<BranchInfo>>() {}.getType();
    public BranchInfoParser(Gson gson) {
        this.gson = gson;
    }

    public List<BranchInfo> parseBranchInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(gson.fromJson(result, BranchInfo.class));
        } 
        return gson.fromJson(result, TYPE);
    }
}
