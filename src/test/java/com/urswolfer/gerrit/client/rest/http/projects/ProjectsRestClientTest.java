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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.ProjectApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.api.projects.Projects;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.LogicalOperator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * @author Thomas Forrer
 */
public class ProjectsRestClientTest {
    @Test
    public void testListProjects() throws Exception {
        ProjectListTestCase testCase = listTestCase().expectUrl("/projects/");
        testCase.execute().verify();
    }

    @Test(dataProvider = "ListProjectTestCases")
    public void testListProjectsWithParameter(ProjectListTestCase testCase) throws Exception {
        testCase.execute().verify();
    }

    @DataProvider(name = "ListProjectTestCases")
    public Iterator<ProjectListTestCase[]> listProjectTestCases() throws Exception {
        return Iterables.transform(Arrays.asList(
                listTestCase().withListParameter(
                        new TestListRequest().withDescription(true)
                ).expectUrl("/projects/?d"),
                listTestCase().withListParameter(
                        new TestListRequest().withDescription(false)
                ).expectUrl("/projects/"),
                listTestCase().withListParameter(
                        new TestListRequest().withLimit(10)
                ).expectUrl("/projects/?n=10"),
                listTestCase().withListParameter(
                        new TestListRequest().withPrefix("test")
                ).expectUrl("/projects/?p=test"),
                listTestCase().withListParameter(
                        new TestListRequest().withStart(5)
                ).expectUrl("/projects/?S=5"),
                listTestCase().withListParameter(
                        new TestListRequest()
                                .withDescription(true)
                                .withLimit(15)
                                .withStart(10)
                                .withPrefix("master")
                ).expectUrl("/projects/?d&p=master&n=15&S=10")
        ), new Function<ProjectListTestCase, ProjectListTestCase[]>() {
            @Override
            public ProjectListTestCase[] apply(ProjectListTestCase testCase) {
                return new ProjectListTestCase[]{testCase};
            }
        }).iterator();
    }

    private static ProjectListTestCase listTestCase() {
        return new ProjectListTestCase();
    }

    private static final class ProjectListTestCase {
        private TestListRequest listParameter = new TestListRequest();
        private String expectedUrl;
        private JsonElement mockJsonElement = EasyMock.createMock(JsonElement.class);
        private GerritRestClient gerritRestClient;
        private ProjectsParser projectsParser;
        private BranchInfoParser branchInfoParser;
        private TagInfoParser tagInfoParser;

        public ProjectListTestCase withListParameter(TestListRequest listParameter) {
            this.listParameter = listParameter;
            return this;
        }

        public ProjectListTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        public ProjectListTestCase execute() throws Exception {
            ProjectsRestClient projectsRestClient = getProjectsRestClient();
            Projects.ListRequest list = projectsRestClient.list();
            listParameter.apply(list).get();
            return this;
        }

        public void verify() {
            EasyMock.verify(gerritRestClient, projectsParser);
        }

        public ProjectsRestClient getProjectsRestClient() throws Exception {
            return new ProjectsRestClient(
                    setupGerritRestClient(),
                    setupProjectsParser(),
                    setupBranchInfoParser(),
                    setupTagInfoParser()
            );
        }

        public GerritRestClient setupGerritRestClient() throws Exception {
            gerritRestClient = EasyMock.createMock(GerritRestClient.class);
            EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
                    .andReturn(mockJsonElement)
                    .once();
            EasyMock.replay(gerritRestClient);
            return gerritRestClient;
        }

        public ProjectsParser setupProjectsParser() throws Exception {
            projectsParser = EasyMock.createMock(ProjectsParser.class);
            EasyMock.expect(projectsParser.parseProjectInfos(mockJsonElement))
                    .andReturn(new TreeMap<String, ProjectInfo>())
                    .once();
            EasyMock.replay(projectsParser);
            return projectsParser;
        }

        public BranchInfoParser setupBranchInfoParser() throws Exception {
            branchInfoParser = EasyMock.createMock(BranchInfoParser.class);
            EasyMock.expect(branchInfoParser.parseBranchInfos(mockJsonElement))
                    .andReturn(Lists.<BranchInfo>newArrayList())
                    .once();
            EasyMock.replay(branchInfoParser);
            return branchInfoParser;
        }

        public TagInfoParser setupTagInfoParser() throws Exception {
            tagInfoParser = EasyMock.createMock(TagInfoParser.class);
            EasyMock.expect(tagInfoParser.parseTagInfos(mockJsonElement))
                    .andReturn(Lists.<TagInfo>newArrayList())
                    .once();
            EasyMock.replay(tagInfoParser);
            return tagInfoParser;
        }

        @Override
        public String toString() {
            return expectedUrl;
        }
    }

    private final static class TestListRequest {
        private Boolean description = null;
        private String prefix = null;
        private Integer limit = null;
        private Integer start = null;

        public TestListRequest withDescription(boolean description) {
            this.description = description;
            return this;
        }

        public TestListRequest withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public TestListRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TestListRequest withStart(int start) {
            this.start = start;
            return this;
        }

        public Projects.ListRequest apply(Projects.ListRequest target) {
            if (description != null) {
                target.withDescription(description);
            }
            if (prefix != null) {
                target.withPrefix(prefix);
            }
            if (limit != null) {
                target.withLimit(limit);
            }
            if (start != null) {
                target.withStart(start);
            }
            return target;
        }
    }

    @Test
    public void testCreateProject() throws Exception {
        final String name = "MyProject";
        createTestCase(name).expectUrl("/projects/" + name).execute().verify();
    }
    private static ProjectCreateTestCase createTestCase(String name) {
        return new ProjectCreateTestCase(name);
    }

    private static final class ProjectCreateTestCase {
        private final String name;
        private String expectedUrl;
        private JsonElement mockJsonElement = EasyMock.createMock(JsonElement.class);
        private GerritRestClient gerritRestClient;
        private ProjectsParser projectsParser;
        private BranchInfoParser branchInfoParser;
        private TagInfoParser tagInfoParser;
        private String mockJsonString = "{\"name\":\"whatever\"}";
        private ProjectInput mockProjectInput;
        private ProjectInfo mockProjectInfo = EasyMock.createMock(ProjectInfo.class);

        public ProjectCreateTestCase(String name) {
            this.name = name;
            mockProjectInput = new ProjectInput();
            mockProjectInput.name = name;
        }

        public ProjectCreateTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        public ProjectCreateTestCase execute() throws Exception {
            ProjectsRestClient projectsRestClient = getProjectsRestClient();
            projectsRestClient.create(name);
            return this;
        }

        public void verify() {
            EasyMock.verify(gerritRestClient, projectsParser);
        }

        public ProjectsRestClient getProjectsRestClient() throws Exception {
            return new ProjectsRestClient(
                    setupGerritRestClient(),
                    setupProjectsParser(),
                    setupBranchInfoParser(),
                    setupTagInfoParser()
            );
        }

        public GerritRestClient setupGerritRestClient() throws Exception {
            gerritRestClient = EasyMock.createMock(GerritRestClient.class);
            EasyMock.expect(gerritRestClient.putRequest(expectedUrl, mockJsonString))
                    .andReturn(mockJsonElement)
                    .once();
            EasyMock.replay(gerritRestClient);
            return gerritRestClient;
        }

        public ProjectsParser setupProjectsParser() throws Exception {
            projectsParser = EasyMock.createMock(ProjectsParser.class);
            EasyMock.expect(projectsParser.generateProjectInput(
                    EasyMock.cmp(mockProjectInput,
                            new SameName(),
                            LogicalOperator.EQUAL)))
                    .andReturn(mockJsonString)
                    .once();
            EasyMock.expect(projectsParser.parseSingleProjectInfo(mockJsonElement))
                    .andReturn(mockProjectInfo)
                    .once();
            EasyMock.replay(projectsParser);
            return projectsParser;
        }

        public BranchInfoParser setupBranchInfoParser() throws Exception {
            branchInfoParser = EasyMock.createMock(BranchInfoParser.class);
            return branchInfoParser;
        }

        public TagInfoParser setupTagInfoParser() throws Exception {
            tagInfoParser = EasyMock.createMock(TagInfoParser.class);
            return tagInfoParser;
        }
    }
    private static class SameName implements Comparator<ProjectInput> {
        @Override
        public int compare(ProjectInput o1, ProjectInput o2) {
            return o1.name.equals(o2.name) ? 0 : 1;
        }
    }
}
