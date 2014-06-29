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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Thomas Forrer
 */
public class CommentsParser {
    private final Gson gson;

    public CommentsParser(Gson gson) {
        this.gson = gson;
    }

    public TreeMap<String, Set<CommentInfo>> parseCommentInfos(JsonElement result) {
        TreeMap<String, Set<CommentInfo>> commentInfos = Maps.newTreeMap();
        JsonObject jsonObject = result.getAsJsonObject();

        for (Map.Entry<String, JsonElement> element : jsonObject.entrySet()) {
            Set<CommentInfo> currentCommentInfos = Sets.newLinkedHashSet();

            for (JsonElement jsonElement : element.getValue().getAsJsonArray()) {
                currentCommentInfos.add(parseSingleCommentInfos(jsonElement.getAsJsonObject()));
            }

            commentInfos.put(element.getKey(), currentCommentInfos);
        }
        return commentInfos;
    }

    private CommentInfo parseSingleCommentInfos(JsonObject result) {
        return gson.fromJson(result, CommentInfo.class);
    }
}
