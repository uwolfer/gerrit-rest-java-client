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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.base.Throwables;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.FileApi;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

/**
 * @author Ingo Rissmann
 */
public class BranchApiRestClient extends BranchApi.NotImplemented implements BranchApi {
  private final GerritRestClient gerritRestClient;
  private final BranchInfoParser branchInfoParser;
  private final ProjectApiRestClient projectApiRestClient;
  private final String name;

  public BranchApiRestClient(GerritRestClient gerritRestClient, BranchInfoParser branchInfoParser,
      ProjectApiRestClient projectApiRestClient, String name) {
    this.gerritRestClient = gerritRestClient;
    this.branchInfoParser = branchInfoParser;
    this.projectApiRestClient = projectApiRestClient;
    this.name = name;

  }

  @Override
  public BranchInfo get() throws RestApiException {
    try {
      JsonElement jsonElement = gerritRestClient.getRequest(branchUrl());
      return branchInfoParser.parseBranchInfos(jsonElement).get(0);
    } catch (RestApiException e) {
      throw Throwables.propagate(e);
    }
  }

  public FileApi file(String path) throws RestApiException {
    return new FileApiRestClient(gerritRestClient, this, path);
  }

  protected String branchUrl() {
    return projectApiRestClient.projectsUrl() + "/branches/" + name;
  }

}
