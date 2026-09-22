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

import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * One REST call: the verb and path, an optional body, and what to make of the response.
 *
 * <p>Started from {@link GerritRestContext#get}, {@link GerritRestContext#post},
 * {@link GerritRestContext#put} or {@link GerritRestContext#delete}. It replaces the block every
 * endpoint used to repeat - build the path, serialize the input, call the rest client, hand the
 * result to the JSON conversion - which ran to three or four lines each across roughly 160 call
 * sites.
 *
 * <pre>
 * context.get(getRequestPath() + "/reviewers").asList(ReviewerInfo.class);
 * context.post(getRequestPath() + "/abandon").body(abandonInput).send();
 * </pre>
 */
public final class GerritRestRequest {

    private final GerritRestClient restClient;
    private final GerritJson json;
    private final HttpVerb verb;
    private final String path;
    private String body;

    GerritRestRequest(GerritRestContext context, HttpVerb verb, String path) {
        this.restClient = context.restClient();
        this.json = context.json();
        this.verb = verb;
        this.path = path;
    }

    /**
     * Serializes {@code value} as the request body.
     */
    public GerritRestRequest body(Object value) {
        this.body = json.toJson(value);
        return this;
    }

    /**
     * Serializes {@code value} as {@code type}, so that only the fields declared on that type are
     * written even when a subclass instance is passed.
     */
    public GerritRestRequest body(Object value, Type type) {
        this.body = json.toJson(value, type);
        return this;
    }

    /**
     * Sends {@code body} unchanged, for the endpoints that take something other than JSON.
     */
    public GerritRestRequest rawBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Makes the call and discards the response.
     */
    public void send() throws RestApiException {
        execute();
    }

    public JsonElement asJson() throws RestApiException {
        return execute();
    }

    /**
     * The response read as a bare JSON string, for the endpoints that return one.
     */
    public String asString() throws RestApiException {
        return execute().getAsString();
    }

    public <T> T as(Class<T> type) throws RestApiException {
        return json.as(execute(), type);
    }

    public <T> T as(Type type) throws RestApiException {
        return json.as(execute(), type);
    }

    public <T> List<T> asList(Class<T> type) throws RestApiException {
        return json.asList(execute(), type);
    }

    public <T> Map<String, T> asMap(Class<T> valueType) throws RestApiException {
        return json.asMap(execute(), valueType);
    }

    public <T> SortedMap<String, T> asSortedMap(Class<T> valueType) throws RestApiException {
        return json.asSortedMap(execute(), valueType);
    }

    public <T> SortedMap<String, List<T>> asSortedMapOfLists(Class<T> valueType) throws RestApiException {
        return json.asSortedMapOfLists(execute(), valueType);
    }

    public <T> Set<T> asSet(Class<T> type) throws RestApiException {
        return json.asSet(execute(), type);
    }

    public <T> SortedSet<T> asSortedSet(Class<T> type) throws RestApiException {
        return json.asSortedSet(execute(), type);
    }

    /**
     * The response as a stream rather than JSON, for file content and similar.
     *
     * @param failureMessage what to report if the transfer fails
     */
    public BinaryResult binary(String failureMessage) throws RestApiException {
        try {
            HttpResponse response = restClient.request(path, body, verb);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw RestApiException.wrap(failureMessage, e);
        }
    }

    /**
     * Sends a non-JSON body and discards the response.
     *
     * @param failureMessage what to report if the transfer fails
     */
    public void sendRaw(String failureMessage) throws RestApiException {
        try {
            restClient.request(path, body, verb);
        } catch (IOException e) {
            throw RestApiException.wrap(failureMessage, e);
        }
    }

    /**
     * Dispatches to the rest client's own verb methods rather than to
     * {@link GerritRestClient#requestJson}, so that what reaches the client is exactly what these
     * call sites sent before. Those methods take no body, so a request that has one goes to
     * {@code requestJson} instead - which is what they delegate to anyway - rather than dropping it.
     */
    private JsonElement execute() throws RestApiException {
        switch (verb) {
            case GET:
                return body == null ? restClient.getRequest(path) : restClient.requestJson(path, body, verb);
            case POST:
                return body == null ? restClient.postRequest(path) : restClient.postRequest(path, body);
            case PUT:
                return body == null ? restClient.putRequest(path) : restClient.putRequest(path, body);
            case DELETE:
                return body == null ? restClient.deleteRequest(path) : restClient.requestJson(path, body, verb);
            default:
                return restClient.requestJson(path, body, verb);
        }
    }
}
