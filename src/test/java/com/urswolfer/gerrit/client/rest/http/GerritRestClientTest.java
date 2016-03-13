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
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApi;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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
        MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("json", "application/json");
        resourceHandler.setMimeTypes(mimeTypes);
        URL url = this.getClass().getResource(".");
        resourceHandler.setBaseResource(new FileResource(url));
        resourceHandler.setWelcomeFiles(new String[] {"changes.json", "projects.json", "account.json"});

        ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.addServlet(LoginSimulationServlet.class, "/login/");

        ServletContextHandler basicAuthContextHandler = new ServletContextHandler(ServletContextHandler.SECURITY);
        basicAuthContextHandler.setSecurityHandler(basicAuth("foo", "bar", "Gerrit Auth"));
        basicAuthContextHandler.setContextPath("/a");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {
            servletContextHandler,
            resourceHandler,
            basicAuthContextHandler
        });
        server.setHandler(handlers);

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

    private GerritRestApi getGerritApiWithJettyHost() {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        return gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl));
    }

    @Test
    public void testGetChanges() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        List<ChangeInfo> changes = gerritClient.changes().query().get();
        Truth.assertThat(changes.size()).isEqualTo(3);
    }

    @Test
    public void testGetSelfAccount() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        AccountInfo accountInfo = gerritClient.accounts().self().get();
        Truth.assertThat(accountInfo.name).isEqualTo("John Doe");
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
        Truth.assertThat(projects.size()).isEqualTo(3);
    }

    @Test
    public void testGetCommitMsgHook() throws Exception {
        GerritRestApi gerritClient = getGerritApiWithJettyHost();
        InputStream commitMessageHook = gerritClient.tools().getCommitMessageHook();
        String result = new BufferedReader(new InputStreamReader(commitMessageHook, Charsets.UTF_8)).readLine();
        Truth.assertThat(result).isEqualTo("dummy-commit-msg-hook");
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

    @Test(expectedExceptions = RestApiException.class)
    public void testInvalidJson() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.accounts().id("invalid_json").get();
    }

    @Test(expectedExceptions = RestApiException.class)
    public void testNullJson() throws Exception {
        GerritApi gerritClient = getGerritApiWithJettyHost();
        gerritClient.accounts().id("null_json").get();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testUnsupportedHttpMethod() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClient(
            new GerritAuthData.Basic(jettyUrl), new HttpRequestExecutor());
        gerritRestClient.requestRest("/invalid/", null, GerritRestClient.HttpVerb.HEAD);
    }

    /**
     * Tests authentication with a login which us handled by HTTP auth (preemptive authentication is assumed)
     * (path: "/a/changes/" isn't mapped -> status 404).
     */
    @Test
    public void testUserAuth() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl, "foo", "bar"));
        boolean catched = false;
        try {
            gerritClient.changes().query().get();
        } catch (HttpStatusException e) {
            catched = true;
            // 404 because this url does not provide a valid response (not set up in this test case)
            Truth.assertThat(e.getStatusCode()).isEqualTo(404);
        }
        Truth.assertThat(catched).isTrue();
    }

    /**
     * Tests authentication with an invalid HTTP login (preemptive authentication is assumed). Status 401 expected.
     */
    @Test
    public void testInvalidUserAuth() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl, "foox", "bar"));
        boolean catched = false;
        try {
            gerritClient.changes().query().get();
        } catch (HttpStatusException e) {
            catched = true;
            Truth.assertThat(e.getStatusCode()).isEqualTo(401);
            Truth.assertThat(e.getStatusText().toLowerCase()).contains("unauthorized");
        }
        Truth.assertThat(catched).isTrue();
    }

    /**
     * Tests that client-builder-extensions are called correctly.
     */
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

        Truth.assertThat(extendCalled[0]).isTrue();
        Truth.assertThat(extendCredentialProviderCalled[0]).isTrue();
    }

    @Test
    public void testVersion() throws Exception {
        GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
        GerritApi gerritClient = gerritRestApiFactory.create(new GerritAuthData.Basic(jettyUrl));
        String version = gerritClient.config().server().getVersion();
        Truth.assertThat(version).isEqualTo("2.10");
    }

    /**
     * When cookie "GerritAccount" is available (sent in test with "LoginServlet"),
     * "GerritAuth" string is extracted and cached.
     * Note that no username / login is NOT sent - otherwise LoginSimulationServlet would
     * not return a GerritAccount-cookie.
     */
    @Test
    public void testGerritAuthExtractionAndCache() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClient(
            new GerritAuthData.Basic(jettyUrl), new HttpRequestExecutor());
        Field loginCacheField = gerritRestClient.getClass().getDeclaredField("loginCache");
        loginCacheField.setAccessible(true);
        LoginCache loginCache = (LoginCache) loginCacheField.get(gerritRestClient);

        Truth.assertThat(loginCache.getGerritAuthOptional().isPresent()).isFalse();
        gerritRestClient.requestRest("/changes/", null, GerritRestClient.HttpVerb.GET);
        gerritRestClient.requestRest("/changes/?n=5", null, GerritRestClient.HttpVerb.GET);
        Truth.assertThat(loginCache.getHostSupportsGerritAuth()).isTrue();
        Truth.assertThat(loginCache.getGerritAuthOptional()).isPresent();

        loginCache.invalidate();
        Truth.assertThat(loginCache.getGerritAuthOptional().isPresent()).isFalse();

        // ensure that even with invalidated cache request is possible and cached filled again
        gerritRestClient.requestRest("/changes/", null, GerritRestClient.HttpVerb.GET);
        Truth.assertThat(loginCache.getGerritAuthOptional()).isPresent();
    }

    /**
     * Tests that the login cache is used correctly for a host which does NOT
     * support Gerrit-Auth method. It tries the first time to get the GerritAccount-cookie
     * and if that fails it continues to use HTTP auth.
     */
    @Test
    public void testGerritAuthNotAvailable() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClient(
            new GerritAuthData.Basic(jettyUrl, "foo", "bar"), new HttpRequestExecutor());
        Field loginCacheField = gerritRestClient.getClass().getDeclaredField("loginCache");
        loginCacheField.setAccessible(true);
        LoginCache loginCache = (LoginCache) loginCacheField.get(gerritRestClient);

        Truth.assertThat(loginCache.getGerritAuthOptional().isPresent()).isFalse();
        Truth.assertThat(loginCache.getHostSupportsGerritAuth()).isTrue();
        requestChanges(gerritRestClient);
        Truth.assertThat(loginCache.getHostSupportsGerritAuth()).isFalse();
        Truth.assertThat(loginCache.getGerritAuthOptional().isPresent()).isFalse();
        requestChanges(gerritRestClient);
        Truth.assertThat(loginCache.getGerritAuthOptional().isPresent()).isFalse();
    }

    private void requestChanges(GerritRestClient gerritRestClient) throws IOException, HttpStatusException {
        try {
            gerritRestClient.requestRest("/changes/", null, GerritRestClient.HttpVerb.GET);
        } catch (HttpStatusException e) {
            if (e.getStatusCode() != 404) { // 404 is expected since path /a/changes is not mapped
                throw e;
            }
        }
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
