/*
 * Copyright 2026 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http;

import com.urswolfer.gerrit.client.rest.GerritAuthData;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * Answers a request for a login with Gerrit's only where Gerrit's host asks, not where a request
 * was redirected to another host which asks too, such as the sign-in page of an authentication
 * proxy. The credentials themselves stay for any host, so that an HTTP proxy which asks for the
 * same login, as some do, still gets it.
 */
class GerritHostAuthenticationStrategy extends TargetAuthenticationStrategy {

    private final GerritAuthData authData;

    GerritHostAuthenticationStrategy(GerritAuthData authData) {
        this.authData = authData;
    }

    @Override
    public boolean isAuthenticationRequested(HttpHost authhost, HttpResponse response, HttpContext context) {
        boolean gerrit = GerritHostOnlyInterceptor.hostName(authData.getHost()) == null // not to tell, as before
            || GerritHostOnlyInterceptor.isGerritHost(authData.getHost(), authhost, false);
        return gerrit && super.isAuthenticationRequested(authhost, response, context);
    }
}
