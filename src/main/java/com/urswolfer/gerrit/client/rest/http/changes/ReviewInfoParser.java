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

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Leonard Brünings
 */
public class ReviewInfoParser {
    private static final Type TYPE = new TypeToken<List<String>>() {}.getType();

    private final Gson gson;

    public ReviewInfoParser(Gson gson) {
        this.gson = gson;
    }

    public List<String> parseFileInfos(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, TYPE);
    }

}
