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

package com.urswolfer.gerrit.client.rest.http.common;

import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApi;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.http.HttpRequestExecutor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory stand-in for a Gerrit instance, used to pin down what the REST client puts on the
 * wire.
 *
 * <p>It hooks in at {@link HttpRequestExecutor}, which is part of the public API
 * ({@link GerritRestApiFactory#create(GerritAuthData, HttpRequestExecutor,
 * com.urswolfer.gerrit.client.rest.http.HttpClientBuilderExtension...)}), so tests exercise the
 * whole client - request building, URL encoding, JSON serialization and parsing - through
 * {@link com.google.gerrit.extensions.api.GerritApi} without touching the network and without
 * mocking any of the classes under test. That makes these tests survive refactorings of the
 * internals, which mock-based tests written against constructors and parser classes do not.
 *
 * <p>Usage:
 * <pre>
 * FakeGerritServer server = new FakeGerritServer()
 *     .stub("GET", "/changes/?q=status:open", "changes/changes.json");
 *
 * List&lt;ChangeInfo&gt; changes = server.api().changes().query("status:open").get();
 *
 * Truth.assertThat(server.trace()).containsExactly("GET /changes/?q=status:open");
 * server.verify();
 * </pre>
 *
 * <p>A request that matches no stub fails the test immediately with the request and the registered
 * stubs in the message. The exception is {@code /login/}: the client probes it once per API
 * instance - with a {@code GET}, followed by a form {@code POST} when a login and password are
 * available - and an unstubbed probe is answered with {@code 401}, which makes the client stop
 * trying for the rest of the session. Those probes are not recorded, and a test that cares about
 * them can stub {@code /login/} itself. Authentication is covered in full by
 * {@link com.urswolfer.gerrit.client.rest.http.GerritRestClientTest} against a real Jetty server.
 */
public final class FakeGerritServer {

    /**
     * The host the {@linkplain #api() API instances} returned by this fake are pointed at.
     */
    public static final String HOST = "http://gerrit.example.com";

    private static final String RESOURCE_BASE = "/com/urswolfer/gerrit/client/rest/http/";
    private static final String LOGIN_PATH = "/login/";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Map<String, StubbedResponse> stubs = new LinkedHashMap<>();
    private final Set<String> usedStubs = new LinkedHashSet<>();
    private final List<RecordedRequest> requests = new ArrayList<>();

    /**
     * Responds to {@code method path} with {@code 200} and the given JSON test resource, named
     * relative to {@code /com/urswolfer/gerrit/client/rest/http/} (for example
     * {@code "changes/changes.json"}).
     */
    public FakeGerritServer stub(String method, String path, String jsonResourceName) {
        return stub(method, path, new StubbedResponse(
            200, ContentType.APPLICATION_JSON.getMimeType(), readResource(jsonResourceName),
            Collections.<String, String>emptyMap()));
    }

    /**
     * Responds to {@code method path} with {@code 200} and the given JSON body.
     */
    public FakeGerritServer stubJson(String method, String path, String json) {
        return stub(method, path, new StubbedResponse(
            200, ContentType.APPLICATION_JSON.getMimeType(), json.getBytes(UTF_8),
            Collections.<String, String>emptyMap()));
    }

    /**
     * Responds to {@code method path} with {@code 204} and no body, as Gerrit does for requests
     * that only have a side effect.
     */
    public FakeGerritServer stubEmpty(String method, String path) {
        return stub(method, path, new StubbedResponse(
            204, null, null, Collections.<String, String>emptyMap()));
    }

    /**
     * Responds to {@code method path} with the given status code and no body.
     */
    public FakeGerritServer stubStatus(String method, String path, int statusCode) {
        return stub(method, path, new StubbedResponse(
            statusCode, null, null, Collections.<String, String>emptyMap()));
    }

    /**
     * Responds to {@code method path} with {@code 200}, a non-JSON body and the given extra
     * response headers - for the endpoints that return file content rather than JSON.
     */
    public FakeGerritServer stubRaw(String method, String path, String contentType, String body,
                                    Map<String, String> extraHeaders) {
        return stub(method, path, new StubbedResponse(
            200, contentType, body.getBytes(UTF_8), extraHeaders));
    }

    private FakeGerritServer stub(String method, String path, StubbedResponse response) {
        stubs.put(key(method, path), response);
        return this;
    }

    /**
     * A Gerrit API for anonymous access to this fake.
     */
    public GerritRestApi api() {
        return api(new GerritAuthData.Basic(HOST));
    }

    /**
     * A Gerrit API for this fake using the given credentials. Note that authenticated access makes
     * the client prefix request paths with {@code /a}.
     */
    public GerritRestApi api(GerritAuthData authData) {
        return new GerritRestApiFactory().create(authData, new RecordingExecutor());
    }

    /**
     * Every request the client made, in order; see the class comment for the one request that is
     * not recorded.
     */
    public List<RecordedRequest> requests() {
        return Collections.unmodifiableList(requests);
    }

    /**
     * Every recorded request rendered as {@code "METHOD /path?query"}, for asserting on the request
     * sequence as a whole.
     */
    public List<String> trace() {
        List<String> trace = new ArrayList<>(requests.size());
        for (RecordedRequest request : requests) {
            trace.add(request.toString());
        }
        return trace;
    }

    /**
     * The single recorded request for {@code method path}; fails when there is not exactly one.
     */
    public RecordedRequest request(String method, String path) {
        List<RecordedRequest> matches = new ArrayList<>();
        for (RecordedRequest request : requests) {
            if (request.getMethod().equals(method) && request.getPath().equals(path)) {
                matches.add(request);
            }
        }
        if (matches.size() != 1) {
            throw new AssertionError(String.format(
                "Expected exactly one '%s %s' request but found %d. Recorded requests:%n%s",
                method, path, matches.size(), join(trace())));
        }
        return matches.get(0);
    }

    /**
     * Asserts that every stub registered on this fake was actually requested, so that a stub left
     * behind by a changed URL fails the test instead of going unnoticed.
     */
    public void verify() {
        Set<String> unused = new LinkedHashSet<>(stubs.keySet());
        unused.removeAll(usedStubs);
        if (!unused.isEmpty()) {
            throw new AssertionError(String.format(
                "Stubbed but never requested:%n%s%nRecorded requests:%n%s",
                join(new ArrayList<>(unused)), join(trace())));
        }
    }

    private static String key(String method, String path) {
        return method + " " + path;
    }

    private static String join(List<String> lines) {
        if (lines.isEmpty()) {
            return "  <none>";
        }
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append("  ").append(line).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static byte[] readResource(String resourceName) {
        String resourcePath = RESOURCE_BASE + resourceName;
        InputStream in = FakeGerritServer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("No such test resource: " + resourcePath);
        }
        try (InputStream resource = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = resource.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read test resource: " + resourcePath, e);
        }
    }

    private static final class StubbedResponse {
        private final int statusCode;
        private final String contentType;
        private final byte[] body;
        private final Map<String, String> extraHeaders;

        StubbedResponse(int statusCode, String contentType, byte[] body,
                        Map<String, String> extraHeaders) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
            this.extraHeaders = extraHeaders;
        }

        HttpResponse toHttpResponse() {
            HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, statusCode, null);
            for (Map.Entry<String, String> header : extraHeaders.entrySet()) {
                response.addHeader(header.getKey(), header.getValue());
            }
            if (body != null) {
                ByteArrayEntity entity = new ByteArrayEntity(body);
                if (contentType != null) {
                    // on the entity for the client's own JSON check, and as a response header
                    // because that is where a real response carries it too
                    entity.setContentType(contentType);
                    response.addHeader("Content-Type", contentType);
                }
                response.setEntity(entity);
            }
            return response;
        }
    }

    private final class RecordingExecutor extends HttpRequestExecutor {
        @Override
        public HttpResponse execute(HttpClientBuilder client, HttpRequestBase method,
                                    HttpContext context) throws IOException {
            String verb = method.getMethod();
            String path = pathOf(method.getURI());
            StubbedResponse response = stubs.get(key(verb, path));
            if (response == null) {
                if (LOGIN_PATH.equals(path)) {
                    // the client probes /login/ once per session to find out whether the host
                    // supports gerrit-auth, first with a GET and then, when a login and password
                    // are available, with a form POST; a 401 makes it stop asking
                    return new BasicHttpResponse(HttpVersion.HTTP_1_1, 401, null);
                }
                throw new AssertionError(String.format(
                    "Unexpected request '%s %s'. Stubbed:%n%s",
                    verb, path, join(new ArrayList<>(stubs.keySet()))));
            }
            usedStubs.add(key(verb, path));
            requests.add(new RecordedRequest(verb, path, bodyOf(method)));
            return response.toHttpResponse();
        }

        private String pathOf(URI uri) {
            String query = uri.getRawQuery();
            return query == null ? uri.getRawPath() : uri.getRawPath() + '?' + query;
        }

        private String bodyOf(HttpRequestBase method) throws IOException {
            if (!(method instanceof HttpEntityEnclosingRequest)) {
                return null;
            }
            HttpEntity entity = ((HttpEntityEnclosingRequest) method).getEntity();
            return entity == null ? null : EntityUtils.toString(entity, UTF_8);
        }
    }
}
