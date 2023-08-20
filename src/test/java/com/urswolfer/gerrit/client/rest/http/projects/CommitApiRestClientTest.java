/*
 * Copyright 2013-2021 Urs Wolfer
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
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import com.urswolfer.gerrit.client.rest.http.projects.parsers.ProjectCommitInfoParser;
import org.easymock.EasyMock;
import org.testng.annotations.Test;


public class CommitApiRestClientTest {

    public static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final CommitInfo MOCK_COMMIT_INFO = EasyMock.createMock(CommitInfo.class);
    private static final IncludedInInfo MOCK_INCLUDED_IN_INFO = EasyMock.createMock(IncludedInInfo.class);

    @Test
    public void testGet() throws Exception {
        String projectName = "sandbox";
        String commitSha = "49f9e84661d06814c1e9fe3d724f8fffb51b60f4";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/commits/49f9e84661d06814c1e9fe3d724f8fffb51b60f4", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder()
            .expectParseCommitInfo(MOCK_JSON_ELEMENT, MOCK_COMMIT_INFO).get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, null, null, projectCommitInfoParser);

        CommitInfo commitInfo = projectsRestClient.name(projectName).commit(commitSha).get();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(commitInfo).isEqualTo(MOCK_COMMIT_INFO);
    }

    @Test
    public void testIncludedIn() throws Exception {
        String projectName = "sandbox";
        String commitSha = "49f9e84661d06814c1e9fe3d724f8fffb51b60f4";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/commits/49f9e84661d06814c1e9fe3d724f8fffb51b60f4/in", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParserBuilder()
            .expectParseIncludedIn(MOCK_JSON_ELEMENT, MOCK_INCLUDED_IN_INFO).get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, null, null, projectCommitInfoParser);

        IncludedInInfo includedInInfo = projectsRestClient.name(projectName).commit(commitSha).includedIn();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(includedInInfo).isEqualTo(MOCK_INCLUDED_IN_INFO);
    }
}
