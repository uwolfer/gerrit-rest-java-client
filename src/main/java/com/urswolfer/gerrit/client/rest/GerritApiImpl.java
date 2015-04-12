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

package com.urswolfer.gerrit.client.rest;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.accounts.Accounts;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.api.config.Config;
import com.google.gerrit.extensions.api.projects.Projects;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpClientBuilderExtension;
import com.urswolfer.gerrit.client.rest.http.HttpRequestExecutor;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.*;
import com.urswolfer.gerrit.client.rest.http.config.ConfigRestClient;
import com.urswolfer.gerrit.client.rest.http.projects.BranchInfoParser;
import com.urswolfer.gerrit.client.rest.http.projects.ProjectsParser;
import com.urswolfer.gerrit.client.rest.http.projects.ProjectsRestClient;
import com.urswolfer.gerrit.client.rest.http.tools.ToolsRestClient;
import com.urswolfer.gerrit.client.rest.tools.Tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Urs Wolfer
 */
public class GerritApiImpl extends GerritApi.NotImplemented implements GerritRestApi {
    private final GerritRestClient gerritRestClient;

    private final Supplier<AccountsRestClient> accountsRestClient = Suppliers.memoize(new Supplier<AccountsRestClient>() {
        @Override
        public AccountsRestClient get() {
            return new AccountsRestClient(gerritRestClient, new AccountsParser(gerritRestClient.getGson()));
        }
    });

    private final Supplier<ChangesRestClient> changesRestClient = Suppliers.memoize(new Supplier<ChangesRestClient>() {
        @Override
        public ChangesRestClient get() {
            boolean supportsChangesStart;
            try {
                supportsChangesStart = GerritApiImpl.this.supportsChangesStart();
            } catch (RestApiException e) {
                supportsChangesStart = true; // assume we are on a current server
            }
            return new ChangesRestClient(
                    gerritRestClient,
                    new ChangesParser(gerritRestClient.getGson()),
                    new CommentsParser(gerritRestClient.getGson()),
                    new FileInfoParser(gerritRestClient.getGson()),
                    new DiffInfoParser(gerritRestClient.getGson()),
                    new SuggestedReviewerInfoParser(gerritRestClient.getGson()),
                    supportsChangesStart);
        }
    });

    private final Supplier<ConfigRestClient> configRestClient = Suppliers.memoize(new Supplier<ConfigRestClient>() {
        @Override
        public ConfigRestClient get() {
            return new ConfigRestClient(gerritRestClient);
        }
    });

    private final Supplier<ProjectsRestClient> projectsRestClient = Suppliers.memoize(new Supplier<ProjectsRestClient>() {
        @Override
        public ProjectsRestClient get() {
            return new ProjectsRestClient(gerritRestClient, new ProjectsParser(gerritRestClient.getGson()), new BranchInfoParser(gerritRestClient.getGson()));
        }
    });

    private final Supplier<ToolsRestClient> toolsRestClient = Suppliers.memoize(new Supplier<ToolsRestClient>() {
        @Override
        public ToolsRestClient get() {
            return new ToolsRestClient(gerritRestClient);
        }
    });

    private double serverVersion = 0.0; // cache for server version which should not change once set over the life of this object, so only look it up once

    public GerritApiImpl(GerritAuthData authData,
                         HttpRequestExecutor httpRequestExecutor,
                         HttpClientBuilderExtension... httpClientBuilderExtensions) {
        this.gerritRestClient = new GerritRestClient(authData, httpRequestExecutor, httpClientBuilderExtensions);
    }

    @Override
    public Accounts accounts() {
        return accountsRestClient.get();
    }

    @Override
    public Changes changes() {
        return changesRestClient.get();
    }

    @Override
    public Config config() {
        return configRestClient.get();
    }

    @Override
    public Projects projects() {
        return projectsRestClient.get();
    }

    @Override
    public Tools tools() {
        return toolsRestClient.get();
    }

    public boolean supportsChangesStart() throws RestApiException {
        return getServerVersion() >= 2.9;
    }

    private static double parseVersion(String version) {
        if (version == null || version.length() == 0) {
            return 0.0;
        }

        try {
            final int fd = version.indexOf('.');
            final int sd = version.indexOf('.', fd + 1);
            if (0 < sd) {
                version = version.substring(0, sd);
            }
            return Double.parseDouble(version);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getServerVersion() throws RestApiException {
        if (serverVersion == 0.0) {
            serverVersion = parseVersion(config().server().getVersion()); // idempotent, so do not worry about threading issues
        }
        return serverVersion;
    }

    // violating normal declaration style for code readability to keep this pattern close to the code that uses it
    private static final Pattern OLD_SORTKEY_PATTERN = Pattern.compile("^(.*?)(?:\\+AND\\+)?\\(resume_sortkey:[^\\)]+\\)(.*)$");

    public Changes.QueryRequest updateResumeSortKey(Changes.QueryRequest queryRequest, String sortkey) {
        StringBuilder query = new StringBuilder();
        String currentQuery = queryRequest.getQuery();
        if (currentQuery != null) {
            // strip off old resume sortkey if present
            Matcher oldSortKeyMatcher = OLD_SORTKEY_PATTERN.matcher(currentQuery);
            if (oldSortKeyMatcher.matches()) {
                currentQuery = new StringBuilder(oldSortKeyMatcher.group(1)).append(oldSortKeyMatcher.group(2)).toString();
            }
            query.append(currentQuery).append("+AND+");
        }
        query.append("(resume_sortkey:").append(sortkey).append(')');

        return queryRequest.withQuery(query.toString());
    }

}
