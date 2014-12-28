/*
 * Copyright 2013-2014 Urs Wolfer
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

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Thomas Forrer
 */
public class AccountsParserTest extends AbstractParserTest {
    private final AccountsParser accountsParser = new AccountsParser(getGson());

    private final AccountInfo johnDoe;

    public AccountsParserTest() {
        this.johnDoe = new AccountInfo(1000003);
        this.johnDoe.name = "John Doe";
        this.johnDoe.email = "jdoe@gmail.com";
        this.johnDoe.username = "jdoe";
        this.johnDoe.avatars = Collections.emptyList();
    }

    @Test
    public void testParseUserInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/account.json");

        AccountInfo accountInfo = accountsParser.parseUserInfo(jsonElement);

        GerritAssert.assertEquals(accountInfo, johnDoe);
    }

    @Test
    public void testParseUserInfosWithNullJsonElement() throws Exception {
        AccountInfo accountInfo = accountsParser.parseUserInfo(null);

        Assert.assertNull(accountInfo);
    }
}
