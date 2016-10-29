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

package com.urswolfer.gerrit.client.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * You can get the Gerrit REST client library version with this class.
 *
 * @author Urs Wolfer
 */
public class Version {

    private static final String VERSION;

    static {
        try {
            VERSION = getVersionFromProperties();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getVersionFromProperties() throws IOException {
        InputStream inputStream = Version.class.getResourceAsStream("/version.properties");
        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("gerrit-rest-java-client.version");
        } finally {
            inputStream.close();
        }
    }

    public static String get() {
        return VERSION;
    }

}
