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

import com.google.gerrit.extensions.api.access.ProjectAccessInfo;
import com.google.gerrit.extensions.api.access.ProjectAccessInput;
import com.google.gerrit.extensions.api.projects.ConfigInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonElement;
import org.easymock.EasyMock;

/**
 * @author Thomas Forrer
 */
public final class ProjectsParserBuilder {
    private final ProjectsParser projectsParser = EasyMock.createMock(ProjectsParser.class);

    public ProjectsParser get() {
        EasyMock.replay(projectsParser);
        return projectsParser;
    }

    public ProjectsParserBuilder expectParseSingleProjectInfo(JsonElement jsonElement, ProjectInfo result) {
        EasyMock.expect(projectsParser.parseSingleProjectInfo(jsonElement))
                .andReturn(result).once();
        return this;
    }

    public ProjectsParserBuilder expectParseProjectAccessInfo(JsonElement jsonElement, ProjectAccessInfo result) {
        EasyMock.expect(projectsParser.parseProjectAccessInfo(jsonElement))
                .andReturn(result).once();
        return this;
    }

    public ProjectsParserBuilder expectParseProjectAccessInput(String json, ProjectAccessInput input) {
        EasyMock.expect(projectsParser.generateProjectAccessInput(input))
                .andReturn(json).once();
        return this;
    }

    public ProjectsParserBuilder expectParseProjectConfigInfo(JsonElement jsonElement, ConfigInfo result) {
        EasyMock.expect(projectsParser.parseConfigInfo(jsonElement))
            .andReturn(result).once();
        return this;
    }
}
