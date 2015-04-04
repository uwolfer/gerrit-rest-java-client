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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    public static final ProjectInfo MOCK_PROJECT_INFO = EasyMock.createMock(ProjectInfo.class);
    public static final BranchInfo MOCK_BRANCH_INFO = EasyMock.createMock(BranchInfo.class);

    @Test
    public void testGetProjectInfoForName() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/projects/sandbox", MOCK_JSON_ELEMENT)
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
                .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
                .get();
        ArrayList<BranchInfo> branches = new ArrayList<BranchInfo>();
        branches.add(MOCK_BRANCH_INFO);
        branches.add(MOCK_BRANCH_INFO);
        branches.add(MOCK_BRANCH_INFO);
        
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
                .expectParseBranchInfos(MOCK_JSON_ELEMENT, branches)
                .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser);

        ProjectInfo projectInfo = projectsRestClient.name(projectName).get();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(projectInfo).isEqualTo(MOCK_PROJECT_INFO);
    }
    
    @Test
    public void testGetBranchesForProject() throws Exception {
        String projectName = "sandbox";
        ArrayList<BranchInfo> mockBranches = new ArrayList<BranchInfo>();
        mockBranches.add(MOCK_BRANCH_INFO);
        mockBranches.add(MOCK_BRANCH_INFO);
        mockBranches.add(MOCK_BRANCH_INFO);
    
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/projects/sandbox/branches", MOCK_JSON_ELEMENT)
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
                .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
                .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
                .expectParseBranchInfos(MOCK_JSON_ELEMENT, mockBranches)
                .get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, projectName);
   
        List<BranchInfo> branches = projectApiRestClient.branches().get();
        Truth.assertThat(branches.equals(mockBranches));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetProjectInfoServerException() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/projects/sandbox", new RestApiException())
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser);

        projectsRestClient.name(projectName).get();
        projectsRestClient.name(projectName).branches().get();
    }

    @Test
    public void testCreateProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPut("/projects/sandbox", MOCK_JSON_ELEMENT)
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser);

        projectsRestClient.name(projectName).create();

        EasyMock.verify(gerritRestClient, projectsParser);
    }

    @Test
    public void testCreateProjectWithProjectInput() throws Exception {
        String projectName = "sandbox";
        ProjectInput projectInput = new ProjectInput();
        projectInput.description = "Feel free to play in the sandbox!";
        projectInput.createEmptyCommit = false;
        projectInput.parent = "playingfield";
        projectInput.branches = Lists.newArrayList("master", "releases");

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectPut(
                        "/projects/sandbox",
                        "{\"parent\":\"playingfield\"," +
                                "\"description\":\"Feel free to play in the sandbox!\"," +
                                "\"permissions_only\":false," +
                                "\"create_empty_commit\":false," +
                                "\"branches\":[\"master\",\"releases\"]}",
                        MOCK_JSON_ELEMENT)
                .expectGetGson()
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser);

        projectsRestClient.name(projectName).create(projectInput);

        EasyMock.verify(gerritRestClient, projectsParser);
    }
}
