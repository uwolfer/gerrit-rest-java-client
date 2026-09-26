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

import com.google.gerrit.extensions.restapi.Url;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Random;
import java.util.function.UnaryOperator;

import static com.google.common.truth.Truth.assertThat;

/**
 * The guarantees {@link UrlEncoding} makes, checked over a few thousand generated values each.
 */
public class UrlEncodingTest {

    private static final int SAMPLES = 5000;

    /** Everything a path segment kept before, besides letters and digits. */
    private static final String KEPT_IN_SEGMENT = "-._~!$&'()*,=:@";
    /** Everything a query value kept before, besides letters and digits. */
    private static final String KEPT_IN_QUERY = "-._~!$'()*,;=:@/?+";

    @DataProvider(name = "encoders")
    public Object[][] encoders() {
        return new Object[][] {
            {"pathSegment", (UnaryOperator<String>) UrlEncoding::pathSegment, KEPT_IN_SEGMENT},
            {"queryValue", (UnaryOperator<String>) UrlEncoding::queryValue, KEPT_IN_QUERY},
            {"queryValueWithLiteralPlus", (UnaryOperator<String>) UrlEncoding::queryValueWithLiteralPlus,
                KEPT_IN_QUERY.replace("+", "")},
        };
    }

    /**
     * What callers passed before and got a valid request for - including values they encoded
     * themselves - goes out byte for byte the same.
     */
    @Test(dataProvider = "encoders")
    public void aValueThatWasAlreadySafeIsUnchanged(String name, UnaryOperator<String> encode, String kept) {
        Random random = new Random(1);
        String alphabet = "abcXYZ019" + kept;
        for (int i = 0; i < SAMPLES; i++) {
            StringBuilder value = new StringBuilder();
            for (int j = random.nextInt(12); j >= 0; j--) {
                if (random.nextInt(6) == 0) {
                    value.append('%').append("0123456789abcdefABCDEF".charAt(random.nextInt(22)))
                        .append("0123456789ABCDEF".charAt(random.nextInt(16)));
                } else {
                    value.append(alphabet.charAt(random.nextInt(alphabet.length())));
                }
            }
            assertThat(encode.apply(value.toString())).isEqualTo(value.toString());
        }
    }

    /**
     * So a value the caller encoded before passing it is not encoded again.
     */
    @Test(dataProvider = "encoders")
    public void encodingTwiceIsEncodingOnce(String name, UnaryOperator<String> encode, String kept) {
        Random random = new Random(2);
        for (int i = 0; i < SAMPLES; i++) {
            String once = encode.apply(anyString(random));
            assertThat(encode.apply(once)).isEqualTo(once);
        }
    }

    /**
     * Whatever goes in, the result is a valid URL that the value does not end early: the request
     * builds instead of throwing, and a path value stays one segment.
     */
    @Test(dataProvider = "encoders")
    public void theResultIsAlwaysValidAndStaysInItsPlace(String name, UnaryOperator<String> encode, String kept) {
        Random random = new Random(3);
        for (int i = 0; i < SAMPLES; i++) {
            String encoded = encode.apply(anyString(random));
            if (name.startsWith("path")) {
                URI uri = URI.create("http://gerrit.example.com/a/" + encoded + "/b");
                assertThat(uri.getRawPath()).isEqualTo("/a/" + encoded + "/b");
                assertThat(encoded).doesNotContainMatch("[/?#;]");
            } else {
                URI uri = URI.create("http://gerrit.example.com/a?q=" + encoded + "&n=1");
                assertThat(uri.getRawQuery()).isEqualTo("q=" + encoded + "&n=1");
                assertThat(encoded).doesNotContainMatch("[&#]");
            }
        }
    }

    /**
     * Gerrit decodes with {@link Url#decode}; it must get back what the caller passed. A {@code %}
     * and, where it may stand for a space, a {@code +} are left out: those are read as the caller's
     * own encoding.
     */
    @Test(dataProvider = "encoders")
    public void gerritDecodesTheOriginalValue(String name, UnaryOperator<String> encode, String kept) {
        boolean plusIsLiteral = name.equals("pathSegment") || name.equals("queryValueWithLiteralPlus");
        Random random = new Random(4);
        for (int i = 0; i < SAMPLES; i++) {
            String value = anyString(random).replace("%", "");
            if (!plusIsLiteral) {
                value = value.replace("+", "");
            }
            assertThat(Url.decode(encode.apply(value))).isEqualTo(value);
        }
    }

    @Test
    public void examples() {
        assertThat(UrlEncoding.pathSegment("refs/heads/master")).isEqualTo("refs%2Fheads%2Fmaster");
        assertThat(UrlEncoding.pathSegment("feature#1")).isEqualTo("feature%231");
        assertThat(UrlEncoding.pathSegment("c++")).isEqualTo("c%2B%2B");
        assertThat(UrlEncoding.pathSegment("a;b")).isEqualTo("a%3Bb");
        assertThat(UrlEncoding.pathSegment("john+tag@example.com")).isEqualTo("john%2Btag@example.com");
        assertThat(UrlEncoding.queryValue("status:open owner:self")).isEqualTo("status:open%20owner:self");
        assertThat(UrlEncoding.queryValue("status:open+owner:self")).isEqualTo("status:open+owner:self");
        assertThat(UrlEncoding.queryValue("message:50%")).isEqualTo("message:50%25");
        assertThat(UrlEncoding.queryValue("message:50%2")).isEqualTo("message:50%252");
        assertThat(UrlEncoding.queryValue("a&b#c")).isEqualTo("a%26b%23c");
        assertThat(UrlEncoding.queryValue("^release-[0-9]$")).isEqualTo("%5Erelease-%5B0-9%5D$");
        assertThat(UrlEncoding.queryValueWithLiteralPlus("^release-[0-9]+$")).isEqualTo("%5Erelease-%5B0-9%5D%2B$");
        assertThat(UrlEncoding.queryValue("ä")).isEqualTo("%C3%A4");
        assertThat(UrlEncoding.queryValue("😀")).isEqualTo("%F0%9F%98%80");
        assertThat(UrlEncoding.queryValue(null)).isNull();
        assertThat(UrlEncoding.pathSegment("")).isEmpty();
    }

    /**
     * UTF-8 cannot encode half a character; the JDK would silently turn it into a '?', which
     * addresses something else.
     */
    @Test(dataProvider = "encoders", expectedExceptions = IllegalArgumentException.class)
    public void anUnpairedSurrogateIsRejected(String name, UnaryOperator<String> encode, String kept) {
        encode.apply("a\uD83Db");
    }

    @Test(dataProvider = "encoders", expectedExceptions = IllegalArgumentException.class)
    public void anUnpairedLowSurrogateIsRejected(String name, UnaryOperator<String> encode, String kept) {
        encode.apply("\uDE00");
    }

    /**
     * A complete, valid surrogate pair must not be rejected. U+1D800 was chosen because its low
     * 16 bits, 0xD800, are themselves a surrogate value - the exact case a check that inspects the
     * truncated {@code (char) codePoint} instead of the pair gets wrong.
     */
    @Test(dataProvider = "encoders")
    public void aValidSurrogatePairIsEncodedNotRejected(String name, UnaryOperator<String> encode, String kept) {
        String pair = new String(Character.toChars(0x1D800));
        assertThat(encode.apply(pair)).isEqualTo("%F0%9D%A0%80");
        assertThat(encode.apply("a" + pair + "b")).isEqualTo("a%F0%9D%A0%80b");
    }

    /**
     * Any printable ASCII, a control character now and then, and letters outside ASCII -
     * including one that takes two chars in Java.
     */
    private static String anyString(Random random) {
        StringBuilder value = new StringBuilder();
        for (int j = random.nextInt(12); j >= 0; j--) {
            int kind = random.nextInt(20);
            if (kind == 0) {
                value.append((char) random.nextInt(32));
            } else if (kind == 1) {
                value.append('ä');
            } else if (kind == 2) {
                value.append("😀");
            } else {
                value.append((char) (32 + random.nextInt(95)));
            }
        }
        return value.toString();
    }
}
