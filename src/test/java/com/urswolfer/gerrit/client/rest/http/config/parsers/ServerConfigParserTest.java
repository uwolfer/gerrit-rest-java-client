/*
 * Copyright 2013-2024 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.config.parsers;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.ServerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

/**
 * @author Thomas Forrer
 */
public class ServerConfigParserTest extends AbstractParserTest {
    private final ServerConfigParser serverConfigParser = new ServerConfigParser(getGson());


    @Test
    public void testParseServerInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("serverinfo.json");
        ServerInfo serverInfo = serverConfigParser.parseServerInfo(jsonElement);

        Truth.assertThat(serverInfo.auth.authType.name()).isEqualTo("LDAP");
        Truth.assertThat(serverInfo.change.allowBlame).isTrue();
    }
}
