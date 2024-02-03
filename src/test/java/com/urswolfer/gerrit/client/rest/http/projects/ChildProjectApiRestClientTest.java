/*
 * Copyright 2013-2024 Urs Wolfer
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
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;


public class ChildProjectApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    public static final ProjectInfo MOCK_PROJECT_INFO = EasyMock.createMock(ProjectInfo.class);


    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);
        EasyMock.expect(projectsParser.parseSingleProjectInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_PROJECT_INFO)
            .once();
        EasyMock.replay(projectsParser);
        ChildProjectApiRestClient client = new ChildProjectApiRestClient(gerritRestClient,
            projectsParser, "/projects/sandbox", "child1");
        ProjectInfo returned = client.get();

        Truth.assertThat(returned).isEqualTo(MOCK_PROJECT_INFO);
        EasyMock.verify(gerritRestClient,projectsParser);
    }

    @Test
    public void testGetRecursive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1?recursive", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);
        EasyMock.expect(projectsParser.parseSingleProjectInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_PROJECT_INFO)
            .once();
        EasyMock.replay(projectsParser);
        ChildProjectApiRestClient client = new ChildProjectApiRestClient(gerritRestClient,
            projectsParser, "/projects/sandbox", "child1");
        ProjectInfo returned = client.get(true);

        Truth.assertThat(returned).isEqualTo(MOCK_PROJECT_INFO);
        EasyMock.verify(gerritRestClient,projectsParser);
    }
}
