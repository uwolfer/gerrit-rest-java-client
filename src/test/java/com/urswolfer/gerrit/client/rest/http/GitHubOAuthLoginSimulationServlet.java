/*
 * Copyright 2020 Luca Milanesio
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

import com.google.common.io.ByteStreams;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Luca Milanesio
 */
public class GitHubOAuthLoginSimulationServlet extends HttpServlet {
    private static final String OAUTH_SCOPE_PATH = "/plugins/github-plugin/static/scope.html";

    /**
     * Always redirect to the OAuth scope selection, simulating the GitHub/OAuth authentication
     * behaviour.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(OAUTH_SCOPE_PATH);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addCookie(new Cookie("GerritAccount", "value"));
        ByteStreams.copy(new FileInputStream(LoginSimulationServlet.INDEX_HTML), resp.getOutputStream());
    }
}
