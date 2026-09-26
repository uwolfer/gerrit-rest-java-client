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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Pins the conversion behaviour the per-entity parsers used to carry, in particular the two places
 * where collapsing them into one class widened what is accepted.
 */
public class GerritJsonTest {

    private final GerritJson gerritJson = new GerritJson(GsonFactory.create());

    @Test
    public void asListReadsAnArray() {
        List<BranchInfo> branches = gerritJson.asList(json("[{\"ref\":\"a\"},{\"ref\":\"b\"}]"), BranchInfo.class);

        Truth.assertThat(branches).hasSize(2);
        Truth.assertThat(branches.get(0).ref).isEqualTo("a");
        Truth.assertThat(branches.get(1).ref).isEqualTo("b");
    }

    /**
     * Gerrit returns a bare object from several endpoints that document a list. Six parsers each
     * carried their own copy of this; it now applies to every list.
     */
    @Test
    public void asListWrapsABareObject() {
        List<BranchInfo> branches = gerritJson.asList(json("{\"ref\":\"a\"}"), BranchInfo.class);

        Truth.assertThat(branches).hasSize(1);
        Truth.assertThat(branches.get(0).ref).isEqualTo("a");
    }

    /**
     * A JSON null is not an array, so it takes the bare-object path and converts to a null element -
     * which is what the parsers this replaces did too.
     */
    @Test
    public void asListOfJsonNullIsASingleNullElement() {
        List<BranchInfo> branches = gerritJson.asList(JsonNull.INSTANCE, BranchInfo.class);

        Truth.assertThat(branches).hasSize(1);
        Truth.assertThat(branches.get(0)).isNull();
    }

    /**
     * Gerrit emits a trailing comma in some lists, which Gson turns into a null element. It is kept
     * here; the one endpoint that must not see it strips it itself.
     *
     * @see com.urswolfer.gerrit.client.rest.http.changes.RevisionApiRestClient
     */
    @Test
    public void asListKeepsANullElementInsideAnArray() {
        List<BranchInfo> branches = gerritJson.asList(json("[{\"ref\":\"a\"},null]"), BranchInfo.class);

        Truth.assertThat(branches).hasSize(2);
        Truth.assertThat(branches.get(0).ref).isEqualTo("a");
        Truth.assertThat(branches.get(1)).isNull();
    }

    /**
     * No response body at all. The parsers this replaces threw {@link NullPointerException} here.
     */
    @Test
    public void asListOfNothingIsNull() {
        Truth.assertThat(gerritJson.asList(null, BranchInfo.class)).isNull();
    }

    @Test
    public void collectionsOfNothingAreNull() {
        Truth.assertThat(gerritJson.as(null, BranchInfo.class)).isNull();
        Truth.assertThat(gerritJson.asMap(null, BranchInfo.class)).isNull();
        Truth.assertThat(gerritJson.asSortedMap(null, BranchInfo.class)).isNull();
        Truth.assertThat(gerritJson.asSortedMapOfLists(null, BranchInfo.class)).isNull();
        Truth.assertThat(gerritJson.asSet(null, String.class)).isNull();
        Truth.assertThat(gerritJson.asSortedSet(null, String.class)).isNull();
    }

    @Test
    public void asMapKeepsTheOrderOfTheResponse() {
        Map<String, BranchInfo> branches =
            gerritJson.asMap(json("{\"b\":{\"ref\":\"b\"},\"a\":{\"ref\":\"a\"}}"), BranchInfo.class);

        Truth.assertThat(branches.keySet()).containsExactly("b", "a").inOrder();
    }

    @Test
    public void asSortedMapOrdersByKey() {
        SortedMap<String, BranchInfo> branches =
            gerritJson.asSortedMap(json("{\"b\":{\"ref\":\"b\"},\"a\":{\"ref\":\"a\"}}"), BranchInfo.class);

        Truth.assertThat(branches.keySet()).containsExactly("a", "b").inOrder();
    }

    @Test
    public void asSortedMapOfListsOrdersByKeyAndReadsTheLists() {
        SortedMap<String, List<BranchInfo>> branches =
            gerritJson.asSortedMapOfLists(json("{\"b\":[{\"ref\":\"b\"}],\"a\":[{\"ref\":\"a\"}]}"), BranchInfo.class);

        Truth.assertThat(branches.keySet()).containsExactly("a", "b").inOrder();
        Truth.assertThat(branches.get("a").get(0).ref).isEqualTo("a");
    }

    @Test
    public void asSetKeepsTheOrderOfTheResponse() {
        Set<String> values = gerritJson.asSet(json("[\"b\",\"a\"]"), String.class);

        Truth.assertThat(values).containsExactly("b", "a").inOrder();
    }

    @Test
    public void asSortedSetOrdersNaturally() {
        SortedSet<String> values = gerritJson.asSortedSet(json("[\"b\",\"a\"]"), String.class);

        Truth.assertThat(values).containsExactly("a", "b").inOrder();
    }

    @Test
    public void toJsonWritesTheFieldsOfTheDeclaredType() {
        ProjectInput input = new ProjectInput();
        input.name = "my/project";

        Truth.assertThat(gerritJson.toJson(input, ProjectInput.class)).isEqualTo(
            "{\"name\":\"my/project\",\"permissions_only\":false,"
                + "\"create_empty_commit\":false,\"init_only\":false}");
    }

    private static JsonElement json(String raw) {
        return JsonParser.parseString(raw);
    }
}
