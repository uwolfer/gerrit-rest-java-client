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
import com.google.gerrit.extensions.common.ServerInfo;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;

/**
 * Pins the server config and tools endpoints, plus the request path the client builds for
 * authenticated access.
 *
 * <p>See {@link FakeGerritServer} for why these tests go through the public API instead of mocking
 * the client internals.
 */
public class ServerContractTest {

    @Test
    public void version() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/config/server/version", "config/server/version");

        Truth.assertThat(server.api().config().server().getVersion()).isEqualTo("2.10");
        server.verify();
    }

    /**
     * Gerrit older than 2.8 has no version endpoint at all.
     */
    @Test
    public void versionFallsBackWhenTheEndpointIsMissing() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubStatus("GET", "/config/server/version", 404);

        Truth.assertThat(server.api().config().server().getVersion()).isEqualTo("<2.8");
        server.verify();
    }

    @Test
    public void info() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/config/server/info", "config/parsers/serverinfo.json");

        ServerInfo serverInfo = server.api().config().server().getInfo();

        Truth.assertThat(serverInfo.auth.authType).isNotNull();
        server.verify();
    }

    @Test
    public void commitMessageHookIsReturnedAsStream() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubRaw("GET", "/tools/hooks/commit-msg", "text/plain", "#!/bin/sh\n",
                Collections.<String, String>emptyMap());

        InputStream hook = server.api().tools().getCommitMessageHook();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            while ((read = hook.read()) != -1) {
                out.write(read);
            }
            Truth.assertThat(out.toString("UTF-8")).isEqualTo("#!/bin/sh\n");
        } finally {
            hook.close();
        }
        server.verify();
    }

    /**
     * A Gerrit HTTP password token is never sent to the login page - that would make Gerrit
     * authenticate it against LDAP and potentially lock the account - so the client skips the login
     * probe entirely and goes straight to the authenticated {@code /a} endpoint.
     */
    @Test
    public void httpPasswordAuthenticationPrefixesThePathWithA() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/a/config/server/version", "config/server/version");

        GerritAuthData authData = new GerritAuthData.Basic(FakeGerritServer.HOST, "jdoe", "secret", true);
        Truth.assertThat(server.api(authData).config().server().getVersion()).isEqualTo("2.10");
        server.verify();
    }

    /**
     * With a login and password that is not an HTTP password token the client does try to log in -
     * a {@code GET} on {@code /login/} and then a form {@code POST} - and falls back to the
     * authenticated {@code /a} endpoint when that yields no session.
     */
    @Test
    public void loginAndPasswordAuthenticationFallsBackToTheAPath() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/a/config/server/version", "config/server/version");

        GerritAuthData authData = new GerritAuthData.Basic(FakeGerritServer.HOST, "jdoe", "secret");

        Truth.assertThat(server.api(authData).config().server().getVersion()).isEqualTo("2.10");
        server.verify();
    }
}
