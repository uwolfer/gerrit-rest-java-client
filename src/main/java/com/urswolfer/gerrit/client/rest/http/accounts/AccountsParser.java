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

import com.google.common.reflect.TypeToken;
import com.google.gerrit.extensions.api.accounts.DeletedDraftCommentInfo;
import com.google.gerrit.extensions.client.ProjectWatchInfo;
import com.google.gerrit.extensions.common.AccountDetailInfo;
import com.google.gerrit.extensions.common.AccountExternalIdInfo;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.config.parsers.PreferencesParser;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Thomas Forrer
 */
public class AccountsParser extends PreferencesParser {
    private static final Type TYPE = new TypeToken<List<AccountInfo>>() {}.getType();
    private static final Type EMAIL_TYPE = new TypeToken<List<EmailInfo>>() {}.getType();
    private static final Type EXTERNAL_ID_TYPE = new TypeToken<List<AccountExternalIdInfo>>() {}.getType();
    private static final Type DELETE_DRAFT_TYPE = new TypeToken<List<DeletedDraftCommentInfo>>() {}.getType();
    private static final Type PROJECT_WATCH_TYPE = new TypeToken<List<ProjectWatchInfo>>() {}.getType();
    private static final Type STAR_TYPE = new TypeToken<SortedSet<String>>() {}.getType();

    public AccountsParser(Gson gson) {
        super(gson);
    }

    public AccountInfo parseAccountInfo(JsonElement result) {
        return gson.fromJson(result, AccountInfo.class);
    }

    public List<AccountInfo> parseAccountInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(parseAccountInfo(result));
        }
        return gson.fromJson(result, TYPE);
    }

    public AccountDetailInfo parseAccountDetailInfo(JsonElement result) {
        return gson.fromJson(result, AccountDetailInfo.class);
    }

    public List<ProjectWatchInfo> parseProjectWatchInfos(JsonElement result){
        return gson.fromJson(result,PROJECT_WATCH_TYPE);
    }

    public SortedSet<String> parseStarLabels(JsonElement result) {
        return gson.fromJson(result,STAR_TYPE);
    }

    public List<EmailInfo> parseEmailInfos(JsonElement result){
        return gson.fromJson(result, EMAIL_TYPE);
    }

    public EmailInfo parseSingleEmailInfo(JsonElement result) {
        return gson.fromJson(result, EmailInfo.class);
    }

    public List<AccountExternalIdInfo> parseAccountExternalIdInfos(JsonElement result){
        return gson.fromJson(result, EXTERNAL_ID_TYPE);
    }

    public List<DeletedDraftCommentInfo> parseDeleteDraftCommentInfos(JsonElement result){
        return gson.fromJson(result, DELETE_DRAFT_TYPE);
    }
}
