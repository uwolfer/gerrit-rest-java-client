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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.api.changes.FileApi;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.UrlQuery;


import java.util.function.Supplier;


/**
 * @author Thomas Forrer
 */
public class FileApiRestClient extends FileApi.NotImplemented {

    private final GerritRestContext context;
    private final RevisionApiRestClient revisionApiRestClient;
    private final String path;

    private final Supplier<String> requestPath = Suppliers.memoize(new com.google.common.base.Supplier<String>() {
        @Override
        public String get() {
            String encodedPath = Url.encode(path);
            return revisionApiRestClient.getRequestPath() + "/files/" + encodedPath;
        }
    });

    public FileApiRestClient(GerritRestContext context,
                             RevisionApiRestClient revisionApiRestClient,
                             String path) {
        this.context = context;
        this.revisionApiRestClient = revisionApiRestClient;
        this.path = path;
    }

    @Override
    public BinaryResult content() throws RestApiException {
        String request = getRequestPath() + "/content";
        return context.get(request).binary("Failed to get file content.");
    }

    @Override
    public DiffInfo diff() throws RestApiException {
        return diffRequest().get();
    }

    @Override
    public DiffInfo diff(String base) throws RestApiException {
        return diffRequest().withBase(base).get();
    }

    @Override
    public DiffInfo diff(int parent) throws RestApiException {
        return diff(diffRequest(), parent);
    }

    @Override
    public DiffRequest diffRequest() throws RestApiException {
        return new DiffRequest() {
            @Override
            public DiffInfo get() throws RestApiException {
                return FileApiRestClient.this.diff(this);
            }
        };
    }

    private DiffInfo diff(DiffRequest diffRequest) throws RestApiException {
        return diff(diffRequest, 0);
    }

    private DiffInfo diff(DiffRequest diffRequest, int parent) throws RestApiException {
        String url = UrlQuery.of(getRequestPath() + "/diff")
            .paramIfNotEmpty("base", diffRequest.getBase())
            .paramIf(diffRequest.getContext() != null, "context", diffRequest.getContext())
            .paramIf(diffRequest.getIntraline() != null, "intraline", diffRequest.getIntraline())
            .paramIf(diffRequest.getWhitespace() != null, "whitespace", diffRequest.getWhitespace())
            .paramIfPositive("parent", parent)
            .toUrl();

        return context.get(url).as(DiffInfo.class);
    }

    protected String getRequestPath() {
        return requestPath.get();
    }
}
