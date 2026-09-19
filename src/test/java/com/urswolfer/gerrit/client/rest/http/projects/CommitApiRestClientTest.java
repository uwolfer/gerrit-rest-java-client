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
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class CommitApiRestClientTest {
    @Test
    public void testGet() throws Exception {
        String projectName = "sandbox";
        String commitSha = "49f9e84661d06814c1e9fe3d724f8fffb51b60f4";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/commits/49f9e84661d06814c1e9fe3d724f8fffb51b60f4", JsonParser.parseString("{\"commit\":\"abc123\"}"))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        CommitInfo commitInfo = projectsRestClient.name(projectName).commit(commitSha).get();

        Truth.assertThat(commitInfo.commit).isEqualTo("abc123");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIncludedIn() throws Exception {
        String projectName = "sandbox";
        String commitSha = "49f9e84661d06814c1e9fe3d724f8fffb51b60f4";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/commits/49f9e84661d06814c1e9fe3d724f8fffb51b60f4/in", JsonParser.parseString("{\"branches\":[\"master\"]}"))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(restContext(gerritRestClient));

        IncludedInInfo includedInInfo = projectsRestClient.name(projectName).commit(commitSha).includedIn();

        Truth.assertThat(includedInInfo.branches).containsExactly("master");
        EasyMock.verify(gerritRestClient);
    }
}
