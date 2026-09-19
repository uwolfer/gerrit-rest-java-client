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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A /login/ page which establishes no session at all: it sets neither a GerritAccount nor an
 * XSRF_TOKEN cookie, so the GET login yields no token. It counts the POSTs it answers, which lets
 * a test assert that the form login was not attempted.
 *
 * @author Urs Wolfer
 */
public class NoSessionLoginSimulationServlet extends HttpServlet {
    private static final AtomicInteger POST_COUNT = new AtomicInteger();

    public static void resetCounts() {
        POST_COUNT.set(0);
    }

    public static int getPostCount() {
        return POST_COUNT.get();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        POST_COUNT.incrementAndGet();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
