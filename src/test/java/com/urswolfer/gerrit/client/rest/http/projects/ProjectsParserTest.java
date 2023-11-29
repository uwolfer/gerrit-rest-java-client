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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.ConfigInfo;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.client.InheritableBoolean;
import com.google.gerrit.extensions.client.ProjectState;
import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import com.urswolfer.gerrit.client.rest.http.common.ProjectInfoBuilder;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

/**
 * @author Thomas Forrer
 */
public class ProjectsParserTest extends AbstractParserTest {
    private static final List<ProjectInfo> PROJECT_INFO_LIST = Lists.newArrayList();

    static {
        PROJECT_INFO_LIST.add(new ProjectInfoBuilder()
                .withId("packages%2Ftest")
                .get());
        PROJECT_INFO_LIST.add(new ProjectInfoBuilder()
                .withId("repo2")
                .get());
        PROJECT_INFO_LIST.add(new ProjectInfoBuilder()
                .withId("testrepo")
                .get());
    }

    private final ProjectsParser projectsParser = new ProjectsParser(getGson());

    @Test
    public void testParseProjectInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("projects.json");

        SortedMap<String, ProjectInfo> projectInfos = projectsParser.parseProjectInfos(jsonElement);

        Truth.assertThat(projectInfos.size()).isEqualTo(3);
        int i = 0;
        for (ProjectInfo projectInfo : projectInfos.values()) {
            ProjectInfo expected = PROJECT_INFO_LIST.get(i);
            GerritAssert.assertEquals(projectInfo, expected);
            i++;
        }
    }

    @Test
    public void testParseSingleProjectInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("project.json");

        ProjectInfo projectInfo = projectsParser.parseSingleProjectInfo(jsonElement);

        GerritAssert.assertEquals(projectInfo, new ProjectInfoBuilder()
            .withId("Hello-World")
            .withName("Hello-World")
            .withParent("My-Parent")
            .withDescription("Hello-World project")
            .withState(ProjectState.ACTIVE)
            .get());
    }

    @Test
    public void testGenerateProjectInput() throws Exception {
        ProjectInput projectInput = new ProjectInput();
        projectInput.name = "MyProject";
        projectInput.description = "This is a demo project.";
        projectInput.owners = Collections.singletonList("MyProject-Owners");

        String outputForTesting = projectsParser.generateProjectInput(projectInput);

        ProjectInput parsedJson = new Gson().fromJson(outputForTesting, ProjectInput.class);
        Truth.assertThat(parsedJson.name).isEqualTo(projectInput.name);
        Truth.assertThat(parsedJson.description).isEqualTo(projectInput.description);
        Truth.assertThat(parsedJson.owners).isEqualTo(projectInput.owners);
    }

    @Test
    public void testParseProjectConfigInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("configInfo.json");

        ConfigInfo configInfo = projectsParser.parseConfigInfo(jsonElement);


        Truth.assertThat(configInfo.maxObjectSizeLimit.value).isEqualTo("10m");
        Truth.assertThat(configInfo.useContentMerge.configuredValue).isEqualTo(InheritableBoolean.TRUE);
        Truth.assertThat(configInfo.useContributorAgreements.configuredValue).isEqualTo(InheritableBoolean.FALSE);
        Truth.assertThat(configInfo.defaultSubmitType.value).isEqualTo(SubmitType.MERGE_IF_NECESSARY);
        ImmutableMap<String, ImmutableList<String>> extensionPanel = configInfo.extensionPanelNames;
        Truth.assertThat(extensionPanel.size() ).isEqualTo(1);
        Truth.assertThat(extensionPanel.containsKey("lfs")).isTrue();
        Truth.assertThat(Objects.requireNonNull(extensionPanel.get("lfs")).size()).isEqualTo(2);

    }
}
