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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Like {@link LoginSimulationServlet}, but it establishes a session even when HTTP credentials are
 * sent, and it counts the login requests it answers. That makes it possible to assert that the form
 * login (POST) is not attempted once the plain GET has already authenticated the client.
 *
 * @author Urs Wolfer
 */
public class CountingLoginSimulationServlet extends HttpServlet {
    private static final AtomicInteger GET_COUNT = new AtomicInteger();
    private static final AtomicInteger POST_COUNT = new AtomicInteger();

    public static void resetCounts() {
        GET_COUNT.set(0);
        POST_COUNT.set(0);
    }

    public static int getGetCount() {
        return GET_COUNT.get();
    }

    public static int getPostCount() {
        return POST_COUNT.get();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        GET_COUNT.incrementAndGet();
        login(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        POST_COUNT.incrementAndGet();
        login(resp);
    }

    private void login(HttpServletResponse resp) throws IOException {
        resp.addCookie(new Cookie("GerritAccount", "value"));
        Files.copy(Paths.get(LoginSimulationServlet.INDEX_HTML), resp.getOutputStream());
    }
}
