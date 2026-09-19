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

package com.urswolfer.gerrit.client.rest.contract;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.EmailInfo;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Pins what {@code gerritApi.accounts()} puts on the wire and what it makes of the response.
 *
 * <p>See {@link FakeGerritServer} for why these tests go through the public API instead of mocking
 * the client internals.
 */
public class AccountsContractTest {

    @Test
    public void self() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/accounts/self", "accounts/self/account.json");

        AccountInfo accountInfo = server.api().accounts().self().get();

        Truth.assertThat(accountInfo.name).isEqualTo("John Doe");
        Truth.assertThat(server.trace()).containsExactly("GET /accounts/self");
        server.verify();
    }

    @Test
    public void detail() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/accounts/self/detail", "accounts/self/accountDetail.json");

        Truth.assertThat(server.api().accounts().id("self").detail()).isNotNull();
        server.verify();
    }

    @Test
    public void emails() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/accounts/self/emails", "accounts/self/emails.json");

        List<EmailInfo> emails = server.api().accounts().id("self").getEmails();

        Truth.assertThat(emails).isNotEmpty();
        server.verify();
    }

    @Test
    public void starredChanges() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/accounts/self/starred.changes", "changes/changes.json");

        List<ChangeInfo> changes = server.api().accounts().id("self").getStarredChanges();

        Truth.assertThat(changes).hasSize(3);
        server.verify();
    }

    /**
     * Ids go through {@code Url.encode}, which is form encoding - a space becomes {@code +}, not
     * {@code %20}.
     */
    @Test
    public void idIsUrlEncoded() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/accounts/john+doe", "accounts/self/account.json");

        server.api().accounts().id("john doe").get();

        server.verify();
    }
}
