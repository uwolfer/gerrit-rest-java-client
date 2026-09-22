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

import com.google.common.io.CharStreams;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * What to make of a response: whether its status says the request failed, whether its body is the
 * JSON we asked for, and how to read that body.
 *
 * <p>Both the request path and the login handshake check status codes, which is why these do not
 * live on either of them.
 */
final class HttpResponses {

    static final String JSON_MIME_TYPE = ContentType.APPLICATION_JSON.getMimeType();

    private HttpResponses() {}

    /**
     * @throws HttpStatusException on any error (client 4xx and server 5xx).
     */
    static void checkStatusCode(HttpResponse response) throws HttpStatusException, IOException {
        checkStatusCodeError(response, 400, 499);
        checkStatusCodeServerError(response);
    }

    /**
     * @throws HttpStatusException on server error (5xx).
     */
    static void checkStatusCodeServerError(HttpResponse response) throws HttpStatusException, IOException {
        checkStatusCodeError(response, 500, 599);
    }

    private static void checkStatusCodeError(HttpResponse response, int errorIfMin, int errorIfMax)
            throws HttpStatusException, IOException {
        StatusLine statusLine = response.getStatusLine();
        int code = statusLine.getStatusCode();
        if (code >= errorIfMin && code <= errorIfMax) {
            throwHttpStatusException(response);
        }
    }

    private static void throwHttpStatusException(HttpResponse response) throws IOException, HttpStatusException {
        StatusLine statusLine = response.getStatusLine();
        String body = "<empty>";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = EntityUtils.toString(entity).trim();
        }
        String message = String.format("Request not successful. Message: %s. Status-Code: %s. Content:%n%s.",
                statusLine.getReasonPhrase(), statusLine.getStatusCode(), body);
        throw new HttpStatusException(statusLine.getStatusCode(), statusLine.getReasonPhrase(), message);
    }

    static void checkContentType(HttpEntity entity) throws RestApiException, IOException {
        Header contentType = entity.getContentType();
        if (contentType != null && !contentType.getValue().contains(JSON_MIME_TYPE)) {
            throw RestApiException.wrap(String.format("Expected JSON but got '%s'. Content:%n%s",
                contentType.getValue(), EntityUtils.toString(entity).trim()), null);
        }
    }

    static JsonElement parseJson(InputStream response) throws IOException {
        Reader reader = new InputStreamReader(response, Consts.UTF_8);
        try {
            return JsonParser.parseReader(reader);
        } catch (JsonSyntaxException jse) {
            throw new IOException(String.format("Couldn't parse response: %n%s", CharStreams.toString(reader)), jse);
        } finally {
            reader.close();
        }
    }
}
