/*
 * Copyright 2018-2026 Urs Wolfer
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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gerrit.extensions.common.ServerInfo;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

public class ServerRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();

    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testGetVersion() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andReturn(new JsonPrimitive("2.9"));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);

        String version = serverRestClient.getVersion();

        assertEquals(version, "2.9");
    }

    @Test
    public void testGetVersionGivenEndpointIsNotAvailable() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(404, "Not found", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);

        String version = serverRestClient.getVersion();

        assertEquals(version, "<2.8");
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testGetVersionGivenEndpointThrowsUnauthorized() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(401, "Unauthorized", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);

        serverRestClient.getVersion();
    }

    @Test
    public void testGetInfo() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/info",JsonParser.parseString("{\"auth\":{\"auth_type\":\"LDAP\"}}"))
            .get();
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);

        ServerInfo info = serverRestClient.getInfo();

        Truth.assertThat(info.auth).isNotNull();
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetDefaultPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences","{\"changes_per_page\":100}",JsonParser.parseString("{\"changes_per_page\":25}"))
            .get();
        GeneralPreferencesInfo generalPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);

        GeneralPreferencesInfo payload = new GeneralPreferencesInfo();
        payload.changesPerPage = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        GeneralPreferencesInfo returned = serverRestClient.setDefaultPreferences(payload);

        Truth.assertThat(returned.changesPerPage).isEqualTo(25);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public  void testGetDefaultPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences",EMPTY_JSON_OBJECT)
            .get();

        GeneralPreferencesInfo generalPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        GeneralPreferencesInfo returned = serverRestClient.getDefaultPreferences();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetDefaultDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences.diff","{\"line_length\":100}",JsonParser.parseString("{\"tab_size\":8}"))
            .get();
        DiffPreferencesInfo diffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);

        DiffPreferencesInfo payload = new DiffPreferencesInfo();
        payload.lineLength = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        DiffPreferencesInfo returned = serverRestClient.setDefaultDiffPreferences(payload);

        Truth.assertThat(returned.tabSize).isEqualTo(8);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetDefaultDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences.diff",JsonParser.parseString("{\"tab_size\":8}"))
            .get();

        DiffPreferencesInfo diffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        DiffPreferencesInfo returned = serverRestClient.getDefaultDiffPreferences();

        Truth.assertThat(returned.tabSize).isEqualTo(8);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSetDefaultEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences.edit","{\"line_length\":100}",JsonParser.parseString("{\"tab_size\":8}"))
            .get();
        EditPreferencesInfo editPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);

        EditPreferencesInfo payload = new EditPreferencesInfo();
        payload.lineLength = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        EditPreferencesInfo returned = serverRestClient.setDefaultEditPreferences(payload);

        Truth.assertThat(returned.tabSize).isEqualTo(8);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetDefaultEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences.edit",JsonParser.parseString("{\"tab_size\":8}"))
            .get();

        EditPreferencesInfo editPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, gerritJson);
        EditPreferencesInfo returned = serverRestClient.getDefaultEditPreferences();

        Truth.assertThat(returned.tabSize).isEqualTo(8);
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCheckConsistency() throws Exception {
    }
}
