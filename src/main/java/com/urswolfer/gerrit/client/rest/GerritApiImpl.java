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
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.api.config.Config;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.api.projects.Projects;
import com.urswolfer.gerrit.client.rest.accounts.Accounts;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.HttpClientBuilderExtension;
import com.urswolfer.gerrit.client.rest.http.HttpRequestExecutor;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsParser;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.*;
import com.urswolfer.gerrit.client.rest.http.config.ConfigRestClient;
import com.urswolfer.gerrit.client.rest.http.groups.GroupsParser;
import com.urswolfer.gerrit.client.rest.http.groups.GroupsRestClient;
import com.urswolfer.gerrit.client.rest.http.projects.BranchInfoParser;
import com.urswolfer.gerrit.client.rest.http.projects.ProjectsParser;
import com.urswolfer.gerrit.client.rest.http.projects.ProjectsRestClient;
import com.urswolfer.gerrit.client.rest.http.projects.TagInfoParser;
import com.urswolfer.gerrit.client.rest.http.tools.ToolsRestClient;
import com.urswolfer.gerrit.client.rest.tools.Tools;

/**
 * @author Urs Wolfer
 */
public class GerritApiImpl extends GerritApi.NotImplemented implements GerritRestApi {
    private final GerritRestClient gerritRestClient;

    private final Supplier<GroupsRestClient> groupsRestClient = Suppliers.memoize(new Supplier<GroupsRestClient>() {
        @Override
        public GroupsRestClient get() {
            return new GroupsRestClient(gerritRestClient, new GroupsParser(gerritRestClient.getGson()));
        }
    });

    private final Supplier<AccountsRestClient> accountsRestClient = Suppliers.memoize(new Supplier<AccountsRestClient>() {
        @Override
        public AccountsRestClient get() {
            return new AccountsRestClient(gerritRestClient, new AccountsParser(gerritRestClient.getGson()));
        }
    });

    private final Supplier<ChangesRestClient> changesRestClient = Suppliers.memoize(new Supplier<ChangesRestClient>() {
        @Override
        public ChangesRestClient get() {
            return new ChangesRestClient(
                    gerritRestClient,
                    new ChangesParser(gerritRestClient.getGson()),
                    new CommentsParser(gerritRestClient.getGson()),
                    new FileInfoParser(gerritRestClient.getGson()),
                    new DiffInfoParser(gerritRestClient.getGson()),
                    new SuggestedReviewerInfoParser(gerritRestClient.getGson()),
                    new ReviewerInfoParser(gerritRestClient.getGson()),
                    new EditInfoParser(gerritRestClient.getGson()));
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
            return new ProjectsRestClient(
                    gerritRestClient,
                    new ProjectsParser(gerritRestClient.getGson()),
                    new BranchInfoParser(gerritRestClient.getGson()),
                    new TagInfoParser(gerritRestClient.getGson()));
        }
    });

    private final Supplier<ToolsRestClient> toolsRestClient = Suppliers.memoize(new Supplier<ToolsRestClient>() {
        @Override
        public ToolsRestClient get() {
            return new ToolsRestClient(gerritRestClient);
        }
    });

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
    public Groups groups() {
        return groupsRestClient.get();
    }

    @Override
    public Projects projects() {
        return projectsRestClient.get();
    }

    @Override
    public Tools tools() {
        return toolsRestClient.get();
    }

    @Override
    public RestClient restClient() {
        return gerritRestClient;
    }
}
