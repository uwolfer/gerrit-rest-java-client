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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Leonard Brünings
 */
public class ReviewInfoParser {
    private static final Type TYPE = new TypeToken<LinkedHashSet<String>>() {}.getType();

    private final Gson gson;

    public ReviewInfoParser(Gson gson) {
        this.gson = gson;
    }

    public Set<String> parseFileInfos(JsonElement jsonElement) {
        final Set<String> result = gson.fromJson(jsonElement, TYPE);
        // apparently the gerrit api includes a trailing comma
        // see https://gerrit-review.googlesource.com/Documentation/rest-api-changes.html#list-files
        // and gson doesn't want to fix this https://github.com/google/gson/issues/494
        // so just remove null here
        result.remove(null);
        return result;
    }
}
