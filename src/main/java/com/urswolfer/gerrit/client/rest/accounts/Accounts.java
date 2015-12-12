/*
 * Copyright 2013-2015 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.accounts;

import com.google.gerrit.extensions.restapi.RestApiException;

/**
 * Required for returning extended AccountApi interface.
 *
 * @author Urs Wolfer
 */
public interface Accounts extends com.google.gerrit.extensions.api.accounts.Accounts {

    @Override
    AccountApi id(String id) throws RestApiException;

    @Override
    AccountApi id(int id) throws RestApiException;

    @Override
    AccountApi self() throws RestApiException;
}
