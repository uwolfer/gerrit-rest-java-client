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

import com.google.gson.JsonElement;
import org.easymock.EasyMock;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Urs Wolfer
 */
public abstract class DateFormatterTest {
    protected static TestCase forDateString(String dateAsString) {
        return new TestCase(dateAsString);
    }

    protected static JsonElement getJsonElementForDateString(String dateAsString) {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        EasyMock.expect(jsonElement.getAsString()).andReturn(dateAsString).once();
        EasyMock.replay(jsonElement);
        return jsonElement;
    }

    protected static final class TestCase {
        protected String dateAsString;
        protected Date date;

        TestCase(String dateAsString) {
            this.dateAsString = dateAsString;
        }

        TestCase[] utcDate(int year, int month, int day, int hours, int minutes, int seconds) {
            Calendar utc = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.set(year, month - 1, day, hours, minutes, seconds);
            utc.set(Calendar.MILLISECOND, 0);
            this.date = utc.getTime();
            return new TestCase[]{this};
        }

        JsonElement getJsonElement() {
            return getJsonElementForDateString(dateAsString);
        }
    }
}
