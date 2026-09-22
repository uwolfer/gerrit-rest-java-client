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

import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.api.projects.CommitApi;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;


public class CommitApiRestClient extends CommitApi.NotImplemented implements CommitApi {

    private final GerritRestContext context;
    private final ProjectApiRestClient projectApiRestClient;

    private final String commit;

    public CommitApiRestClient(GerritRestContext context,
                               ProjectApiRestClient projectApiRestClient,
                               String commit) {
        this.context = context;
        this.projectApiRestClient = projectApiRestClient;
        this.commit = commit;
    }

    @Override
    public CommitInfo get() throws RestApiException {
        return context.get(commitURL()).as(CommitInfo.class);
    }


    @Override
    public IncludedInInfo includedIn() throws RestApiException {
        return context.get(commitURL() + "/in").as(IncludedInInfo.class);
    }

    protected String commitURL() {
        return projectApiRestClient.projectsUrl() + "/commits/" + Url.encode(commit);
    }
}
