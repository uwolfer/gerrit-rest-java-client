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

package com.urswolfer.gerrit.client.rest;

import com.google.common.base.Throwables;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Urs Wolfer
 */
public class Version {

    private static final String PLUGIN_VERSION;

    static {
        try {
            InputStream inputStream = Version.class.getResourceAsStream("/version.properties");
            PLUGIN_VERSION = getVersionProperties(inputStream);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static String getVersionProperties(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();
        return properties.getProperty("gerrit-rest-java-client.version");
    }

    public static String get() {
        return PLUGIN_VERSION;
    }

}
