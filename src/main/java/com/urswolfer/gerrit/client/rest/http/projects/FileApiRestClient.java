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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.api.projects.FileApi;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class FileApiRestClient implements FileApi {
    private final GerritRestClient gerritRestClient;
    private final BranchApiRestClient branchApi;
    private final String path;

    private final Supplier<String> requestPath = Suppliers.memoize(new Supplier<String>() {
        @Override
        public String get() {
            String encodedPath = Url.encode(path);
            return branchApi.branchUrl() + "/files/" + encodedPath;
        }
    });

    public FileApiRestClient(GerritRestClient gerritRestClient,
                             BranchApiRestClient branchApi,
                             String path) {
        this.gerritRestClient = gerritRestClient;
        this.branchApi = branchApi;
        this.path = path;
    }

    @Override
    public BinaryResult content() throws RestApiException {
        String request = getRequestPath() + "/content";
        try {
            HttpResponse response = gerritRestClient.request(request, null, GerritRestClient.HttpVerb.GET);
            return BinaryResultUtils.createBinaryResult(response);
        } catch (IOException e) {
            throw new RestApiException("Failed to get file content.", e);
        }
    }

    protected String getRequestPath() {
        return requestPath.get();
    }
}
