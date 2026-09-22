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

import com.google.common.base.Strings;

/**
 * Builds a request URL with an optional query string.
 *
 * <p>This replaces the accumulate-a-string-then-maybe-append-a-question-mark block that each
 * listing endpoint carried its own copy of.
 *
 * <p>Values are written exactly as given, which is what the endpoints using this already did. The
 * client's URL encoding is inconsistent - a change query is not encoded while
 * {@code suggest_reviewers} is, and a branch ref is not encoded while the project name it is
 * appended to is - and this class deliberately does not change that. Encoding those values is a
 * change to what goes on the wire and belongs in its own commit, not hidden inside a refactoring.
 */
public final class UrlQuery {

    private final String path;
    private final StringBuilder query = new StringBuilder();

    private UrlQuery(String path) {
        this.path = path;
    }

    public static UrlQuery of(String path) {
        return new UrlQuery(path);
    }

    /**
     * A parameter with no value, such as {@code ?recursive}.
     */
    public UrlQuery flag(String name) {
        return append(name);
    }

    public UrlQuery flagIf(boolean condition, String name) {
        return condition ? flag(name) : this;
    }

    public UrlQuery param(String name, Object value) {
        return append(name + "=" + value);
    }

    public UrlQuery paramIf(boolean condition, String name, Object value) {
        return condition ? param(name, value) : this;
    }

    public UrlQuery paramIfNotEmpty(String name, String value) {
        return paramIf(!Strings.isNullOrEmpty(value), name, value);
    }

    /**
     * Adds the parameter when the value is positive, the convention Gerrit's paging arguments use
     * for "not set".
     */
    public UrlQuery paramIfPositive(String name, int value) {
        return paramIf(value > 0, name, value);
    }

    /**
     * Adds the parameter when the value is set at all, for the few arguments where zero is
     * meaningful.
     */
    public UrlQuery paramIfNonZero(String name, int value) {
        return paramIf(value != 0, name, value);
    }

    /**
     * Repeats the parameter once per value, as Gerrit's {@code o=} options do.
     */
    public UrlQuery params(String name, Iterable<?> values) {
        for (Object value : values) {
            param(name, value);
        }
        return this;
    }

    public String toUrl() {
        return query.length() == 0 ? path : path + '?' + query;
    }

    private UrlQuery append(String parameter) {
        if (query.length() > 0) {
            query.append('&');
        }
        query.append(parameter);
        return this;
    }
}
