/*
 * Copyright 2013-2015 Urs Wolfer
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
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.BranchInput;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

/**
 * @author Urs Wolfer
 */
public class BranchApiRestClientTest {

    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final BranchInfo MOCK_BRANCH_INFO = EasyMock.createMock(BranchInfo.class);
    private static final String FILE_CONTENT = "some new changes";
    private static final String FILE_PATH = "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java";

    private ProjectApiRestClient projectApiRestClient;

    @Test
    public void testCreateBranch() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/projects/sandbox/branches/some-feature", "{}", MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null, null);

        projectsRestClient.name("sandbox").branch("some-feature").create(new BranchInput());
    }

    @Test
    public void testGetBranchInfoForName() throws Exception {
        String projectName = "sandbox";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/branches/master", MOCK_JSON_ELEMENT)
            .get();
        ProjectsParser projectsParser = new ProjectsParserBuilder().get();
        BranchInfoParser branchInfoParser = new BranchInfoParserBuilder()
            .expectParseBranchInfos(MOCK_JSON_ELEMENT, Collections.singletonList(MOCK_BRANCH_INFO))
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, projectsParser, branchInfoParser, null);

        BranchInfo branchInfo = projectsRestClient.name(projectName).branch("master").get();

        EasyMock.verify(gerritRestClient, projectsParser);
        Truth.assertThat(branchInfo).isEqualTo(MOCK_BRANCH_INFO);
    }

    @Test
    public void testDeleteBranch() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/projects/sandbox/branches/some-feature")
            .get();
        ProjectsRestClient projectsRestClient = new ProjectsRestClient(gerritRestClient, null, null, null);

        projectsRestClient.name("sandbox").branch("some-feature").delete();
    }

    @Test
    public void testFileContent() throws Exception {
        String requestUrl = getBaseRequestUrl() + "/content";
        String base64String = Base64.encodeBase64String(FILE_CONTENT.getBytes("UTF-8"));
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(base64String.getBytes("UTF-8")));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "base64"));
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "text/plain"));
        EasyMock.replay(httpEntity, httpResponse);

        setupServices();

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GerritRestClient.HttpVerb.GET, httpResponse)
            .get();

        BranchApiRestClient branchApiRestClient = new BranchApiRestClient(gerritRestClient, null, projectApiRestClient, "master");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = branchApiRestClient.file(FILE_PATH);
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));

            Truth.assertThat(actualContent).isEqualTo(FILE_CONTENT);
            Truth.assertThat(binaryResult.isBase64()).isTrue();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("text/plain");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    private String getBaseRequestUrl() {
        String filePathEncoded = Url.encode(FILE_PATH);
        return "/projects/sandbox/branches/master/files/" + filePathEncoded;
    }

    public void setupServices() {
        projectApiRestClient = EasyMock.createMock(ProjectApiRestClient.class);
        EasyMock.expect(projectApiRestClient.projectsUrl())
            .andReturn("/projects/sandbox").once();
        EasyMock.replay(projectApiRestClient);
    }
}
