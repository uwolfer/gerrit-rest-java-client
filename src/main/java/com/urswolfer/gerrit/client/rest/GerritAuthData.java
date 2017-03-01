/*
 * Copyright 2013 Urs Wolfer
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

import com.google.common.base.Strings;

/**
 * In most cases {@link Basic} is what you need.
 * You can create your own implementation when you for example want to access
 * application settings dynamically (after setting up the REST client).
 *
 * @author Thomas Forrer
 */
public interface GerritAuthData {
    /**
     * Username used for login.
     */
    String getLogin();

    /**
     * Password used for login.
     */
    String getPassword();

    /**
     * Returns TRUE if the password value is an HTTP password corresponding to
     * the password token displayed on the user Settings -> HTTP Password page.
     * Gerrit can accept two types of passwords for authentication, the normal
     * site password (local user database, LDAP, etc) or a user-generated HTTP
     * password token.  The HTTP password token is used for REST API requests.
     *
     * If this method returns TRUE, API calls should not be redirected to the
     * /login endpoint, which
     *
     * @return TRUE if the password is the HTTP password token used for REST API authentication.
     * @see <a href="https://gerrit-review.googlesource.com/Documentation/rest-api.html#authentication">Gerrit REST API authentication</a>
     * @see <a href="https://gerrit-review.googlesource.com/Documentation/rest-api-config.html#get-info">Gerrit REST API configuration</a>
     */
    boolean isHttpPassword();

    /**
     * HTTP URL for accessing Gerrit. Please make sure that Gerrit root URL is used.
     *
     * Example: {@code "https://gerrit-review.googlesource.com"}
     */
    String getHost();

    /**
     * @return true when username and password is available. When false, anonymous access
     * will be used.
     */
    boolean isLoginAndPasswordAvailable();

    /**
     * A simple implementation for providing username and password at
     * construction time.
     * Note: It is not related to HTTP basic access authentication.
     */
    public class Basic implements GerritAuthData {
        private final String host;
        private final String login;
        private final String password;
        private final boolean httpPassword;

        /**
         * @param host see {@link GerritAuthData#getHost}.
         */
        public Basic(String host) {
            this(host, "", "");
        }

        /**
         * @param host see {@link GerritAuthData#getPassword}.
         * @param login see {@link GerritAuthData#getLogin}.
         * @param password see {@link GerritAuthData#getLogin}.
         */
        public Basic(String host, String login, String password) {
            this(host, login, password, false);
        }

        /**
         * @param host see {@link GerritAuthData#getPassword}.
         * @param login see {@link GerritAuthData#getLogin}.
         * @param password see {@link GerritAuthData#getLogin}.
         * @param httpPassword see {@link GerritAuthData@isHttpPassword}.
         */
        public Basic(String host, String login, String password, boolean httpPassword) {
            this.host = stripTrailingSlash(host);
            this.login = login;
            this.password = password;
            this.httpPassword = httpPassword;
        }

        @Override
        public boolean isHttpPassword() {
            return httpPassword;
        }

        @Override
        public String getLogin() {
            return login;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getHost() {
            return host;
        }

        @Override
        public boolean isLoginAndPasswordAvailable() {
            return !Strings.isNullOrEmpty(getLogin()) && !Strings.isNullOrEmpty(getPassword());
        }

        private String stripTrailingSlash(String host) {
            if (!Strings.isNullOrEmpty(host) && host.endsWith("/")) {
                host = host.substring(0, host.length() - 1);
            }
            return host;
        }
    }
}
