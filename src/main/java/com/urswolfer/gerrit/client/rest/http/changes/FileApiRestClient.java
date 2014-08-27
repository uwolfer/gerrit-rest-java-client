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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.gerrit.extensions.api.changes.FileApi;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Thomas Forrer
 */
public class FileApiRestClient implements FileApi {

    private final GerritRestClient gerritRestClient;
    private final RevisionApiRestClient revisionApiRestClient;
    private final DiffInfoParser diffInfoParser;
    private final String path;

    private final Supplier<String> requestPath = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            try {
                String encodedPath = URLEncoder.encode(path, "UTF-8");
                return revisionApiRestClient.getRequestPath() + "/files/" + encodedPath;
            } catch (UnsupportedEncodingException e) {
                throw Throwables.propagate(e);
            }
        }
    });

    public FileApiRestClient(GerritRestClient gerritRestClient,
                             RevisionApiRestClient revisionApiRestClient,
                             DiffInfoParser diffInfoParser, String path) {
        this.gerritRestClient = gerritRestClient;
        this.revisionApiRestClient = revisionApiRestClient;
        this.diffInfoParser = diffInfoParser;
        this.path = path;
    }

    @Override
    public String content() throws RestApiException {
        String request = getRequestPath() + "/content";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        String content = jsonElement.getAsString();
        return new String(Base64.decodeBase64(content));
    }

    @Override
    public DiffInfo diff() throws RestApiException {
        String request = getRequestPath() + "/diff";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return diffInfoParser.parseDiffInfo(jsonElement);
    }

    @Override
    public DiffInfo diff(String base) throws RestApiException {
        String request = getRequestPath() + "/diff?base=" + base;
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return diffInfoParser.parseDiffInfo(jsonElement);
    }

    protected String getRequestPath() {
        return requestPath.get();
    }
}
