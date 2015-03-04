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

package com.urswolfer.gerrit.client.rest.gson;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gson.JsonElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

/**
 * @author Urs Wolfer
 */
public class DateSerializerTest extends DateFormatterTest {
    private final DateSerializer dateSerializer = new DateSerializer();

    @Test(dataProvider = "TestCases")
    public void testSerialize(TestCase testCase) throws Exception {
        JsonElement jsonElement = dateSerializer.serialize(testCase.date, null, null);
        Truth.assertThat(jsonElement.getAsString()).isEqualTo(testCase.getJsonElement().getAsString());
    }

    @DataProvider(name = "TestCases")
    public Iterator<TestCase[]> getTestCases() throws Exception {
        return Lists.newArrayList(
                forDateString("2013-07-21 14:23:59")
                        .utcDate(2013, 7, 21, 14, 23, 59),
                forDateString("2014-01-12 07:12:22")
                        .utcDate(2014, 1, 12, 7, 12, 22),
                forDateString("2015-12-05 23:59:59")
                        .utcDate(2015, 12, 5, 23, 59, 59)
        ).iterator();
    }
}
