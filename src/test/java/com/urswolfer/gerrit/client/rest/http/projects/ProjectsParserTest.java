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
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import com.urswolfer.gerrit.client.rest.http.common.ProjectInfoBuilder;
import org.testng.annotations.Test;

import java.util.List;
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
}
