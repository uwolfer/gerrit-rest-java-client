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

import static com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest.restContext;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.api.projects.TagInput;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Pavel Bely
 */
public class TagApiRestClientTest {
    public static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();
    public static final TagInfo MOCK_TAG_INFO = EasyMock.createMock(TagInfo.class);

    @Test
    public void testCreateTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/tags/some-tag", "{}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(restContext(gerritRestClient));

        projectsRestClient.name("sandbox").tag("some-tag").create(new TagInput());
    }

    @Test
    public void testGetTagsForProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags?n=3&s=1",
                JsonParser.parseString("[{\"ref\":\"a\"},{\"ref\":\"b\"},{\"ref\":\"c\"}]"))
            .get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(restContext(gerritRestClient), projectName);

        List<TagInfo> tags = projectApiRestClient.tags()
            .withLimit(3)
            .withStart(1)
            .get();
        Truth.assertThat(tags).hasSize(3);
    }

    @Test
    public void testGetTagForProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags/v0.0.1",
                JsonParser.parseString("{\"ref\":\"refs/tags/v0.0.1\"}"))
            .get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(restContext(gerritRestClient), projectName);

        TagInfo tag = projectApiRestClient.tag("v0.0.1")
            .get();

        Truth.assertThat(tag.ref).isEqualTo("refs/tags/v0.0.1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testDeleteTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/projects/sandbox/tags/some-tag")
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(restContext(gerritRestClient));

        projectsRestClient.name("sandbox").tag("some-tag").delete();
    }
}
