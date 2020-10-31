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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.collect.Maps;
import com.google.gerrit.extensions.common.ActionInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

import java.util.TreeMap;

/**
 * @author EFregnan
 */
public class ActionInfoParserTest extends AbstractParserTest{

    private static final TreeMap<String, ActionInfo> ACTION_INFOS = Maps.newTreeMap();

    static {
        ACTION_INFOS.put("submit",
            new ActionInfoBuilder()
                .withMethod("POST")
                .withLabel("Submit")
                .withTitle("Submit patch set 1 into master")
                .withEnabled(true)
                .get()
        );
        ACTION_INFOS.put("cherrypick",
            new ActionInfoBuilder()
                .withMethod("POST")
                .withLabel("Cherry Pick")
                .withTitle("Cherry pick change")
                .withEnabled(true)
                .get()
            );
    }

    private ActionInfoParser actionsParser = new ActionInfoParser(getGson());

    @Test
    public void testParseActionInfos() throws Exception {
        TreeMap<String, ActionInfo> actions = parseActions();
        GerritAssert.assertEquals(actions, ACTION_INFOS);
    }

    private TreeMap<String, ActionInfo> parseActions() throws Exception {
        JsonElement jsonElement = getJsonElement("actions.json");
        return actionsParser.parseActionInfos(jsonElement);
    }
}
