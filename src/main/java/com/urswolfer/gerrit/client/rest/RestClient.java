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

import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * This interface provides basic HTTP access to the REST interface of a Gerrit instance.
 * All HTTP operations support authentication and other stuff required for REST (or basic HTTP) calls.
 *
 * @author Urs Wolfer
 */
public interface RestClient {
    /**
     * Returns Gson with optimal configuration for Gerrit REST API.
     */
    Gson getGson();

    /**
     * Shortcut for {@code #requestJson} for doing {@code GET} operation.
     */
    JsonElement getRequest(String path) throws RestApiException;

    /**
     * Shortcut for {@code #requestJson} for doing {@code POST} operation.
     */
    JsonElement postRequest(String path) throws RestApiException;

    /**
     * Shortcut for {@code #requestJson} for doing {@code POST} operation with a request body.
     */
    JsonElement postRequest(String path, String requestBody) throws RestApiException;

    /**
     * Shortcut for {@code #requestJson} for doing {@code PUT} operation.
     */
    JsonElement putRequest(String path) throws RestApiException;

    /**
     * Shortcut for {@code #requestJson} for doing {@code PUT} operation with a request body.
     */
    JsonElement putRequest(String path, String requestBody) throws RestApiException;

    /**
     * Shortcut for {@code #requestJson} for doing {@code DELETE} operation.
     */
    JsonElement deleteRequest(String path) throws RestApiException;

    /**
     * Executes a request and returns a JSON response.
     */
    JsonElement requestJson(String path, String requestBody, HttpVerb verb) throws RestApiException;

    /**
     * Executes a request with Accept-header set to "application/json" and returns plain response.
     */
    HttpResponse requestRest(String path,
                             String requestBody,
                             HttpVerb verb) throws IOException, HttpStatusException;

    /**
     * Executes a HTTP request and returns plain response. Can be used to request non-JSON resources.
     */
    HttpResponse request(String path,
                         String requestBody,
                         HttpVerb verb,
                         Header... headers) throws IOException, HttpStatusException;

    enum HttpVerb {
        GET, POST, DELETE, HEAD, PUT
    }
}
