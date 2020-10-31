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
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.TreeMap;

/**
 * @author EFregnan
 */
public class ActionInfoParser {

    private static final Type ACTION_TYPE = new TypeToken<TreeMap<String, ActionInfo>>() {}.getType();

    private final Gson gson;

    public ActionInfoParser(Gson gson) {
        this.gson = gson;
    }

    public TreeMap<String, ActionInfo> parseActionInfos(JsonElement result) {
        return gson.fromJson(result, ACTION_TYPE);
    }
}
