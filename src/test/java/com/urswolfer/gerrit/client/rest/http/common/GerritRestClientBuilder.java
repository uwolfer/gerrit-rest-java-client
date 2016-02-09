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

package com.urswolfer.gerrit.client.rest.http.common;

import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import org.apache.http.HttpResponse;
import org.easymock.EasyMock;

/**
 * @author Thomas Forrer
 */
public final class GerritRestClientBuilder {
    private final GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);

    public GerritRestClient get() {
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    public GerritRestClientBuilder expectGet(String url, JsonElement result) throws Exception {
        EasyMock.expect(gerritRestClient.getRequest(url)).andReturn(result).once();
        return this;
    }

    public GerritRestClientBuilder expectGet(String url, Throwable throwable) throws Exception {
        EasyMock.expect(gerritRestClient.getRequest(url)).andThrow(throwable).once();
        return this;
    }

    public GerritRestClientBuilder expectPut(String url) throws Exception {
        return expectPut(url, EasyMock.createMock(JsonElement.class));
    }

    public GerritRestClientBuilder expectPut(String url, JsonElement result) throws Exception {
        EasyMock.expect(gerritRestClient.putRequest(url)).andReturn(result).once();
        return this;
    }

    public GerritRestClientBuilder expectPut(String url, String requestBody, JsonElement result) throws Exception {
        EasyMock.expect(gerritRestClient.putRequest(url, requestBody)).andReturn(result).once();
        return this;
    }

    public GerritRestClientBuilder expectPost(String url) throws Exception {
        EasyMock.expect(gerritRestClient.postRequest(url))
                .andReturn(EasyMock.createMock(JsonElement.class)).once();
        return this;
    }

    public GerritRestClientBuilder expectPost(String url, String requestBody) throws Exception {
        return expectPost(url, requestBody, EasyMock.createMock(JsonElement.class));
    }

    public GerritRestClientBuilder expectPost(String url, String requestBody, JsonElement result) throws Exception {
        EasyMock.expect(gerritRestClient.postRequest(url, requestBody))
                .andReturn(result).once();
        return this;
    }

    public GerritRestClientBuilder expectDelete(String url) throws Exception {
        EasyMock.expect(gerritRestClient.deleteRequest(url))
                .andReturn(EasyMock.createMock(JsonElement.class)).once();
        return this;
    }

    public GerritRestClientBuilder expectRequest(String path, String requestBody, GerritRestClient.HttpVerb verb,
                                                 HttpResponse result) throws Exception {
        EasyMock.expect(gerritRestClient.request(path, requestBody, verb)).andReturn(result).once();
        return this;
    }

    public GerritRestClientBuilder expectGetGson() {
        EasyMock.expect(gerritRestClient.getGson()).andReturn(AbstractParserTest.getGson()).once();
        return this;
    }
}
