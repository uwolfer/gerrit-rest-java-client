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

import com.google.common.truth.Truth;
import org.testng.annotations.Test;

/**
 * @author Urs Wolfer
 */
public class GerritAuthDataTest {
    @Test
    public void basicAuth() throws Exception {
        String host = "http://localhost:8080";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host);
        Truth.assertThat(authData.getHost()).isEqualTo(host);
        Truth.assertThat(authData.getLogin()).isEmpty();
        Truth.assertThat(authData.getPassword()).isEmpty();
        Truth.assertThat(authData.isLoginAndPasswordAvailable()).isFalse();
    }

    @Test
    public void basicAuthWithUsernameAndPassword() throws Exception {
        String host = "http://localhost:8080";
        String user = "foo";
        String password = "bar";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host, user, password);
        Truth.assertThat(authData.getHost()).isEqualTo(host);
        Truth.assertThat(authData.getLogin()).isEqualTo(user);
        Truth.assertThat(authData.getPassword()).isEqualTo(password);
        Truth.assertThat(authData.isLoginAndPasswordAvailable()).isTrue();
    }

    @Test
    public void basicAuthUrlWithTrailingSlash() throws Exception {
        String host = "http://localhost:8080/r/";
        GerritAuthData.Basic authData = new GerritAuthData.Basic(host);
        Truth.assertThat(authData.getHost()).isEqualTo("http://localhost:8080/r");
    }
}
