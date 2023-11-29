/*
 * Copyright 2013-2015 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.gson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Urs Wolfer
 */
public class GsonFactory {

    private GsonFactory() {}

    public static GsonBuilder getBuilder(){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateDeserializer());
        builder.registerTypeAdapter(Date.class, new DateSerializer());
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return builder;
    }

    public static Gson create() {
        return getBuilder().create();
    }

    public static class ImmutableMapStringListAdaptor implements JsonDeserializer<ImmutableMap<String, ImmutableList<String>>> {
        public ImmutableMap<String, ImmutableList<String>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            Map<String, JsonElement> object = json.getAsJsonObject().asMap();
            return object.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey,
                    entry -> {
                        Type listType = new TypeToken<List<String>>() {}.getType();
                        JsonArray jsonArray = entry.getValue().getAsJsonArray();
                        List<String> list = new Gson().fromJson(jsonArray, listType);
                        return ImmutableList.copyOf(list);
                    }
                ));
        }
    }

}
