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
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
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

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SECURITY);
        context.setSecurityHandler(basicAuth("foo", "bar", "Gerrit Auth"));
        context.setContextPath("/a");
        resourceHandler.setHandler(context);

        server.start();

        Connector connector = server.getConnectors()[0];
        String host = "localhost";
        int port = connector.getLocalPort();
        jettyUrl = String.format("http://%s:%s", host, port);
    }

    private static SecurityHandler basicAuth(String username, String password, String realm) {
        HashLoginService loginService = new HashLoginService();
        loginService.putUser(username, Credential.getCredential(password), new String[]{"user"});
        loginService.setName(realm);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__DIGEST_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("realm");
        csh.addConstraintMapping(constraintMapping);
        csh.setLoginService(loginService);
        return csh;
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

    @Test(expectedExceptions = HttpStatusException.class)
    public void testStarNotLoggedIn() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.accounts().self().starChange("1");
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testUnstarNotLoggedIn() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.accounts().self().unstarChange("1");
    }

    @Test(expectedExceptions = HttpStatusException.class)
    public void testAbandonNotLoggedIn() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.changes().id(1).abandon();
    }

    @Test
    public void testUserAuth() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl, "foo", "bar"));
        try {
            gerritClient.changes().query().get();
        } catch (HttpStatusException e) {
            Assert.assertEquals(e.getStatusCode(), 404);
        }
    }

    @Test
    public void testInvalidUserAuth() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl, "foox", "bar"));
        try {
            gerritClient.changes().query().get();
        } catch (HttpStatusException e) {
            Assert.assertEquals(e.getStatusCode(), 401);
            Assert.assertTrue(e.getStatusText().toLowerCase().contains("unauthorized"));
        }
    }

    @Test
    public void testHttpClientBuilderExtension() throws Exception {
        final boolean[] extendCalled = {false};
        final boolean[] extendCredentialProviderCalled = {false};
        HttpClientBuilderExtension httpClientBuilderExtension = new HttpClientBuilderExtension() {
            @Override
            public HttpClientBuilder extend(HttpClientBuilder httpClientBuilder, GerritAuthData authData) {
                extendCalled[0] = true;
                return super.extend(httpClientBuilder, authData);
            }

            @Override
            public CredentialsProvider extendCredentialProvider(HttpClientBuilder httpClientBuilder, CredentialsProvider credentialsProvider, GerritAuthData authData) {
                extendCredentialProviderCalled[0] = true;
                return super.extendCredentialProvider(httpClientBuilder, credentialsProvider, authData);
            }
        };

        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl), httpClientBuilderExtension);
        gerritClient.changes().query().get();

        Assert.assertEquals(extendCalled[0], true);
        Assert.assertEquals(extendCredentialProviderCalled[0], true);
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
