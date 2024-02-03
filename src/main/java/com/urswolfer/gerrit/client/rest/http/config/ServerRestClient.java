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
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import com.urswolfer.gerrit.client.rest.http.config.parsers.ServerConfigParser;

import java.util.concurrent.atomic.AtomicReference;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;

/**
 * @author Urs Wolfer
 */
public class ServerRestClient extends Server.NotImplemented implements Server {
    private final GerritRestClient gerritRestClient;
    private final AtomicReference<String> version = new AtomicReference<>();
    private final ServerConfigParser serverConfigParser;

    public ServerRestClient(GerritRestClient gerritRestClient, ServerConfigParser serverConfigParser) {
        this.gerritRestClient = gerritRestClient;
        this.serverConfigParser = serverConfigParser;
    }

    @Override
    public String getVersion() throws RestApiException {
        try {
            JsonElement jsonElement = gerritRestClient.getRequest("/config/server/version");
            version.set(jsonElement.getAsString());
            return version.get();
        } catch (HttpStatusException e) {
            int statusCode = e.getStatusCode();
            if (statusCode == SC_NOT_FOUND) { // Gerrit older than 2.8
                return "<2.8";
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServerInfo getInfo() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/info");
        return serverConfigParser.parseServerInfo(result);
    }

    @Override
    public GeneralPreferencesInfo setDefaultPreferences(GeneralPreferencesInfo input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences", body);
        return serverConfigParser.parseGeneralPreferences(result);
    }

    @Override
    public  GeneralPreferencesInfo getDefaultPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences");
        return serverConfigParser.parseGeneralPreferences(result);
    }

    @Override
    public DiffPreferencesInfo setDefaultDiffPreferences(DiffPreferencesInfo input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences.diff", body);
        return serverConfigParser.parseDiffPreferences(result);
    }

    @Override
    public DiffPreferencesInfo getDefaultDiffPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences.diff");
        return serverConfigParser.parseDiffPreferences(result);
    }

    @Override
    public EditPreferencesInfo setDefaultEditPreferences(EditPreferencesInfo input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/preferences.edit", body);
        return serverConfigParser.parseEditPreferences(result);
    }

    @Override
    public EditPreferencesInfo getDefaultEditPreferences() throws RestApiException {
        JsonElement result = gerritRestClient.getRequest("/config/server/preferences.edit");
        return serverConfigParser.parseEditPreferences(result);
    }

    @Override
    public ConsistencyCheckInfo checkConsistency(ConsistencyCheckInput input) throws RestApiException {
        String body = gerritRestClient.getGson().toJson(input);
        JsonElement result = gerritRestClient.putRequest("/config/server/check.consistency", body);
        return serverConfigParser.parseConsistencyInfo(result);
    }

    public String getVersionCached() throws RestApiException {
        String gerritVersion = version.get();
        return gerritVersion == null ? getVersion() : gerritVersion;
    }
}
