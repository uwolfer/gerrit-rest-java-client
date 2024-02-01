/*
 * Copyright 2013-2024 Urs Wolfer
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
package com.urswolfer.gerrit.client.rest.http.config.parsers;

import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class PreferencesParser {
    protected final Gson gson;

    public PreferencesParser(Gson gson) {
        this.gson = gson;
    }


    public GeneralPreferencesInfo parseGeneralPreferences(JsonElement result) {
        return gson.fromJson(result, GeneralPreferencesInfo.class);
    }

    public DiffPreferencesInfo parseDiffPreferences(JsonElement result) {
        return gson.fromJson(result, DiffPreferencesInfo.class);
    }

    public EditPreferencesInfo parseEditPreferences(JsonElement result) {
        return gson.fromJson(result, EditPreferencesInfo.class);
    }

}
