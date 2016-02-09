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

package com.urswolfer.gerrit.client.rest.http.accounts;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Urs Wolfer
 */
public class AccountApiRestClientTest {

    @Test
    public void testDownloadAvatar() throws Exception {
        String imageContent = "image content";
        String requestUrl = "/accounts/101/avatar?s=16";
        HttpResponse httpResponse = EasyMock.createMock(HttpResponse.class);
        HttpEntity httpEntity = EasyMock.createMock(HttpEntity.class);
        EasyMock.expect(httpEntity.getContent()).andStubReturn(new ByteArrayInputStream(imageContent.getBytes("UTF-8")));
        EasyMock.expect(httpResponse.getEntity()).andStubReturn(httpEntity);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Encoding")).andStubReturn(null);
        EasyMock.expect(httpResponse.getFirstHeader("X-FYI-Content-Type")).andStubReturn(null);
        EasyMock.expect(httpResponse.getFirstHeader("Content-Type")).andStubReturn(
            new BasicHeader("Content-Type", "image/png"));
        EasyMock.replay(httpEntity, httpResponse);

        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectRequest(requestUrl, null, GerritRestClient.HttpVerb.GET, httpResponse)
            .get();

        AccountsRestClient accountsRestClient = getAccountsRestClient(gerritRestClient);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryResult binaryResult = accountsRestClient.id(101).downloadAvatar(16);
        try {
            binaryResult.writeTo(byteArrayOutputStream);
            String actualContent = byteArrayOutputStream.toString();

            Truth.assertThat(actualContent).isEqualTo(imageContent);
            Truth.assertThat(binaryResult.isBase64()).isFalse();
            Truth.assertThat(binaryResult.getContentType()).isEqualTo("image/png");
            EasyMock.verify(gerritRestClient);
        } finally {
            binaryResult.close();
            byteArrayOutputStream.close();
        }
    }

    private AccountsRestClient getAccountsRestClient(GerritRestClient gerritRestClient) {
        AccountsParser accountsParser = EasyMock.createMock(AccountsParser.class);
        return new AccountsRestClient(gerritRestClient, accountsParser);
    }
}
