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

import java.nio.charset.StandardCharsets;

/**
 * Makes a value safe to put into a URL without changing any value that was already safe.
 *
 * <p>Branch names, emails and query values used to go out as given, so callers who hit the cases
 * that broke passed them already encoded ({@code refs%2Fheads%2Fmaster}, {@code status:open+owner:self}).
 * Encoding fully, as {@link com.google.gerrit.extensions.restapi.Url#encode} does, would encode
 * those a second time. So this escapes only what cannot have worked: characters not allowed in a
 * URL, a {@code %} that does not start an escape, and the characters that end the value early
 * ({@code #}, {@code /?;} in a path segment, {@code &} in a query value). An existing {@code %XX}
 * is kept, so encoding twice gives the same as encoding once.
 *
 * <p>A {@code +} is a space to Gerrit, so {@link #queryValue} keeps it. Where the value cannot
 * contain a space - a ref, an email, a label - {@link #pathSegment} and
 * {@link #queryValueWithLiteralPlus} escape it as the literal plus it must be.
 *
 * <p>Not for a change id: Gerrit builds those with {@code Url.encode} itself, and every method
 * that takes one passes it on unchanged.
 */
public final class UrlEncoding {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /** Allowed in a path segment besides letters, digits and {@code -._~}; not {@code /?#;+}. */
    private static final String PATH_SEGMENT = "!$&'()*,=:@";

    /** Allowed in a query value besides letters, digits, {@code -._~} and {@code +}; not {@code &#}. */
    private static final String QUERY_VALUE_WITH_LITERAL_PLUS = "!$'()*,;=:@/?";

    private static final String QUERY_VALUE = QUERY_VALUE_WITH_LITERAL_PLUS + '+';

    private UrlEncoding() {}

    /** For one path segment that cannot contain a space: a branch or tag name, an email. */
    public static String pathSegment(String value) {
        return escape(value, PATH_SEGMENT);
    }

    /** For a query parameter value. A {@code +} stays a space. */
    public static String queryValue(String value) {
        return escape(value, QUERY_VALUE);
    }

    /** For a query parameter value that cannot contain a space, such as a ref name or pattern. */
    public static String queryValueWithLiteralPlus(String value) {
        return escape(value, QUERY_VALUE_WITH_LITERAL_PLUS);
    }

    private static String escape(String value, String alsoAllowed) {
        if (value == null) {
            return null;
        }
        StringBuilder out = null;
        for (int i = 0; i < value.length(); i += Character.charCount(value.codePointAt(i))) {
            int codePoint = value.codePointAt(i);
            if (isAllowed(codePoint, alsoAllowed) || isEscape(value, i)) {
                if (out != null) {
                    out.appendCodePoint(codePoint);
                }
            } else {
                if (out == null) {
                    out = new StringBuilder(value.length() + 16).append(value, 0, i);
                }
                appendEscaped(out, value, i);
            }
        }
        return out == null ? value : out.toString();
    }

    private static void appendEscaped(StringBuilder out, String value, int i) {
        int length = Character.charCount(value.codePointAt(i));
        if (length == 1 && Character.isSurrogate(value.charAt(i))) {
            // UTF-8 has no encoding for half a character; the JDK would silently write '?'
            throw new IllegalArgumentException("Unpaired surrogate at index " + i + " of a URL value");
        }
        for (byte b : value.substring(i, i + length).getBytes(StandardCharsets.UTF_8)) {
            out.append('%').append(HEX[(b >> 4) & 0xF]).append(HEX[b & 0xF]);
        }
    }

    private static boolean isAllowed(int c, String alsoAllowed) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
            || c == '-' || c == '.' || c == '_' || c == '~'
            || alsoAllowed.indexOf(c) >= 0;
    }

    /** Whether a {@code %} at {@code i} starts an existing escape, which is kept. */
    private static boolean isEscape(String value, int i) {
        return value.charAt(i) == '%' && i + 2 < value.length()
            && isHex(value.charAt(i + 1)) && isHex(value.charAt(i + 2));
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }
}
