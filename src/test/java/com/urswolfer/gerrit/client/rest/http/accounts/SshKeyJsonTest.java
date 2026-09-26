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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.List;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;

/**
 * @author Philip Moore
 */
public class SshKeyJsonTest extends AbstractJsonTest {
    private final GerritJson gerritJson = new GerritJson(getGson());

    private final SshKeyInfo keyOneInfo;

    public SshKeyJsonTest() {
        this.keyOneInfo = new SshKeyInfo();
        this.keyOneInfo.seq = 1;
        this.keyOneInfo.sshPublicKey = "ssh_key1";
        this.keyOneInfo.encodedKey = "encoded_key1";
        this.keyOneInfo.algorithm = "ssh-rsa";
        this.keyOneInfo.comment = "TestKey1";
        this.keyOneInfo.valid = true;
    }

    @Test
    public void testParseSshKeyInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("self/sshkey.json");
        SshKeyInfo keyInfo = gerritJson.as(jsonElement, SshKeyInfo.class);
        GerritAssert.assertEquals(keyInfo, keyOneInfo);
    }

    @Test
    public void testParseSshKeyInfoWithNullJsonElement() {
        SshKeyInfo keyInfo = gerritJson.as(null, SshKeyInfo.class);
        Truth.assertThat(keyInfo).isNull();
    }

    @Test
    public void testParseSshKeyInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/sshkeys.json");
        List<SshKeyInfo> keyInfos = gerritJson.asList(jsonElement, SshKeyInfo.class);
        Truth.assertThat(keyInfos).hasSize(2);
    }

    @Test
    public void testParseSingleSshKeyInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/sshkey.json");
        List<SshKeyInfo> keyInfos = gerritJson.asList(jsonElement, SshKeyInfo.class);
        Truth.assertThat(keyInfos).hasSize(1);
    }
}
