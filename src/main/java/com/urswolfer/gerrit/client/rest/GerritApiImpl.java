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

package com.urswolfer.gerrit.client.rest;

import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.api.config.Config;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.api.projects.Projects;
import com.urswolfer.gerrit.client.rest.accounts.Accounts;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;
import com.urswolfer.gerrit.client.rest.http.HttpClientBuilderExtension;
import com.urswolfer.gerrit.client.rest.http.HttpRequestExecutor;
import com.urswolfer.gerrit.client.rest.http.accounts.AccountsRestClient;
import com.urswolfer.gerrit.client.rest.http.changes.ChangesRestClient;
import com.urswolfer.gerrit.client.rest.http.config.ConfigRestClient;
import com.urswolfer.gerrit.client.rest.http.groups.GroupsRestClient;
import com.urswolfer.gerrit.client.rest.http.projects.ProjectsRestClient;
import com.urswolfer.gerrit.client.rest.http.tools.ToolsRestClient;
import com.urswolfer.gerrit.client.rest.tools.Tools;

import java.util.function.Supplier;

/**
 * @author Urs Wolfer
 */
public class GerritApiImpl extends GerritApi.NotImplemented implements GerritRestApi {
    private final GerritRestClient gerritRestClient;
    private final GerritRestContext context;

    private final Supplier<AccountsRestClient> accountsRestClient;
    private final Supplier<ChangesRestClient> changesRestClient;
    private final Supplier<ConfigRestClient> configRestClient;
    private final Supplier<GroupsRestClient> groupsRestClient;
    private final Supplier<ProjectsRestClient> projectsRestClient;
    private final Supplier<ToolsRestClient> toolsRestClient;

    public GerritApiImpl(GerritAuthData authData,
                         HttpRequestExecutor httpRequestExecutor,
                         HttpClientBuilderExtension... httpClientBuilderExtensions) {
        this.gerritRestClient = new GerritRestClient(authData, httpRequestExecutor, httpClientBuilderExtensions);
        this.context = new GerritRestContext(gerritRestClient, new GerritJson(gerritRestClient.getGson()));
        // built here rather than in field initializers, where a lambda may not yet read a blank final
        this.accountsRestClient = Suppliers.memoize(() -> new AccountsRestClient(context));
        this.changesRestClient = Suppliers.memoize(() -> new ChangesRestClient(context));
        this.configRestClient = Suppliers.memoize(() -> new ConfigRestClient(context));
        this.groupsRestClient = Suppliers.memoize(() -> new GroupsRestClient(context));
        this.projectsRestClient = Suppliers.memoize(() -> new ProjectsRestClient(context));
        this.toolsRestClient = Suppliers.memoize(() -> new ToolsRestClient(context));
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
