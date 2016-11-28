/*
 * Copyright 2013-2014 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.base.Function;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.client.DiffPreferencesInfo.Whitespace;
import com.google.gerrit.extensions.common.DiffInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.google.gerrit.extensions.restapi.Url;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Thomas Forrer
 */
public class FileApiRestClientTest {

    private static final String FILE_CONTENT = "some new changes";
    private static final String FILE_PATH = "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java";

    private RevisionApiRestClient revisionApiRestClient;

    @Test
    public void testContent() throws Exception {
        String requestUrl = getBaseRequestUrl() + "/content";
        String base64String = Base64.encodeBase64String(FILE_CONTENT.getBytes("UTF-8"));
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(base64String.getBytes("UTF-8")));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "base64"));
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(
            new BasicHeader("X-FYI-Content-Type", "text/plain"));
        EasyMock.replay(httpEntity, httpResponse);

        setupServices();

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GerritRestClient.HttpVerb.GET, httpResponse)
            .get();

        FileApiRestClient fileApiRestClient = new FileApiRestClient(gerritRestClient, revisionApiRestClient, null, FILE_PATH);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = fileApiRestClient.content();
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = new String(Base64.decodeBase64(byteArrayOutputStream.toString()));

            Truth.assertThat(actualContent).isEqualTo(FILE_CONTENT);
            Truth.assertThat(binaryResult.isBase64()).isTrue();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("text/plain");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    @Test
    public void testDiff() throws Exception {
        String requestUrl = getBaseRequestUrl() + "/diff";
        testDiff(new Function<FileApiRestClient, Void>() {
            @Override
            public Void apply(FileApiRestClient fileApiRestClient) {
                try {
                    fileApiRestClient.diff();
                } catch (RestApiException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }, requestUrl);
    }

    @Test
    public void testDiffWithBase() throws Exception {
        final String base = "2";
        String requestUrl = getBaseRequestUrl() + "/diff?base=" + base;
        testDiff(new Function<FileApiRestClient, Void>() {
            @Override
            public Void apply(FileApiRestClient fileApiRestClient) {
                try {
                    fileApiRestClient.diff(base);
                } catch (RestApiException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }, requestUrl);
    }

    @Test
    public void testDiffRequest() throws Exception {
        final Integer context = 10;
        String requestUrl = getBaseRequestUrl() + "/diff?context=" + context;
        testDiff(new Function<FileApiRestClient, Void>() {
            @Override
            public Void apply(FileApiRestClient fileApiRestClient) {
                try {
                    fileApiRestClient.diffRequest()
                        .withContext(context)
                        .get();
                } catch (RestApiException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }, requestUrl);

        final boolean intraline = true;
        requestUrl += "&intraline=" + Boolean.toString(intraline);
        testDiff(new Function<FileApiRestClient, Void>() {
            @Override
            public Void apply(FileApiRestClient fileApiRestClient) {
                  try {
                      fileApiRestClient.diffRequest()
                          .withContext(context)
                          .withIntraline(intraline)
                          .get();
                  } catch (RestApiException e) {
                      throw new RuntimeException(e);
                  }
                  return null;
              }
        }, requestUrl);

        final Whitespace whitespace = Whitespace.IGNORE_ALL;
        requestUrl += "&whitespace=" + whitespace.name();
        testDiff(new Function<FileApiRestClient, Void>() {
            @Override
            public Void apply(FileApiRestClient fileApiRestClient) {
                try {
                    fileApiRestClient.diffRequest()
                        .withContext(context)
                        .withIntraline(intraline)
                        .withWhitespace(whitespace)
                        .get();
                } catch (RestApiException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
      }, requestUrl);
    }

    private void testDiff(Function<FileApiRestClient, Void> method, String expectedRequestUrl) throws Exception {
        JsonElement jsonElement = EasyMock.createMock(JsonElement.class);
        DiffInfo diffInfo = EasyMock.createMock(DiffInfo.class);

        DiffInfoParser diffInfoParser = EasyMock.createMock(DiffInfoParser.class);
        EasyMock.expect(diffInfoParser.parseDiffInfo(jsonElement)).andReturn(diffInfo).once();
        EasyMock.replay(diffInfoParser);

        setupServices();
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().expectGet(expectedRequestUrl, jsonElement).get();

        FileApiRestClient fileApiRestClient = new FileApiRestClient(gerritRestClient, revisionApiRestClient, diffInfoParser, FILE_PATH);
        method.apply(fileApiRestClient);

        EasyMock.verify(gerritRestClient, diffInfoParser);
    }

    private String getBaseRequestUrl() {
        String filePathEncoded = Url.encode(FILE_PATH);
        return "/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/1/files/" + filePathEncoded;
    }

    public void setupServices() {
        revisionApiRestClient = EasyMock.createMock(RevisionApiRestClient.class);
        EasyMock.expect(revisionApiRestClient.getRequestPath())
            .andReturn("/changes/myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940/revisions/1").once();
        EasyMock.replay(revisionApiRestClient);
    }
}
