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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.gerrit.extensions.api.projects.ChildProjectApi;
import com.google.gerrit.extensions.common.ProjectInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

public class ChildProjectApiRestClient extends ChildProjectApi.NotImplemented implements ChildProjectApi {

    private final GerritRestContext context;

    private final String name;
    private final String parentUrl;

    public ChildProjectApiRestClient(GerritRestContext context,
                                     String parentUrl,
                                     String name) {
        this.context = context;
        this.parentUrl = parentUrl;
        this.name = name;
    }

    @Override
    public ProjectInfo get() throws RestApiException {
        return get(false);
    }

    @Override
    public ProjectInfo get(boolean recursive) throws RestApiException {
        String requestUrl = childProjectUrl();
        if (recursive) {
            requestUrl = requestUrl + "?recursive";
        }
        return context.get(requestUrl).as(ProjectInfo.class);
    }

    protected String childProjectUrl() {
        return parentUrl + "/children/" + Url.encode(name);
    }

}
