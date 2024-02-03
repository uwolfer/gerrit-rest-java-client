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
import com.google.gerrit.extensions.client.DiffPreferencesInfo;
import com.google.gerrit.extensions.client.EditPreferencesInfo;
import com.google.gerrit.extensions.client.GeneralPreferencesInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

/**
 * @author Thomas Forrer
 */
public class PreferencesParserTest extends AbstractParserTest {
    private final PreferencesParser preferencesParser = new PreferencesParser(getGson());


    @Test
    public void testParseGeneralPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("generalPreferences.json");
        GeneralPreferencesInfo preferencesInfo = preferencesParser.parseGeneralPreferences(jsonElement);
        Truth.assertThat(preferencesInfo.changesPerPage).isEqualTo(25);
        Truth.assertThat(preferencesInfo.workInProgressByDefault).isTrue();
    }

    @Test
    public void testParseDiffPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("diffPreferences.json");
        DiffPreferencesInfo diffPreferencesInfo = preferencesParser.parseDiffPreferences(jsonElement);
        Truth.assertThat(diffPreferencesInfo.ignoreWhitespace).isEqualTo(DiffPreferencesInfo.Whitespace.IGNORE_NONE);
    }

    @Test
    public void testParseEditPreferences() throws Exception {
        JsonElement jsonElement = getJsonElement("editPreferences.json");
        EditPreferencesInfo editPreferencesInfo = preferencesParser.parseEditPreferences(jsonElement);
        Truth.assertThat(editPreferencesInfo.lineLength).isEqualTo(100);
        Truth.assertThat(editPreferencesInfo.showTabs).isEqualTo(true);
    }
}
