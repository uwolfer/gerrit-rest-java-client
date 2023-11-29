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
import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gerrit.extensions.client.ProjectWatchInfo;
import com.google.gerrit.extensions.common.AccountDetailInfo;
import com.google.gerrit.extensions.common.AccountExternalIdInfo;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public void testParseUserInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("self/account.json");
        AccountInfo accountInfo = accountsParser.parseAccountInfo(jsonElement);
        GerritAssert.assertEquals(accountInfo, johnDoe);
    }

    @Test
    public void testParseAccountDetailInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("self/accountDetail.json");
        AccountDetailInfo accountDetailInfo = accountsParser.parseAccountDetailInfo(jsonElement);
        Truth.assertThat(accountDetailInfo.registeredOn).isNotNull();
        Truth.assertThat(accountDetailInfo.inactive).isTrue();
        Truth.assertThat(accountDetailInfo).isEqualTo(johnDoe);
    }

    @Test
    public void testParseUserInfoWithNullJsonElement() throws Exception {
        AccountInfo accountInfo = accountsParser.parseAccountInfo(null);
        Truth.assertThat(accountInfo).isNull();
    }

    @Test
    public void testParseUserInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("accounts.json");
        List<AccountInfo> accountInfos = accountsParser.parseAccountInfos(jsonElement);
        Truth.assertThat(accountInfos).hasSize(2);
    }

    @Test
    public void testParseSingleUserInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/account.json");
        List<AccountInfo> accountInfos = accountsParser.parseAccountInfos(jsonElement);
        Truth.assertThat(accountInfos).hasSize(1);
    }

    @Test
    public void testParseGeneralPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("self/generalPreferences.json");
        GeneralPreferencesInfo preferencesInfo = accountsParser.parseGeneralPreferences(jsonElement);
        Truth.assertThat(preferencesInfo.changesPerPage).isEqualTo(25);
        Truth.assertThat(preferencesInfo.workInProgressByDefault).isTrue();
    }

    @Test
    public void testParseDiffPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("self/diffPreferences.json");
        DiffPreferencesInfo diffPreferencesInfo = accountsParser.parseDiffPreferences(jsonElement);
        Truth.assertThat(diffPreferencesInfo.ignoreWhitespace).isEqualTo(DiffPreferencesInfo.Whitespace.IGNORE_NONE);
    }

    @Test
    public void testParseEditPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("self/editPreferences.json");
        EditPreferencesInfo editPreferencesInfo = accountsParser.parseEditPreferences(jsonElement);
        Truth.assertThat(editPreferencesInfo.lineLength).isEqualTo(100);
        Truth.assertThat(editPreferencesInfo.showTabs).isEqualTo(true);
    }

    @Test
    public void testParseProjectWatchInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/watchedProjects.json");
        List<ProjectWatchInfo> watchInfoList = accountsParser.parseProjectWatchInfos(jsonElement);
        Truth.assertThat(watchInfoList).hasSize(2);
    }

    @Test
    public void testParseStarLabels() throws Exception {
        JsonElement jsonElement = getJsonElement("self/stars.json");
        Set<String> starLabels = accountsParser.parseStarLabels(jsonElement);
        Truth.assertThat(starLabels).hasSize(3);
        Truth.assertThat(starLabels).containsExactly("blue", "green", "red");
    }

    @Test
    public void testParseEmailInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("self/email.json");
        List<EmailInfo> accountInfo = accountsParser.parseEmailInfos(jsonElement);
        Truth.assertThat(accountInfo).hasSize(1);
    }

    @Test
    public void testParseEmailInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/emails.json");
        List<EmailInfo> accountInfo = accountsParser.parseEmailInfos(jsonElement);
        Truth.assertThat(accountInfo).hasSize(2);
    }

    @Test
    public void parseAccountExternalIdInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/externalIds.json");
        List<AccountExternalIdInfo> accountInfo = accountsParser.parseAccountExternalIdInfos(jsonElement);
        Truth.assertThat(accountInfo).hasSize(3);
    }

    @Test
    public void parseDeleteDraftCommentInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("self/deletedDraftComments.json");
        List<AccountExternalIdInfo> accountInfo = accountsParser.parseAccountExternalIdInfos(jsonElement);
        Truth.assertThat(accountInfo).hasSize(1);
    }
}
