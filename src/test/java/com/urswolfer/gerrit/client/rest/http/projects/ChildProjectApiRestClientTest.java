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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

public class ChildProjectApiRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();


    @Test
    public void testGet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1", JsonParser.parseString("{\"id\":\"p1\"}"))
            .get();
        ChildProjectApiRestClient client = new ChildProjectApiRestClient(gerritRestClient, gerritJson, "/projects/sandbox", "child1");
        ProjectInfo returned = client.get();

        Truth.assertThat(returned.id).isEqualTo("p1");
        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testGetRecursive() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/projects/sandbox/children/child1?recursive", JsonParser.parseString("{\"id\":\"p1\"}"))
            .get();
        ChildProjectApiRestClient client = new ChildProjectApiRestClient(gerritRestClient, gerritJson, "/projects/sandbox", "child1");
        ProjectInfo returned = client.get(true);

        Truth.assertThat(returned.id).isEqualTo("p1");
        EasyMock.verify(gerritRestClient);
    }
}
