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

package com.urswolfer.gerrit.client.rest.http.accounts;

import com.google.gerrit.extensions.api.accounts.EmailApi;
import com.google.gerrit.extensions.common.EmailInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.urswolfer.gerrit.client.rest.http.GerritRestContext;

public class EmailApiRestClient extends EmailApi.NotImplemented implements EmailApi {


    private final GerritRestContext context;
    private final String name;
    private final String email;

    public EmailApiRestClient(GerritRestContext context,
                              String name,
                              String email) {
        this.context = context;
        this.name = name;
        this.email = email;
    }

    @Override
    public EmailInfo get() throws RestApiException {
        return context.get(getRequestPath()).as(EmailInfo.class);
    }

    @Override
    public void delete() throws  RestApiException {
        context.delete(getRequestPath()).send();
    }

    @Override
    public void setPreferred() throws  RestApiException {
        context.put(getRequestPath() + "/preferred").send();
    }

    private String getRequestPath() {
        return "/accounts/" + Url.encode(name) + "/emails/" + email;
    }
}
