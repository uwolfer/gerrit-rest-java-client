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

import static org.testng.Assert.assertEquals;

import com.google.gson.JsonPrimitive;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class ServerRestClientTest {

    @Test
    public void testGetVersion() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andReturn(new JsonPrimitive("2.9"));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient);

        String version = serverRestClient.getVersion();

        assertEquals(version, "2.9");
    }

    @Test
    public void testGetVersionGivenEndpointIsNotAvailable() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(404, "Not found", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient);

        String version = serverRestClient.getVersion();

        assertEquals(version, "<2.8");
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testGetVersionGivenEndpointThrowsUnauthorized() throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest("/config/server/version")).andThrow(new HttpStatusException(401, "Unauthorized", ""));
        EasyMock.replay(gerritRestClient);
        ServerRestClient serverRestClient = new ServerRestClient(gerritRestClient);

        serverRestClient.getVersion();
    }
}
