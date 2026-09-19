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

package com.urswolfer.gerrit.client.rest.contract;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ConfigInfo;
import com.google.gerrit.extensions.api.projects.Projects;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.testng.annotations.Test;

import java.util.List;
import java.util.SortedMap;

/**
 * Pins what {@code gerritApi.projects()} puts on the wire and what it makes of the response.
 *
 * <p>See {@link FakeGerritServer} for why these tests go through the public API instead of mocking
 * the client internals.
 */
public class ProjectsContractTest {

    private static final String PROJECT_PATH = "/projects/my%2Fproject";

    @Test
    public void list() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/", "projects/projects.json");

        SortedMap<String, ProjectInfo> projects = server.api().projects().list().getAsMap();

        Truth.assertThat(projects.keySet()).containsExactly("packages/test", "repo2", "testrepo");
        Truth.assertThat(server.trace()).containsExactly("GET /projects/");
        server.verify();
    }

    @Test
    public void listWithAllParameters() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/?d&t&p=pkg&n=2&S=1&b=master&type=CODE", "projects/projects.json");

        server.api().projects().list()
            .withDescription(true)
            .withTree(true)
            .withPrefix("pkg")
            .withLimit(2)
            .withStart(1)
            .addShowBranch("master")
            .withType(Projects.ListRequest.FilterType.CODE)
            .getAsMap();

        server.verify();
    }

    @Test
    public void nameIsUrlEncoded() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH, "projects/project.json");

        ProjectInfo projectInfo = server.api().projects().name("my/project").get();

        Truth.assertThat(projectInfo).isNotNull();
        Truth.assertThat(server.trace()).containsExactly("GET " + PROJECT_PATH);
        server.verify();
    }

    @Test
    public void branches() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/branches", "projects/branches.json");

        List<BranchInfo> branches = server.api().projects().name("my/project").branches().get();

        Truth.assertThat(branches).hasSize(3);
        Truth.assertThat(branches.get(0).ref).isEqualTo("HEAD");
        server.verify();
    }

    @Test
    public void branchesWithLimitAndSubstring() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/branches?n=2&s=1&m=mas", "projects/branches.json");

        server.api().projects().name("my/project").branches()
            .withLimit(2)
            .withStart(1)
            .withSubstring("mas")
            .get();

        server.verify();
    }

    /**
     * Note that the ref is not URL-encoded, unlike the project name it is appended to.
     */
    @Test
    public void branchRefIsNotUrlEncoded() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/branches/refs/heads/master", "projects/branch.json");

        BranchInfo branchInfo = server.api().projects().name("my/project").branch("refs/heads/master").get();

        Truth.assertThat(branchInfo.ref).isEqualTo("refs/heads/master");
        server.verify();
    }

    @Test
    public void tags() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/tags", "projects/tags.json");

        List<TagInfo> tags = server.api().projects().name("my/project").tags().get();

        Truth.assertThat(tags).hasSize(3);
        server.verify();
    }

    /**
     * {@code ConfigInfo} is the one response that needs a Gson instance of its own, for its
     * {@code ImmutableMap} fields.
     */
    @Test
    public void config() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/config", "projects/configInfo.json");

        ConfigInfo configInfo = server.api().projects().name("my/project").config();

        Truth.assertThat(configInfo).isNotNull();
        server.verify();
    }

    @Test
    public void children() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", PROJECT_PATH + "/children?recursive", "projects/projects.json");

        List<ProjectInfo> children = server.api().projects().name("my/project").children(true);

        Truth.assertThat(children).hasSize(3);
        server.verify();
    }

    @Test
    public void create() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("PUT", PROJECT_PATH, "projects/project.json");

        server.api().projects().create("my/project");

        Truth.assertThat(server.request("PUT", PROJECT_PATH).getBody())
            .isEqualTo("{\"name\":\"my/project\",\"permissions_only\":false,"
                + "\"create_empty_commit\":false,\"init_only\":false}");
        server.verify();
    }
}
