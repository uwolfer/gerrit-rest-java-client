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
import com.google.gerrit.extensions.common.SshKeyInfo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Created by Phil Moore on 24/04/2020.
 */
public class SshKeysParser {
    private static final Type TYPE = new TypeToken<List<SshKeyInfo>>() {}.getType();

    private final Gson gson;

    public SshKeysParser(Gson gson) {
        this.gson = gson;
    }

    public SshKeyInfo parseSshKeyInfo(JsonElement result) {
        return gson.fromJson(result, SshKeyInfo.class);
    }

    public List<SshKeyInfo> parseSshKeyInfos(JsonElement result) {
        if (!result.isJsonArray()) {
            return Collections.singletonList(parseSshKeyInfo(result));
        }
        return gson.fromJson(result, TYPE);
    }
}
