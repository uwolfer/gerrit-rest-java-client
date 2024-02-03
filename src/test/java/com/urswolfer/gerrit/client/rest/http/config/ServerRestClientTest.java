/*
 * Copyright 2018 Urs Wolfer
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
import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import com.urswolfer.gerrit.client.rest.http.config.parsers.ServerConfigParser;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ServerRestClientTest {

    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);

    @Test
    public void testGetVersion() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andReturn(new JsonPrimitive("2.9"));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,null);

        String version = serverRestClient.getVersion();

        assertEquals(version, "2.9");
    }

    @Test
    public void testGetVersionGivenEndpointIsNotAvailable() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(404, "Not found", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,null);

        String version = serverRestClient.getVersion();

        assertEquals(version, "<2.8");
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testGetVersionGivenEndpointThrowsUnauthorized() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(401, "Unauthorized", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient, null);

        serverRestClient.getVersion();
    }

    @Test
    public void testGetInfo() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/info",MOCK_JSON_ELEMENT)
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);
        ServerInfo info = EasyMock.createMock(ServerInfo.class);

        EasyMock.expect(configParser.parseServerInfo(MOCK_JSON_ELEMENT)).andReturn(info).once();
        EasyMock.replay(configParser);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        serverRestClient.getInfo();
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testSetDefaultPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences","{\"changes_per_page\":100}",MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);
        GeneralPreferencesInfo generalPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);
        EasyMock.expect(configParser.parseGeneralPreferences(MOCK_JSON_ELEMENT)).andReturn(generalPreferencesInfo).once();
        EasyMock.replay(configParser);

        GeneralPreferencesInfo payload = new GeneralPreferencesInfo();
        payload.changesPerPage = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        GeneralPreferencesInfo returned = serverRestClient.setDefaultPreferences(payload);

        Truth.assertThat(returned).isEqualTo(generalPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public  void testGetDefaultPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences",MOCK_JSON_ELEMENT)
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);

        GeneralPreferencesInfo generalPreferencesInfo = EasyMock.createMock(GeneralPreferencesInfo.class);
        EasyMock.expect(configParser.parseGeneralPreferences(MOCK_JSON_ELEMENT)).andReturn(generalPreferencesInfo).once();
        EasyMock.replay(configParser);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        GeneralPreferencesInfo returned = serverRestClient.getDefaultPreferences();

        Truth.assertThat(returned).isEqualTo(generalPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testSetDefaultDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences.diff","{\"line_length\":100}",MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);
        DiffPreferencesInfo diffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);
        EasyMock.expect(configParser.parseDiffPreferences(MOCK_JSON_ELEMENT)).andReturn(diffPreferencesInfo).once();
        EasyMock.replay(configParser);

        DiffPreferencesInfo payload = new DiffPreferencesInfo();
        payload.lineLength = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        DiffPreferencesInfo returned = serverRestClient.setDefaultDiffPreferences(payload);

        Truth.assertThat(returned).isEqualTo(diffPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testGetDefaultDiffPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences.diff",MOCK_JSON_ELEMENT)
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);

        DiffPreferencesInfo diffPreferencesInfo = EasyMock.createMock(DiffPreferencesInfo.class);
        EasyMock.expect(configParser.parseDiffPreferences(MOCK_JSON_ELEMENT)).andReturn(diffPreferencesInfo).once();
        EasyMock.replay(configParser);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        DiffPreferencesInfo returned = serverRestClient.getDefaultDiffPreferences();

        Truth.assertThat(returned).isEqualTo(diffPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testSetDefaultEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/config/server/preferences.edit","{\"line_length\":100}",MOCK_JSON_ELEMENT)
            .expectGetGson()
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);
        EditPreferencesInfo editPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);
        EasyMock.expect(configParser.parseEditPreferences(MOCK_JSON_ELEMENT)).andReturn(editPreferencesInfo).once();
        EasyMock.replay(configParser);

        EditPreferencesInfo payload = new EditPreferencesInfo();
        payload.lineLength = 100;
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        EditPreferencesInfo returned = serverRestClient.setDefaultEditPreferences(payload);

        Truth.assertThat(returned).isEqualTo(editPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testGetDefaultEditPreferences() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/config/server/preferences.edit",MOCK_JSON_ELEMENT)
            .get();
        ServerConfigParser configParser = EasyMock.createMock(ServerConfigParser.class);

        EditPreferencesInfo editPreferencesInfo = EasyMock.createMock(EditPreferencesInfo.class);
        EasyMock.expect(configParser.parseEditPreferences(MOCK_JSON_ELEMENT)).andReturn(editPreferencesInfo).once();
        EasyMock.replay(configParser);

        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient,configParser);
        EditPreferencesInfo returned = serverRestClient.getDefaultEditPreferences();

        Truth.assertThat(returned).isEqualTo(editPreferencesInfo);
        EasyMock.verify(gerritRestClient,configParser);
    }

    @Test
    public void testCheckConsistency() throws Exception {
    }
}
