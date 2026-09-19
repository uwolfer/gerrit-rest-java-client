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

package com.urswolfer.gerrit.client.rest.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simulates a Gerrit >= 2.12 login, which returns the XSRF token in a cookie instead of embedding
 * it in the start page HTML. The body still carries a (different) xGerritAuth token, so a test can
 * tell which of the two the client picked.
 *
 * @author Urs Wolfer
 */
public class XsrfCookieLoginSimulationServlet extends HttpServlet {
    public static final String XSRF_TOKEN = "xsrf-token-from-cookie";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addCookie(new Cookie("GerritAccount", "value"));
        resp.addCookie(new Cookie("XSRF_TOKEN", XSRF_TOKEN));
        Files.copy(Paths.get(LoginSimulationServlet.INDEX_HTML), resp.getOutputStream());
    }
}
