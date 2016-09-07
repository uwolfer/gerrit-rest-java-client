// Copyright (C) 2016 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.*;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

public class VerifyStatusApiRestClient extends VerifyStatusApi.NotImplemented
    implements VerifyStatusApi {

  private final GerritRestClient gerritRestClient;
  private final RevisionApiRestClient revisionApiRestClient;

  public VerifyStatusApiRestClient(GerritRestClient gerritRestClient,
      RevisionApiRestClient revisionApiRestClient) {
    this.gerritRestClient = gerritRestClient;
    this.revisionApiRestClient = revisionApiRestClient;
  }

  @Override
  public void verifications(VerifyInput verifyInput) throws RestApiException {
    String request =
        revisionApiRestClient.getRequestPath() + "/verify-status~verifications";
    String json = gerritRestClient.getGson().toJson(verifyInput);
    gerritRestClient.postRequest(request, json);
  }
}