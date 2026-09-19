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

package com.urswolfer.gerrit.client.rest.http.config;

import com.google.gerrit.extensions.api.config.ConsistencyCheckInfo;
import com.google.gerrit.extensions.api.config.ConsistencyCheckInput;
import com.google.gerrit.extensions.api.config.Server;
import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gerrit.extensions.common.ServerInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;


import static org.apache.http.HttpStatus.SC_NOT_FOUND;

/**
 * @author Urs Wolfer
 */
public class ServerRestClient extends Server.NotImplemented implements Server {

    /**
     * What {@link #getVersion()} answers when the server has no version endpoint at all.
     */
    public static final String VERSION_BEFORE_2_8 = "<2.8";
    private final GerritRestContext context;
    private final GerritRestClient gerritRestClient;
    private final GerritJson gerritJson;

    public ServerRestClient(GerritRestContext context) {
        this.context = context;
        this.gerritRestClient = context.restClient();
        this.gerritJson = context.json();
    }

    @Override
    public String getVersion() throws RestApiException {
        try {
            JsonElement jsonElement = gerritRestClient.getRequest("/config/server/version");
            return jsonElement.getAsString();
        } catch (HttpStatusException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == SC_NOT_FOUND) { // Gerrit older than 2.8
                return VERSION_BEFORE_2_8;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServerInfo getInfo() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/info");
        return gerritJson.as(result, ServerInfo.class);
    }

    @Override
    public GeneralPreferencesInfo setDefaultPreferences(GeneralPreferencesInfo input) throws RestApiException {
        String body = gerritJson.toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences", body);
        return gerritJson.as(result, GeneralPreferencesInfo.class);
    }

    @Override
    public  GeneralPreferencesInfo getDefaultPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences");
        return gerritJson.as(result, GeneralPreferencesInfo.class);
    }

    @Override
    public DiffPreferencesInfo setDefaultDiffPreferences(DiffPreferencesInfo input) throws RestApiException {
        String body = gerritJson.toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences.diff", body);
        return gerritJson.as(result, DiffPreferencesInfo.class);
    }

    @Override
    public DiffPreferencesInfo getDefaultDiffPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences.diff");
        return gerritJson.as(result, DiffPreferencesInfo.class);
    }

    @Override
    public EditPreferencesInfo setDefaultEditPreferences(EditPreferencesInfo input) throws RestApiException {
        String body = gerritJson.toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences.edit", body);
        return gerritJson.as(result, EditPreferencesInfo.class);
    }

    @Override
    public EditPreferencesInfo getDefaultEditPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences.edit");
        return gerritJson.as(result, EditPreferencesInfo.class);
    }

    @Override
    public ConsistencyCheckInfo checkConsistency(ConsistencyCheckInput input) throws RestApiException {
        String body = gerritJson.toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/check.consistency", body);
        return gerritJson.as(result, ConsistencyCheckInfo.class);
    }

    /**
     * The version, read once per API instance rather than once per client.
     */
    public String getVersionCached() throws RestApiException {
        return context.serverVersion();
    }
}
