package com.urswolfer.gerrit.client.rest.http.projects;

import java.io.IOException;

import org.apache.http.HttpResponse;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gerrit.extensions.api.projects.FileApi;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.util.BinaryResultUtils;

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

  public FileApiRestClient(GerritRestClient gerritRestClient, BranchApiRestClient branchApi, String path) {
    this.gerritRestClient = gerritRestClient;
    this.branchApi = branchApi;
    this.path = path;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gerrit.extensions.api.projects.FileApi#content()
   */
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
