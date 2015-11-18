package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.base.Throwables;
import com.google.gerrit.extensions.api.projects.BranchApi;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.FileApi;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

/**
 * Implementation of <code>BranchApi</code>.
 * 
 * @author Ingo Rissmann
 */
public class BranchApiRestClient extends BranchApi.NotImplemented implements BranchApi {
  private final GerritRestClient gerritRestClient;
  private final BranchInfoParser branchInfoParser;
  private final ProjectApiRestClient projectApiRestClient;
  private final String name;

  /**
   * Constructor.
   * 
   * @param gerritRestClient Instance of the Gerrit REST api client.
   * @param branchInfoParser A parser for the REST Jason answer.  
   * @param projectApiRestClient Reference to the project the branch is belonging to.
   * @param name Name of the branch.
   */
  public BranchApiRestClient(GerritRestClient gerritRestClient, BranchInfoParser branchInfoParser,
      ProjectApiRestClient projectApiRestClient, String name) {
    this.gerritRestClient = gerritRestClient;
    this.branchInfoParser = branchInfoParser;
    this.projectApiRestClient = projectApiRestClient;
    this.name = name;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gerrit.extensions.api.projects.BranchApi#get()
   */
  @Override
  public BranchInfo get() throws RestApiException {
    try {
      JsonElement jsonElement = gerritRestClient.getRequest(branchUrl());
      return branchInfoParser.parseBranchInfos(jsonElement).get(0);
    } catch (RestApiException e) {
      throw Throwables.propagate(e);
    }
  }

  /* (non-Javadoc)
   * @see com.google.gerrit.extensions.api.projects.BranchApi.NotImplemented#file(java.lang.String)
   */
  public FileApi file(String path) throws RestApiException {
    return new FileApiRestClient(gerritRestClient, this, path);
  }

  /**
   * Returns the path to request the branch from the Gerrit - REST API.
   * 
   * @return
   */
  protected String branchUrl() {
    return projectApiRestClient.projectsUrl() + "/branches/" + name;
  }

}
