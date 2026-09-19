/*
 * Copyright 2013-2026 Urs Wolfer
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Converts between Gerrit's JSON and the {@code com.google.gerrit.extensions} types.
 *
 * <p>This replaces the per-entity parser classes this client used to have. Those were almost
 * entirely {@code gson.fromJson(json, SOME_TYPE)} one-liners, and every one of them had to be
 * constructed and threaded through the REST client that needed it, which is why
 * {@code ChangeApiRestClient} once took thirteen constructor arguments. The collection shapes they
 * encoded are expressed here as the element type instead, so a new endpoint needs no new class.
 *
 * <p>The collection methods pick the same implementations the parsers did, because Gerrit's
 * responses and this client's public signatures depend on them: insertion order for
 * {@link #asMap} and {@link #asSet}, key order for {@link #asSortedMap}, {@link #asSortedSet} and
 * {@link #asSortedMapOfLists}.
 */
public class GerritJson {

    private final Gson gson;

    public GerritJson(Gson gson) {
        this.gson = gson;
    }

    public <T> T as(JsonElement json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * Reads a type a {@link Class} cannot express, such as a parameterized one.
     */
    public <T> T as(JsonElement json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * Reads a list, tolerating a bare object in place of a single-element array.
     *
     * <p>Gerrit returns a bare object from several endpoints that document a list, so the parsers
     * this replaces normalized that - each in its own copy of the same three lines. Applying it
     * everywhere is a widening: an endpoint that always returns an array is unaffected, and one
     * that returns an object now yields a single-element list where it used to throw.
     */
    public <T> List<T> asList(JsonElement json, Class<T> type) {
        if (json != null && !json.isJsonArray()) {
            return Collections.singletonList(as(json, type));
        }
        return gson.fromJson(json, parameterized(List.class, type));
    }

    /**
     * Reads an object as a map, keeping the order the fields appear in the response.
     */
    public <T> Map<String, T> asMap(JsonElement json, Class<T> valueType) {
        return gson.fromJson(json, parameterized(LinkedHashMap.class, String.class, valueType));
    }

    /**
     * Reads an object as a map ordered by key.
     */
    public <T> SortedMap<String, T> asSortedMap(JsonElement json, Class<T> valueType) {
        return gson.fromJson(json, parameterized(TreeMap.class, String.class, valueType));
    }

    /**
     * Reads an object whose values are lists - how Gerrit returns comments, keyed by file path -
     * as a map ordered by key.
     */
    public <T> SortedMap<String, List<T>> asSortedMapOfLists(JsonElement json, Class<T> valueType) {
        Type listType = parameterized(List.class, valueType);
        return gson.fromJson(json, parameterized(TreeMap.class, String.class, listType));
    }

    /**
     * Reads an array as a set, keeping the order of the response.
     */
    public <T> Set<T> asSet(JsonElement json, Class<T> type) {
        return gson.fromJson(json, parameterized(LinkedHashSet.class, type));
    }

    /**
     * Reads an array as a set ordered by its natural ordering.
     */
    public <T> SortedSet<T> asSortedSet(JsonElement json, Class<T> type) {
        return gson.fromJson(json, parameterized(TreeSet.class, type));
    }

    public String toJson(Object value) {
        return gson.toJson(value);
    }

    /**
     * Serializes {@code value} as {@code type}, so that only the fields declared on that type are
     * written even when a subclass instance is passed.
     */
    public String toJson(Object value, Type type) {
        return gson.toJson(value, type);
    }

    private static Type parameterized(Class<?> rawType, Type... typeArguments) {
        return TypeToken.getParameterized(rawType, typeArguments).getType();
    }
}
