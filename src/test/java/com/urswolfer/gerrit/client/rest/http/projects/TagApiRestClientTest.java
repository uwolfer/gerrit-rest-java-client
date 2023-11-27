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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.api.projects.TagInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import com.urswolfer.gerrit.client.rest.http.projects.parsers.ProjectCommitInfoParser;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Bely
 */
public class TagApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    public static final ProjectInfo MOCK_PROJECT_INFO = EasyMock.createMock(ProjectInfo.class);
    public static final TagInfo MOCK_TAG_INFO = EasyMock.createMock(TagInfo.class);

    @Test
    public void testCreateTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/tags/some-tag", "{}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(gerritRestClient, null, null, null, null);

        projectsRestClient.name("sandbox").tag("some-tag").create(new TagInput());
    }

    @Test
    public void testGetTagsForProject() throws Exception {
        String projectName = "sandbox";
        ArrayList<TagInfo> mockTags = Lists.newArrayList();
        mockTags.add(MOCK_TAG_INFO);
        mockTags.add(MOCK_TAG_INFO);
        mockTags.add(MOCK_TAG_INFO);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags?n=3&s=1", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
            .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
            .get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder()
            .expectParseTagInfos(MOCK_JSON_ELEMENT, mockTags)
            .get();
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser, projectName);

        List<TagInfo> tags = projectApiRestClient.tags()
            .withLimit(3)
            .withStart(1)
            .get();
        Truth.assertThat(tags).isEqualTo(mockTags);
    }

    @Test
    public void testGetTagForProject() throws Exception {
        String projectName = "sandbox";
        ArrayList<TagInfo> mockTags = Lists.newArrayList();
        mockTags.add(MOCK_TAG_INFO);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/tags/v0.0.1", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
            .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
            .get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder()
            .expectParseTagInfos(MOCK_JSON_ELEMENT, mockTags)
            .get();
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser, projectName);

        TagInfo tags = projectApiRestClient.tag("v0.0.1")
            .get();
        Truth.assertThat(tags).isEqualTo(MOCK_TAG_INFO);
    }

    @Test
    public void testDeleteTag() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/projects/sandbox/tags/some-tag")
            .get();
        ProjectsRestClient projectsRestClient =
            new ProjectsRestClient(gerritRestClient, null, null, null, null);

        projectsRestClient.name("sandbox").tag("some-tag").delete();
    }
}
