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
import com.google.gerrit.extensions.api.access.AccessSectionInfo;
import com.google.gerrit.extensions.api.access.PermissionInfo;
import com.google.gerrit.extensions.api.access.PermissionRuleInfo;
import com.google.gerrit.extensions.api.access.PermissionRuleInfo.Action;
import com.google.gerrit.extensions.api.access.ProjectAccessInfo;
import com.google.gerrit.extensions.api.access.ProjectAccessInput;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    public static final ProjectInfo MOCK_PROJECT_INFO = EasyMock.createMock(ProjectInfo.class);
    public static final BranchInfo MOCK_BRANCH_INFO = EasyMock.createMock(BranchInfo.class);
    public static final ProjectAccessInfo MOCK_PROJECT_ACCESS_INFO = EasyMock.createMock(ProjectAccessInfo.class);

    @Test
    public void testGetProjectInfoForName() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/projects/sandbox", MOCK_JSON_ELEMENT)
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
                .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
                .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder().get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser);

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
                .expectGet("/projects/sandbox/branches?n=5&s=1&m=s&r=.", MOCK_JSON_ELEMENT)
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
                .expectParseSingleProjectInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_INFO)
                .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
                .expectParseBranchInfos(MOCK_JSON_ELEMENT, mockBranches)
                .get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectName);

        List<BranchInfo> branches = projectApiRestClient.branches()
            .withLimit(5).withStart(1).withRegex(".").withSubstring("s")
            .get();
        Truth.assertThat(branches).isEqualTo(mockBranches);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetProjectInfoServerException() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/projects/sandbox", RestApiException.wrap(null, null))
                .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder().get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser);

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
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser);

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
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser);

        projectsRestClient.name(projectName).create(projectInput);

        EasyMock.verify(gerritRestClient, projectsParser);
    }

    @Test
    public void testProjectAccess() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/access", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseProjectAccessInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_ACCESS_INFO)
            .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
            .get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectName);

        ProjectAccessInfo accessInfo = projectApiRestClient.access();

        Truth.assertThat(accessInfo).isEqualTo(MOCK_PROJECT_ACCESS_INFO);
        EasyMock.verify(gerritRestClient, projectsParser);
    }

    @Test
    public void testUpdateProjectAccess() throws Exception {
        String projectName = "sandbox";
        String requestBody = "{\"add\":{\"refs/*\":"
            + "{\"permissions\":"
            + "{\"read\":{\"label\":\"\",\"exclusive\":false,\"rules\":"
            + "{\"d064e6028af64945c9512108a9a4f5bde6baaebf\":"
            + "{\"action\":\"ALLOW\",\"force\":false}}}}}}}";
        ProjectAccessInput projectAccessInput = new ProjectAccessInput();
        AccessSectionInfo accessSectionInfo = new AccessSectionInfo();
        PermissionInfo permissionInfo = new PermissionInfo("", false);
        projectAccessInput.add = new HashMap<String, AccessSectionInfo>();
        permissionInfo.rules = new HashMap<String, PermissionRuleInfo>();
        accessSectionInfo.permissions = new HashMap<String, PermissionInfo>();
        permissionInfo.rules.put("d064e6028af64945c9512108a9a4f5bde6baaebf", new PermissionRuleInfo(Action.ALLOW, false));
        accessSectionInfo.permissions.put("read", permissionInfo);
        projectAccessInput.add.put("refs/*", accessSectionInfo);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/access"
                , requestBody
                , MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseProjectAccessInfo(MOCK_JSON_ELEMENT, MOCK_PROJECT_ACCESS_INFO)
            .expectParseProjectAccessInput(requestBody, projectAccessInput)
            .get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
            .get();
        TagInfoParser tagInfoParser = new TagInfoParserBuilder().get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectName);

        ProjectAccessInfo accessInfo = projectApiRestClient.access(projectAccessInput);

        Truth.assertThat(accessInfo).isEqualTo(MOCK_PROJECT_ACCESS_INFO);
        EasyMock.verify(gerritRestClient, projectsParser);
    }
}
