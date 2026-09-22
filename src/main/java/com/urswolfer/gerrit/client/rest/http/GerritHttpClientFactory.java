/*
 * Copyright 2013-2026 Urs Wolfer
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

import static com.urswolfer.gerrit.client.rest.http.PreemptiveAuthHttpRequestInterceptor.PREEMPTIVE_AUTH;

import com.urswolfer.gerrit.client.rest.GerritAuthData;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the http client each request runs on: timeouts, redirect handling, the shared cookie
 * store, credentials, and whatever the {@link HttpClientBuilderExtension}s the caller passed want
 * to change.
 *
 * <p>A client is built per request rather than kept, which is what makes the
 * {@linkplain #credentialsProvider() one-shot credentials provider} below safe.
 */
class GerritHttpClientFactory {

    private static final int CONNECTION_TIMEOUT_MS = 300000;

    private final GerritAuthData authData;
    private final List<HttpClientBuilderExtension> httpClientBuilderExtensions;
    private final BasicCookieStore cookieStore;

    GerritHttpClientFactory(GerritAuthData authData,
                            List<HttpClientBuilderExtension> httpClientBuilderExtensions,
                            BasicCookieStore cookieStore) {
        this.authData = authData;
        this.httpClientBuilderExtensions = httpClientBuilderExtensions;
        this.cookieStore = cookieStore;
    }

    HttpClientBuilder create(HttpContext httpContext) {
        HttpClientBuilder client = HttpClients.custom();

        client.useSystemProperties(); // see also: com.intellij.util.net.ssl.CertificateManager

        // we need to get redirected result after login (which is done with POST) for extracting xGerritAuth
        client.setRedirectStrategy(new LaxRedirectStrategy());

        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        RequestConfig.Builder requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT_MS) // how long it takes to connect to remote host
                .setSocketTimeout(CONNECTION_TIMEOUT_MS) // how long it takes to retrieve data from remote host
                .setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS);
        client.setDefaultRequestConfig(requestConfig.build());

        CredentialsProvider credentialsProvider = credentialsProvider();
        client.setDefaultCredentialsProvider(credentialsProvider);

        if (authData.isLoginAndPasswordAvailable()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(authData.getLogin(), authData.getPassword()));

            BasicScheme basicAuth = new BasicScheme();
            httpContext.setAttribute(PREEMPTIVE_AUTH, basicAuth);
            client.addInterceptorFirst(new PreemptiveAuthHttpRequestInterceptor(authData));
        }

        client.addInterceptorLast(new UserAgentHttpRequestInterceptor());

        for (HttpClientBuilderExtension httpClientBuilderExtension : httpClientBuilderExtensions) {
            client = httpClientBuilderExtension.extend(client, authData);
            CredentialsProvider extended =
                httpClientBuilderExtension.extendCredentialProvider(client, credentialsProvider, authData);
            if (extended != credentialsProvider) {
                // the extension's contract is that what it returns is used from here on; only set it
                // when it is a different provider, so an extension that installed its own in extend()
                // and handed back the one it was given keeps what it installed
                client.setDefaultCredentialsProvider(extended);
                credentialsProvider = extended;
            }
        }

        return client;
    }

    /**
     * With this impl, it only returns the same credentials once. Otherwise it's possible that a loop will occur.
     * When server returns status code 401, the HTTP client provides the same credentials forever.
     * Since we create a new HTTP client for every request, we can handle it this way.
     */
    private BasicCredentialsProvider credentialsProvider() {
        return new BasicCredentialsProvider() {
            private final Set<AuthScope> authAlreadyTried = new HashSet<>();

            @Override
            public Credentials getCredentials(AuthScope authscope) {
                if (authAlreadyTried.contains(authscope)) {
                    return null;
                }
                authAlreadyTried.add(authscope);
                return super.getCredentials(authscope);
            }
        };
    }
}
