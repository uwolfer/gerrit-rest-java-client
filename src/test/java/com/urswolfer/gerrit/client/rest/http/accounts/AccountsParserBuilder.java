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

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gson.JsonElement;
import org.easymock.EasyMock;

import java.util.List;

public final class AccountsParserBuilder {
    private final AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);

    public AccountsParser get() {
        EasyMock.replay(accountsParser);
        return accountsParser;
    }

    public AccountsParserBuilder expectParseAccountInfos(JsonElement jsonElement, List<AccountInfo> result) {
        EasyMock.expect(accountsParser.parseAccountInfos(jsonElement))
                .andReturn(result).once();
        return this;
    }

    public AccountsParserBuilder expectParseAccountInfo(JsonElement jsonElement, AccountInfo result) {
        EasyMock.expect(accountsParser.parseAccountInfo(jsonElement))
                .andReturn(result).once();
        return this;
    }
}
