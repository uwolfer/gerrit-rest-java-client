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

package com.urswolfer.gerrit.client.rest.http.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.urswolfer.gerrit.client.rest.gson.GsonFactory;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * @author Thomas Forrer
 */
public abstract class AbstractParserTest {
    protected JsonElement getJsonElement(String resourceName) throws Exception {
        File file = getFile(resourceName);
        return new JsonParser().parse(new FileReader(file));
    }

    protected static Gson getGson() {
        return GsonFactory.create();
    }

    protected File getFile(String resourceName) throws Exception {
        URL url = this.getClass().getResource(resourceName);
        return new File(url.toURI());
    }
}
