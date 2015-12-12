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

package com.urswolfer.gerrit.client.rest.http.util;

import com.google.gerrit.extensions.restapi.BinaryResult;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Urs Wolfer
 */
public class BinaryResultUtils {
    private BinaryResultUtils() {}

    public static BinaryResult createBinaryResult(HttpResponse response) throws IOException {
        InputStream content = response.getEntity().getContent();
        BinaryResult binaryResult = BinaryResult.create(content);
        setContentType(response, binaryResult);
        setContentEncoding(response, binaryResult);
        return binaryResult;
    }

    private static void setContentType(HttpResponse response, BinaryResult binaryResult) {
        Header fileContentType = response.getFirstHeader("X-FYI-Content-Type");
        if (fileContentType == null) {
            fileContentType = response.getFirstHeader("Content-Type");
        }
        if (fileContentType != null) {
            binaryResult.setContentType(fileContentType.getValue());
        }
    }

    private static void setContentEncoding(HttpResponse response, BinaryResult binaryResult) {
        Header contentEncoding = response.getFirstHeader("X-FYI-Content-Encoding");
        if (contentEncoding != null) {
            if ("base64".equals(contentEncoding.getValue())) {
                binaryResult.base64();
            }
        }
    }
}
