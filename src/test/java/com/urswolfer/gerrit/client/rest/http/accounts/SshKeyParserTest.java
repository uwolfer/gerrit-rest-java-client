/*
 * Copyright 2013-2015 Urs Wolfer
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
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Philip Moore
 */
public class SshKeyParserTest extends AbstractParserTest {
    private final SshKeysParser sshKeysParser = new SshKeysParser(getGson());

    private final SshKeyInfo keyOneInfo;

    public SshKeyParserTest() {
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
        SshKeyInfo keyInfo = sshKeysParser.parseSshKeyInfo(jsonElement);
        GerritAssert.assertEquals(keyInfo, keyOneInfo);
    }

    @Test
    public void testParseSshKeyInfoWithNullJsonElement() throws Exception {
        SshKeyInfo keyInfo = sshKeysParser.parseSshKeyInfo(null);
        Truth.assertThat(keyInfo).isNull();
    }

    @Test
    public void testParseSshKeyInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/sshkeys.json");
        List<SshKeyInfo> keyInfos = sshKeysParser.parseSshKeyInfos(jsonElement);
        Truth.assertThat(keyInfos).hasSize(2);
    }

    @Test
    public void testParseSingleSshKeyInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/sshkey.json");
        List<SshKeyInfo> keyInfos = sshKeysParser.parseSshKeyInfos(jsonElement);
        Truth.assertThat(keyInfos).hasSize(1);
    }
}
