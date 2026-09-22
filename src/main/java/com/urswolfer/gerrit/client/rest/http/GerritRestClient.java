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

import static org.apache.http.HttpStatus.SC_FORBIDDEN;

import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.RestClient;
import com.urswolfer.gerrit.client.rest.gson.GsonFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Sends the requests: works out the URI, asks {@link GerritAuthenticator} whether a session token
 * has to go with it, runs it on a client from {@link GerritHttpClientFactory}, and hands the
 * response to {@link HttpResponses}.
 *
 * @author Urs Wolfer
 */
public class GerritRestClient implements RestClient {

    private static final Gson GSON = GsonFactory.create();
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom().setNormalizeUri(false).build();

    private final GerritAuthData authData;
    private final HttpRequestExecutor httpRequestExecutor;
    private final GerritHttpClientFactory httpClientFactory;
    private final GerritAuthenticator authenticator;

    public GerritRestClient(GerritAuthData authData,
                            HttpRequestExecutor httpRequestExecutor,
                            HttpClientBuilderExtension... httpClientBuilderExtensions) {
        this.authData = authData;
        this.httpRequestExecutor = httpRequestExecutor;

        BasicCookieStore cookieStore = new BasicCookieStore();
        this.httpClientFactory =
            new GerritHttpClientFactory(authData, Arrays.asList(httpClientBuilderExtensions), cookieStore);
        this.authenticator = new GerritAuthenticator(authData, httpRequestExecutor, cookieStore);
    }

    /**
     * The login state this client built up, for the tests that assert on it.
     */
    LoginCache loginCache() {
        return authenticator.loginCache();
    }

    @Override
    public Gson getGson() {
        return GSON;
    }

    @Override
    public JsonElement getRequest(String path) throws RestApiException {
        return requestJson(path, null, HttpVerb.GET);
    }

    @Override
    public JsonElement postRequest(String path) throws RestApiException {
        return postRequest(path, null);
    }

    @Override
    public JsonElement postRequest(String path, String requestBody) throws RestApiException {
        return requestJson(path, requestBody, HttpVerb.POST);
    }

    @Override
    public JsonElement putRequest(String path) throws RestApiException {
        return putRequest(path, null);
    }

    @Override
    public JsonElement putRequest(String path, String requestBody) throws RestApiException {
        return requestJson(path, requestBody, HttpVerb.PUT);
    }

    @Override
    public JsonElement deleteRequest(String path) throws RestApiException {
        return requestJson(path, null, HttpVerb.DELETE);
    }

    @Override
    public JsonElement requestJson(String path, String requestBody, HttpVerb verb) throws RestApiException {
        try {
            HttpResponse response = requestRest(path, requestBody, verb);

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return null;
            }

            HttpResponses.checkContentType(entity);

            JsonElement ret = HttpResponses.parseJson(entity.getContent());
            if (ret.isJsonNull()) {
                throw RestApiException.wrap("Unexpectedly empty response.", null);
            }
            return ret;
        } catch (IOException e) {
            throw RestApiException.wrap("Request failed.", e);
        }
    }

    @Override
    public HttpResponse requestRest(String path,
                                    String requestBody,
                                    HttpVerb verb) throws IOException, HttpStatusException {
        BasicHeader acceptHeader = new BasicHeader("Accept", HttpResponses.JSON_MIME_TYPE);
        return request(path, requestBody, verb, false, acceptHeader);
    }

    @Override
    public HttpResponse request(String path,
                                String requestBody,
                                HttpVerb verb,
                                Header... headers) throws IOException, HttpStatusException {
        return request(path, requestBody, verb, false, headers);
    }

    private HttpResponse request(String path,
                                 String requestBody,
                                 HttpVerb verb,
                                 boolean isRetry,
                                 Header... headers) throws IOException, HttpStatusException {
        HttpContext httpContext = new BasicHttpContext();
        HttpClientBuilder client = httpClientFactory.create(httpContext);

        Optional<String> gerritAuthOptional = authenticator.authenticate(client, httpContext);

        HttpRequestBase method = HttpRequests.create(verb, uri(path, gerritAuthOptional), requestBody);

        if (gerritAuthOptional.isPresent()) {
            method.addHeader("X-Gerrit-Auth", gerritAuthOptional.get());
        }

        for (Header header : headers) {
            method.addHeader(header);
        }

        method.setConfig(REQUEST_CONFIG);
        HttpResponse response = httpRequestExecutor.execute(client, method, httpContext);

        if (!isRetry && response.getStatusLine().getStatusCode() == SC_FORBIDDEN && authenticator.hasSession()) {
            // handle expired sessions: try again with a fresh login, asking for what was asked for
            // the first time - a plain-text or binary request must not come back as JSON
            authenticator.invalidateSession();
            EntityUtils.consumeQuietly(response.getEntity());
            response = request(path, requestBody, verb, true, headers);
        }

        HttpResponses.checkStatusCode(response);

        return response;
    }

    private String uri(String path, Optional<String> gerritAuthOptional) {
        String uri = authData.getHost();
        // only use /a when http login is required (i.e. we haven't got a gerrit-auth cookie)
        // it would work in most cases also with /a, but it breaks with HTTP digest auth ("Forbidden" returned)
        if (authData.isLoginAndPasswordAvailable() && !gerritAuthOptional.isPresent()) {
            uri += "/a";
        }
        return uri + path;
    }
}
