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

package com.urswolfer.gerrit.client.rest.http.projects.parsers;

import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Parser for commit information from project/projectname/commit
 */
public class ProjectCommitInfoParser {

    private final Gson gson;

    public ProjectCommitInfoParser(Gson gson) {
        this.gson = gson;
    }

    public CommitInfo parseSingleCommitInfo(JsonElement result) {
        return gson.fromJson(result, CommitInfo.class);
    }


    public IncludedInInfo parseIncludedInInfo(JsonElement jsonElement) {
        return gson.fromJson(jsonElement, IncludedInInfo.class);
    }
}
