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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.gerrit.extensions.api.changes.ReviewerApi;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;

import java.util.Map;

/**
 * @author Chenglong Sun
 */
public class ReviewerApiRestClient extends ReviewerApi.NotImplemented implements ReviewerApi {

    private final GerritRestClient gerritRestClient;
    private final ChangeApiRestClient changeApiRestClient;
    private final Integer accountId;

    public ReviewerApiRestClient(GerritRestClient gerritRestClient,
                                 ChangeApiRestClient changeApiRestClient,
                                 Integer accountId) {
        this.gerritRestClient = gerritRestClient;
        this.changeApiRestClient = changeApiRestClient;
        this.accountId = accountId;
    }

    @Override
    public Map<String, Short> votes() throws RestApiException {
        String request = getRequestPath() + "/votes";
        JsonElement jsonElement = gerritRestClient.getRequest(request);
        return gerritRestClient.getGson().fromJson(jsonElement, new TypeToken<Map<String, Short>>() {}.getType());
    }

    @Override
    public void deleteVote(String label) throws RestApiException {
        String request = getRequestPath() + "/votes/" + label;
        gerritRestClient.deleteRequest(request);
    }

    protected String getRequestPath() {
        return changeApiRestClient.getRequestPath() + "/reviewers/" + accountId;
    }
}
