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

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.api.access.ProjectAccessInfo;
import com.google.gerrit.extensions.api.access.ProjectAccessInput;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.SortedMap;

/**
 * @author Thomas Forrer
 */
public class ProjectsParser {
    private static final Type TYPE = new TypeToken<SortedMap<String, ProjectInfo>>() {}.getType();

    private final Gson gson;

    public ProjectsParser(Gson gson) {
        this.gson = gson;
    }

    public SortedMap<String, ProjectInfo> parseProjectInfos(JsonElement result) {
        return gson.fromJson(result, TYPE);
    }

    public ProjectInfo parseSingleProjectInfo(JsonElement result) {
        return gson.fromJson(result, ProjectInfo.class);
    }

    public String generateProjectInput(ProjectInput input) {
        return gson.toJson(input, ProjectInput.class);
    }
    
    public ProjectAccessInfo parseProjectAccessInfo(JsonElement result) {
    	return gson.fromJson(result,ProjectAccessInfo.class);
    }
    public String toJson(ProjectAccessInput p) {
    	return gson.toJson(p);
    }
}
