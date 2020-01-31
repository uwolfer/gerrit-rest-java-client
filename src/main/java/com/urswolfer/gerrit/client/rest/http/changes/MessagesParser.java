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
import com.google.gerrit.extensions.common.ChangeMessageInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author EFregnan
 */
public class MessagesParser {

    private static final Type TYPE = new TypeToken<List<ChangeMessageInfo>>() {}.getType();

    private final Gson gson;

    public MessagesParser(Gson gson) {
        this.gson = gson;
    }

    public List<ChangeMessageInfo> parseChangeMessageInfos(JsonElement result) {
        return gson.fromJson(result, TYPE);
    }

    public ChangeMessageInfo parseSingleChangeMessageInfo(JsonObject result) {
        return gson.fromJson(result, ChangeMessageInfo.class);
    }
}
