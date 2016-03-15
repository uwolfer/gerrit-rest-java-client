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
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gson.Gson;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;


/**
 * This test are run against real Gerrit servers.
 *
 * @see #getData() for notice how to set up test data.
 *
 * @author Urs Wolfer
 */
public class RealServerTest {

    @DataProvider(name = "LoginData")
    public Object[][] getData() throws Exception {
        URL url = this.getClass().getResource("testhosts.json");
        if (url == null) {
            String message =
                "File 'src/test/resources/com/urswolfer/gerrit/client/rest/testhosts.json' not found.\n" +
                "Not running any " + getClass().getSimpleName() + " tests.\n" +
                "Create the json file with following content: '[[\"http://gerrit\", null, null], [\"http://host2\", \"user\", \"pw\"]]'.";
            throw new SkipException(message);
        }
        File file = new File(url.toURI());
        return new Gson().fromJson(new FileReader(file), Object[][].class);
    }

    @Test(dataProvider = "LoginData")
    public void run(String url, String user, String password) throws Exception {
        System.out.println(String.format("Testing against '%s'", url));
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData;
        if (user != null) {
            authData = new GerritAuthData.Basic(url, user, password);
        } else {
            authData = new GerritAuthData.Basic(url);
        }
        GerritRestApi gerritApi = gerritRestApiFactory.create(authData);

        System.out.println(String.format("Gerrit version: %s", gerritApi.config().server().getVersion()));

        List<ChangeInfo> changeInfoList = gerritApi.changes().query("status:merged").withLimit(1).get();

        Truth.assertThat(changeInfoList.size()).isLessThan(2);

        if (authData.isLoginAndPasswordAvailable()) {
            if (!changeInfoList.isEmpty()) {
                String id = changeInfoList.get(0).id;
                try {
                    gerritApi.accounts().self().starChange(id);
                    gerritApi.accounts().self().unstarChange(id);
                } catch (HttpStatusException exception) {
                    System.out.println("Failed starring changes. Probably too old Gerrit version (<2.8)?");
                    exception.printStackTrace();
                }
            }

            AccountInfo accountInfo = gerritApi.accounts().self().get();
            System.out.println(String.format("Username: '%s', name: '%s'", accountInfo.username, accountInfo.name));
        }
    }
}
