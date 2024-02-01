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
import com.google.gerrit.extensions.api.projects.ChildProjectApi;
import com.google.gerrit.extensions.api.projects.ConfigInfo;
import com.google.gerrit.extensions.api.projects.ConfigInput;
import com.google.gerrit.extensions.api.projects.DescriptionInput;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.BatchLabelInput;
import com.google.gerrit.extensions.common.LabelDefinitionInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import com.urswolfer.gerrit.client.rest.http.projects.parsers.ProjectCommitInfoParser;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser);

        ProjectInfo projectInfo = projectsRestClient.name(projectName).get();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(projectInfo).isEqualTo(MOCK_PROJECT_INFO);
    }

    @Test
    public void testGetBranchesForProject() throws Exception {
        String projectName = "sandbox";
        ArrayList<BranchInfo> mockBranches = new ArrayList<>();
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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser, projectName);

        List<BranchInfo> branches = projectApiRestClient.branches()
            .withLimit(5).withStart(1).withRegex(".").withSubstring("s")
            .get();
        Truth.assertThat(branches).isEqualTo(mockBranches);
    }

    @Test
    public void testGetProjectConfig() throws Exception {
        String projectName = "sandbox";
        ConfigInfo mockConfigInfo = EasyMock.createMock(ConfigInfo.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/config", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseProjectConfigInfo(MOCK_JSON_ELEMENT, mockConfigInfo)
            .get();
        ProjectApiRestClient projectApiRestClient =
            new ProjectApiRestClient(gerritRestClient, projectsParser, null, null, null, projectName);
        ConfigInfo configInfo = projectApiRestClient.config();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(configInfo).isEqualTo(mockConfigInfo);
    }

    @Test
    public void testSetProjectConfig() throws Exception {
        String projectName = "sandbox";
        ConfigInfo mockConfigInfo = EasyMock.createMock(ConfigInfo.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/config", "{\"description\":\"foo\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        mockConfigInfo.description= "foo";
        ProjectsParser projectsParser = new ProjectsParserBuilder()
            .expectParseProjectConfigInfo(MOCK_JSON_ELEMENT, mockConfigInfo)
            .get();
        ProjectApiRestClient projectApiRestClient =
            new ProjectApiRestClient(gerritRestClient, projectsParser, null, null, null, projectName);
        ConfigInput input = new ConfigInput();
        input.description = "foo";
        ConfigInfo configInfo = projectApiRestClient.config(input);

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(configInfo).isEqualTo(mockConfigInfo);
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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser);

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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();

        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser);

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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();

        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser);

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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser, projectName);

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
        projectAccessInput.add = new HashMap<>();
        permissionInfo.rules = new HashMap<>();
        accessSectionInfo.permissions = new HashMap<>();
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
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder().get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(gerritRestClient, projectsParser, branchInfoParser, tagInfoParser, projectCommitInfoParser
            , projectName);

        ProjectAccessInfo accessInfo = projectApiRestClient.access(projectAccessInput);

        Truth.assertThat(accessInfo).isEqualTo(MOCK_PROJECT_ACCESS_INFO);
        EasyMock.verify(gerritRestClient, projectsParser);
    }

    @Test
    public void testGetDescription() throws Exception {
        String description = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(description);
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/description", jsonObject)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);

        String response = projectsRestClient.name(projectName).description();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(description);
    }

    @Test
    public void testSetDescription() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/description", "{\"description\":\"Some Description\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);

        DescriptionInput input = new DescriptionInput();
        input.description = "Some Description";
        projectsRestClient.name(projectName).description(input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetChildren() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);
        List<ProjectInfo> expected = new ArrayList<>(Collections.emptyList());
        expected.add(EasyMock.createMock(ProjectInfo.class));
        expected.add(EasyMock.createMock(ProjectInfo.class));
        expected.add(EasyMock.createMock(ProjectInfo.class));
        EasyMock.expect(projectsParser.parseProjectInfosList(MOCK_JSON_ELEMENT))
            .andReturn(expected)
            .once();
        EasyMock.replay(projectsParser);;
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, null,
            null, null);
        List<ProjectInfo> result = projectsRestClient.name(projectName).children();
        Truth.assertThat(result).isEqualTo(expected);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetChildrenRecursive() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children?recursive", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);
        List<ProjectInfo> expected = new ArrayList<>(Collections.emptyList());
        expected.add(EasyMock.createMock(ProjectInfo.class));
        expected.add(EasyMock.createMock(ProjectInfo.class));
        expected.add(EasyMock.createMock(ProjectInfo.class));
        EasyMock.expect(projectsParser.parseProjectInfosList(MOCK_JSON_ELEMENT))
            .andReturn(expected)
            .once();
        EasyMock.replay(projectsParser);;
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, null,
            null, null);
        List<ProjectInfo> result = projectsRestClient.name(projectName).children(true);
        Truth.assertThat(result).isEqualTo(expected);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChild() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);
        EasyMock.expect(projectsParser.parseSingleProjectInfo(MOCK_JSON_ELEMENT))
            .andReturn(MOCK_PROJECT_INFO)
            .once();
        EasyMock.replay(projectsParser);
        ChildProjectApi client =  new ProjectsRestClient(gerritRestClient, projectsParser, null,
            null, null).name("sandbox").child("child1");
        ProjectInfo returned = client.get();
        Truth.assertThat(returned).isEqualTo(MOCK_PROJECT_INFO);
        EasyMock.verify(gerritRestClient,projectsParser);
    }

    @Test
    public void testGetHead() throws Exception {
        String head = "refs/heads/master";
        JsonPrimitive jsonObject = new JsonPrimitive(head);
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/HEAD", jsonObject)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);

        String response = projectsRestClient.name(projectName).head();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(head);
    }

    @Test
    public void testSetHead() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/HEAD", "{\"ref\":\"refs/heads/new\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);
        projectsRestClient.name(projectName).head("refs/heads/new");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetParent() throws Exception {
        String parent = "All-Projects";
        JsonPrimitive jsonObject = new JsonPrimitive(parent);
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/parent", jsonObject)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);

        String response = projectsRestClient.name(projectName).parent();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(parent);
    }

    @Test
    public void testSetParent() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/parent", "{\"parent\":\"Parent-project\"}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);
        projectsRestClient.name(projectName).parent("Parent-project");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndex() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/index", "{\"index_children\":true}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);
        projectsRestClient.name(projectName).index(true);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndexChanges() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/index.changes")
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);
        projectsRestClient.name(projectName).indexChanges();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testLabels() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/labels",
                "{\"commit_message\":\"some commit\",\"create\":[{\"name\":\"reviews\",\"branches\":[\"master\"]}]}")
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null,
            null, null);

        BatchLabelInput input = new BatchLabelInput();
        input.commitMessage = "some commit";
        LabelDefinitionInput labelDefinitionInput = new LabelDefinitionInput();
        labelDefinitionInput.name = "reviews";
        labelDefinitionInput.branches = Collections.singletonList("master");
        input.create = Collections.singletonList(labelDefinitionInput);
        projectsRestClient.name(projectName).labels(input);
        EasyMock.verify(gerritRestClient);
    }
}
