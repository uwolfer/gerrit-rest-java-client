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
 * @author Urs Wolfer
 */
public class LoginSimulationServlet extends HttpServlet {

    /**
     * Only handle case when no "Authorization" header is sent. When "Authorization" header is sent,
     * leave it to GerritRestClientTest#basicAuth.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getHeader("Authorization") != null) {
            return;
        }
        resp.addCookie(new Cookie("GerritAccount", "value"));
        ByteStreams.copy(new FileInputStream("src/test/resources/com/urswolfer/gerrit/client/rest/http/login/index.html"), resp.getOutputStream());
    }
}
