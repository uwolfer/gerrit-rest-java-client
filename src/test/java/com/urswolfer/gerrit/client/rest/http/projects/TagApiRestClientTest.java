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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.api.projects.TagInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

/**
 * @author Pavel Bely
 */
public class TagApiRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    public static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();
    public static final TagInfo MOCK_TAG_INFO = EasyMock.createMock(TagInfo.class);

    @Test
    public void testCreateTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/tags/some-tag", "{}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(gerritRestClient, gerritJson);

        projectsRestClient.name("sandbox").tag("some-tag").create(new TagInput());
    }

    @Test
    public void testGetTagsForProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags?n=3&s=1",
                new JsonParser().parse("[{\"ref\":\"a\"},{\"ref\":\"b\"},{\"ref\":\"c\"}]"))
            .get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, gerritJson, projectName);

        List<TagInfo> tags = projectApiRestClient.tags()
            .withLimit(3)
            .withStart(1)
            .get();
        Truth.assertThat(tags).hasSize(3);
    }

    @Test
    public void testGetTagForProject() throws Exception {
        String projectName = "sandbox";
        ArrayList<TagInfo> mockTags = new ArrayList<>();
        mockTags.add(MOCK_TAG_INFO);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags/v0.0.1", EMPTY_JSON_OBJECT)
            .get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, gerritJson, projectName);

        TagInfo tags = projectApiRestClient.tag("v0.0.1")
            .get();
    }

    @Test
    public void testDeleteTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/projects/sandbox/tags/some-tag")
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(gerritRestClient, gerritJson);

        projectsRestClient.name("sandbox").tag("some-tag").delete();
    }
}
