/*
 * Copyright 2013-2020 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeMessageInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author EFregnan
 */
public class MessagesParserTest extends AbstractParserTest {
    private static final List<ChangeMessageInfo> MESSAGES_INFOS = new ArrayList<ChangeMessageInfo>();

    static {
        AccountInfo accountInfo = new AccountInfoBuilder()
            .withName("EFregnan")
            .withEmail("abc@gmail.com")
            .withUsername("efregnan")
            .withAccountId(1000000)
            .get();

        MESSAGES_INFOS.add(new ChangeMessageInfoBuilder()
                    .withId("EAF")
                    .withMessage("Patch Set 1: This is the first message.")
                    .withDate("2019-11-28 22:28:50")
                    .withAuthor(accountInfo)
                    .withRevisionNumber(1)
                    .get()
        );
        MESSAGES_INFOS.add(new ChangeMessageInfoBuilder()
                    .withId("YH-egE")
                    .withMessage("i think so")
                    .withDate("2019-11-28 22:33:12")
                    .withAuthor(accountInfo)
                    .withRevisionNumber(2)
                    .get()
        );
    }

    private MessagesParser messagesParser = new MessagesParser(getGson());

    @Test
    public void testParseCommentInfos() throws Exception {
        List<ChangeMessageInfo> messages = parseMessages();
        GerritAssert.assertEquals(messages, MESSAGES_INFOS);
    }

    private List<ChangeMessageInfo> parseMessages() throws Exception {
        JsonElement jsonElement = getJsonElement("messages.json");
        return messagesParser.parseChangeMessageInfos(jsonElement);
    }
}

