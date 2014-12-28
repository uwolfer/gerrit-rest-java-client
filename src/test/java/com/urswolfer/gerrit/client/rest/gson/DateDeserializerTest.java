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

package com.urswolfer.gerrit.client.rest.gson;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Thomas Forrer
 */
public class DateDeserializerTest {
    private final DateDeserializer dateDeserializer = new DateDeserializer();

    @Test(dataProvider = "TestCases")
    public void testDeserialize(TestCase testCase) throws Exception {
        Date actualDate = dateDeserializer.deserialize(testCase.getJsonElement(), null, null);
        Truth.assertThat(actualDate).isEqualTo(testCase.expectedDate);
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void testInvalidFormattedDateString() throws Exception {
        JsonElement jsonElement = getJsonElementForDateString("12.06.2013 12:12:44.123000000");
        dateDeserializer.deserialize(jsonElement, null, null);
    }

    @DataProvider(name = "TestCases")
    public Iterator<TestCase[]> getTestCases() throws Exception {
        return Lists.newArrayList(
                forDateString("2013-07-21 14:23:59.207000000")
                        .expectUtcDate(2013, 7, 21, 14, 23, 59),
                forDateString("2014-01-12 07:12:22.090000000")
                        .expectUtcDate(2014, 1, 12, 7, 12, 22),
                forDateString("2015-12-05 23:59:59.000000000")
                        .expectUtcDate(2015, 12, 5, 23, 59, 59)
        ).iterator();
    }

    private static TestCase forDateString(String dateAsString) {
        return new TestCase(dateAsString);
    }

    private static JsonElement getJsonElementForDateString(String dateAsString) {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(jsonElement.getAsString()).andReturn(dateAsString).once();
        EasyMock.replay(jsonElement);
        return jsonElement;
    }

    private static final class TestCase {
        private String dateAsString;
        private Date expectedDate;

        TestCase(String dateAsString) {
            this.dateAsString = dateAsString;
        }

        TestCase[] expectUtcDate(int year, int month, int day, int hours, int minutes, int seconds) {
            Calendar utc = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.set(year, month - 1, day, hours, minutes, seconds);
            utc.set(Calendar.MILLISECOND, 0);
            this.expectedDate = utc.getTime();
            return new TestCase[]{this};
        }

        JsonElement getJsonElement() {
            return getJsonElementForDateString(dateAsString);
        }
    }
}
