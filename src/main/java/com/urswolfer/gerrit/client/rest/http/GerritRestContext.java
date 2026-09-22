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

import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.RestClient.HttpVerb;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.config.ServerRestClient;

import java.util.concurrent.atomic.AtomicReference;

/**
 * What every REST client in this package needs, passed as one argument instead of several.
 *
 * <p>A context belongs to one {@link com.urswolfer.gerrit.client.rest.GerritApiImpl}, and the REST
 * clients hand it down to the sub-clients they create, so state that should be shared for the
 * lifetime of an API instance can live here. The server version is the first such state: it is
 * needed to decide which {@code ListChangesOption}s a server understands, and used to be cached per
 * {@code ChangeApi}, which cost an extra request for every change.
 */
public class GerritRestContext {

    private final GerritRestClient restClient;
    private final GerritJson json;
    private final AtomicReference<String> serverVersion = new AtomicReference<>();

    public GerritRestContext(GerritRestClient restClient, GerritJson json) {
        this.restClient = restClient;
        this.json = json;
    }

    public GerritRestClient restClient() {
        return restClient;
    }

    public GerritJson json() {
        return json;
    }

    public GerritRestRequest get(String path) {
        return new GerritRestRequest(this, HttpVerb.GET, path);
    }

    public GerritRestRequest post(String path) {
        return new GerritRestRequest(this, HttpVerb.POST, path);
    }

    public GerritRestRequest put(String path) {
        return new GerritRestRequest(this, HttpVerb.PUT, path);
    }

    public GerritRestRequest delete(String path) {
        return new GerritRestRequest(this, HttpVerb.DELETE, path);
    }

    /**
     * A call with a verb the shorthands above do not cover, such as the plain-text variants.
     */
    public GerritRestRequest request(HttpVerb verb, String path) {
        return new GerritRestRequest(this, verb, path);
    }

    /**
     * The version this Gerrit reports, read at most once per API instance.
     *
     * <p>A version a server reports is treated as fixed for the life of this context, so a Gerrit
     * upgraded underneath a long-lived API instance keeps the option set of the version it reported
     * first until a new instance is created. The
     * {@link ServerRestClient#VERSION_BEFORE_2_8} fallback is not cached: it is inferred from a
     * {@code 404}, which a proxy or a transient failure can also produce, and caching it would pin
     * the client to the smallest option set for the rest of the session.
     *
     * <p>Two callers racing here both read and reach the same answer, which is why this is not
     * synchronized. An explicit {@code config().server().getVersion()} still asks the server every
     * time, as it always has.
     */
    public String serverVersion() throws RestApiException {
        String cached = serverVersion.get();
        if (cached != null) {
            return cached;
        }
        String fetched = new ServerRestClient(this).getVersion();
        if (!ServerRestClient.VERSION_BEFORE_2_8.equals(fetched)) {
            serverVersion.set(fetched);
        }
        return fetched;
    }
}
