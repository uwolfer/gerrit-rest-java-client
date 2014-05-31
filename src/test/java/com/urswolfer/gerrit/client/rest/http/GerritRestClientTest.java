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

package com.urswolfer.gerrit.client.rest.http;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * @author Urs Wolfer
 */
public class GerritRestClientTest {
    private String jettyUrl;

    @BeforeClass
    public void startJetty() throws Exception {
        Server server = new Server(0);

        ResourceHandler resourceHandler = new ResourceHandler();
        URL url = this.getClass().getResource(".");
        resourceHandler.setBaseResource(new FileResource(url));
        resourceHandler.setWelcomeFiles(new String[] {"changes.json", "projects.json", "account.json"});
        server.setHandler(resourceHandler);

        server.start();

        Connector connector = server.getConnectors()[0];
        String host = "localhost";
        int port = connector.getLocalPort();
        jettyUrl = String.format("http://%s:%s", host, port);
    }

    @Test(expectedExceptions = RestApiException.class)
    public void testInvalidHost() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritAuthData.Basic authData = new GerritAuthData.Basic("http://averyinvaliddomainforgerritresttest.com:8089");
        GerritApi gerritClient = gerritRestApiFactory.create(authData);
        gerritClient.changes().query().get();
    }

    private GerritApi getGerritApiWithJettyHost() {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        return gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl));
    }

    @Test
    public void testGetChanges() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        List<ChangeInfo> changes = gerritClient.changes().query().get();
        Assert.assertEquals(3, changes.size());
    }

    @Test
    public void testGetSelfAccount() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        AccountInfo accountInfo = gerritClient.accounts().self().get();
        Assert.assertEquals("John Doe", accountInfo.name);
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testGetInvalidAccount() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.accounts().id("invalid").get();
    }

    @Test
    public void testGetProjects() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        List<ProjectInfo> projects = gerritClient.projects().list().get();
        Assert.assertEquals(3, projects.size());
    }

    @Test
    public void testGetCommitMsgHook() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        InputStream commitMessageHook = gerritClient.tools().getCommitMessageHook();
        String result = CharStreams.toString(new InputStreamReader(commitMessageHook, Charsets.UTF_8));
        Assert.assertEquals("dummy-commit-msg-hook\n", result);
    }

    @Test(enabled = false) // requires running Gerrit instance
    public void testBasicRestCallToLocalhost() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic("http://localhost:8080"));
        List<ChangeInfo> changes = gerritClient.changes().query().get();
        System.out.println(String.format("Got %s changes.", changes.size()));
        System.out.println(changes);
    }

    @Test(enabled = false) // requires running Gerrit instance
    public void testBasicRestCallToLocalhostProjects() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic("http://localhost:8080"));
        List<ProjectInfo> projects = gerritClient.projects().list().get();
        System.out.println(String.format("Got %s projects.", projects.size()));
        System.out.println(projects);
    }

    @Test(enabled = false) // requires running Gerrit instance
    public void testBasicRestCallToLocalhostProjectsQuery() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic("http://localhost:8080"));
        List<ProjectInfo> projects = gerritClient.projects().list().withLimit(1).withDescription(true).get();
        System.out.println(String.format("Got %s projects.", projects.size()));
        System.out.println(projects);
    }
}
