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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Thomas Forrer
 */
public class ProjectApiRestClientTest {
    public static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testGetProjectInfoForName() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox", JsonParser.parseString("{\"id\":\"p1\"}"))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        ProjectInfo projectInfo = projectsRestClient.name(projectName).get();

        Truth.assertThat(projectInfo.id).isEqualTo("p1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetBranchesForProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/branches?n=5&s=1&m=s&r=.",
                JsonParser.parseString("[{\"ref\":\"a\"},{\"ref\":\"b\"},{\"ref\":\"c\"}]"))
            .get();
        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(restContext(gerritRestClient), projectName);

        List<BranchInfo> branches = projectApiRestClient.branches()
            .withLimit(5).withStart(1).withRegex(".").withSubstring("s")
            .get();
        Truth.assertThat(branches).hasSize(3);
    }

    @Test
    public void testGetProjectConfig() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/config", JsonParser.parseString("{\"description\":\"a project\"}"))
            .get();
        ProjectApiRestClient projectApiRestClient =
            new ProjectApiRestClient(restContext(gerritRestClient), projectName);
        ConfigInfo configInfo = projectApiRestClient.config();

        Truth.assertThat(configInfo.description).isEqualTo("a project");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetProjectConfig() throws Exception {
        String projectName = "sandbox";
        ConfigInfo mockConfigInfo = EasyMock.createMock(ConfigInfo.class);
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/config", "{\"description\":\"foo\"}", JsonParser.parseString("{\"description\":\"a project\"}"))
            .get();
        mockConfigInfo.description= "foo";
        ProjectApiRestClient projectApiRestClient =
            new ProjectApiRestClient(restContext(gerritRestClient), projectName);
        ConfigInput input = new ConfigInput();
        input.description = "foo";
        ConfigInfo configInfo = projectApiRestClient.config(input);

        Truth.assertThat(configInfo.description).isEqualTo("a project");
        EasyMock.verify(gerritRestClient);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetProjectInfoServerException() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox", RestApiException.wrap(null, null))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        projectsRestClient.name(projectName).get();
        projectsRestClient.name(projectName).branches().get();
    }

    @Test
    public void testCreateProject() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox", EMPTY_JSON_OBJECT)
            .get();

        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        projectsRestClient.name(projectName).create();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCreateProjectWithProjectInput() throws Exception {
        String projectName = "sandbox";
        ProjectInput projectInput = new ProjectInput();
        projectInput.description = "Feel free to play in the sandbox!";
        projectInput.createEmptyCommit = false;
        projectInput.parent = "playingfield";
        projectInput.branches = Arrays.asList("master", "releases");

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut(
                "/projects/sandbox",
                "{\"parent\":\"playingfield\"," +
                    "\"description\":\"Feel free to play in the sandbox!\"," +
                    "\"permissions_only\":false," +
                    "\"create_empty_commit\":false," +
                    "\"branches\":[\"master\",\"releases\"]," +
                    "\"init_only\":false}",
                EMPTY_JSON_OBJECT)
            .get();

        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        projectsRestClient.name(projectName).create(projectInput);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testProjectAccess() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/access", JsonParser.parseString("{\"revision\":\"abc123\"}"))
            .get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(restContext(gerritRestClient), projectName);

        ProjectAccessInfo accessInfo = projectApiRestClient.access();

        Truth.assertThat(accessInfo.revision).isEqualTo("abc123");
        EasyMock.verify(gerritRestClient);
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
                , JsonParser.parseString("{\"revision\":\"abc123\"}"))
            .get();

        ProjectApiRestClient projectApiRestClient = new ProjectApiRestClient(restContext(gerritRestClient), projectName);

        ProjectAccessInfo accessInfo = projectApiRestClient.access(projectAccessInput);

        Truth.assertThat(accessInfo.revision).isEqualTo("abc123");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetDescription() throws Exception {
        String description = "foo";
        JsonPrimitive jsonObject = new JsonPrimitive(description);
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/description", jsonObject)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        String response = projectsRestClient.name(projectName).description();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(description);
    }

    @Test
    public void testSetDescription() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/description", "{\"description\":\"Some Description\"}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        DescriptionInput input = new DescriptionInput();
        input.description = "Some Description";
        projectsRestClient.name(projectName).description(input);

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetChildren() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children", JsonParser.parseString("{\"a\":{},\"b\":{},\"c\":{}}"))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
        List<ProjectInfo> result = projectsRestClient.name(projectName).children();
        Truth.assertThat(result).hasSize(3);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetChildrenRecursive() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children?recursive",
                JsonParser.parseString("{\"a\":{},\"b\":{},\"c\":{}}"))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
        List<ProjectInfo> result = projectsRestClient.name(projectName).children(true);
        Truth.assertThat(result).hasSize(3);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testChild() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1", JsonParser.parseString("{\"id\":\"p1\"}"))
            .get();
        ChildProjectApi client =  new ProjectsRestClient(restContext(gerritRestClient)).name("sandbox").child("child1");
        ProjectInfo returned = client.get();
        Truth.assertThat(returned.id).isEqualTo("p1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetHead() throws Exception {
        String head = "refs/heads/master";
        JsonPrimitive jsonObject = new JsonPrimitive(head);
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/HEAD", jsonObject)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        String response = projectsRestClient.name(projectName).head();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(head);
    }

    @Test
    public void testSetHead() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/HEAD", "{\"ref\":\"refs/heads/new\"}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
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
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        String response = projectsRestClient.name(projectName).parent();

        EasyMock.verify(gerritRestClient);
        Truth.assertThat(response).isEqualTo(parent);
    }

    @Test
    public void testSetParent() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/parent", "{\"parent\":\"Parent-project\"}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
        projectsRestClient.name(projectName).parent("Parent-project");

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndex() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/index", "{\"index_children\":true}", EMPTY_JSON_OBJECT)
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
        projectsRestClient.name(projectName).index(true);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIndexChanges() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/index.changes")
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));
        projectsRestClient.name(projectName).indexChanges();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testLabels() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPost("/projects/sandbox/labels",
                "{\"commit_message\":\"some commit\",\"create\":[{\"name\":\"reviews\",\"branches\":[\"master\"]}]}")
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

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
