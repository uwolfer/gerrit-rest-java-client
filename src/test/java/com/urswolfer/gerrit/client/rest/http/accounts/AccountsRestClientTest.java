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

package com.urswolfer.gerrit.client.rest.http.accounts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;

/**
 * @author Thomas Forrer
 */
public class AccountsRestClientTest {

    private static final GerritJson gerritJson = AbstractJsonTest.getGerritJson();
    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testId() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/accounts/jdoe");
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, gerritJson);
        accountsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSelf() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/accounts/self");
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, gerritJson);
        accountsRestClient.self().get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testSuggestAccount() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet(
                "/accounts/?suggest&q=jdoe&n=5");
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, gerritJson);

        accountsRestClient.suggestAccounts("jdoe").withLimit(5).get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCreate() throws Exception {
        String username = "foo";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/" + username, "{\"username\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        AccountsRestClient accountsRestClient = new AccountsRestClient(gerritRestClient, gerritJson);
        accountsRestClient.create(username);
        EasyMock.verify(gerritRestClient);
    }

    private GerritRestClient gerritRestClientExpectGet(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
            .andReturn(EMPTY_JSON_OBJECT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GerritRestClient gerritRestClientExpectPut(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.putRequest(expectedUrl))
            .andReturn(EMPTY_JSON_OBJECT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GerritRestClient gerritRestClientExpectDelete(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.deleteRequest(expectedUrl))
            .andReturn(EMPTY_JSON_OBJECT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }


}
