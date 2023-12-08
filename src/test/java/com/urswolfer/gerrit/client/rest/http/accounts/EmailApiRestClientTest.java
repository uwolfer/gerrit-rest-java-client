/*
 * Copyright 2013-2023 Urs Wolfer
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
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class EmailApiRestClientTest {

    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);

    @Test
    public void get() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/accounts/jdoe/emails/john.doe@example.com", MOCK_JSON_ELEMENT)
            .get();
        EmailApiRestClient emailApiRestClient = getEmailApiRestClient(gerritRestClient, null,
            "jdoe", "john.doe@example.com");

        emailApiRestClient.get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void delete() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectDelete("/accounts/jdoe/emails/john.doe@example.com")
            .get();
        EmailApiRestClient emailApiRestClient = getEmailApiRestClient(gerritRestClient, null,
            "jdoe", "john.doe@example.com");

        emailApiRestClient.delete();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void setPreferred() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/accounts/jdoe/emails/john.doe@example.com/preferred", MOCK_JSON_ELEMENT)
            .get();
        EmailApiRestClient emailApiRestClient = getEmailApiRestClient(gerritRestClient, null,
            "jdoe", "john.doe@example.com");

        emailApiRestClient.setPreferred();

        EasyMock.verify(gerritRestClient);
    }

    private EmailApiRestClient getEmailApiRestClient (GerritRestClient gerritRestClient, AccountsParser accountsParser,
                                                         String name, String email){
        if(accountsParser == null){
            accountsParser = EasyMock.createMock(AccountsParser.class);
        }
        return new EmailApiRestClient(gerritRestClient, accountsParser, name, email);
    }
}
