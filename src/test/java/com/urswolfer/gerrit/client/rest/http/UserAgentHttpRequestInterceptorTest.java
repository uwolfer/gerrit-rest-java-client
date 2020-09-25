/*
 * Copyright 2013-2020 Urs Wolfer
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

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpContext;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class UserAgentHttpRequestInterceptorTest {

    @Test
    public void testHeaderIsModified() {
        HttpRequest req = EasyMock.createMock(HttpRequest.class);
        Header header = EasyMock.createMock(Header.class);
        EasyMock.expect(header.getValue()).andReturn("BaseAgent").once();
        EasyMock.expect(req.getFirstHeader(HttpHeaders.USER_AGENT))
            .andReturn(header).once();
        req.setHeader(EasyMock.matches("^User-Agent$"),
            EasyMock.matches("^gerrit-rest-java-client/.* using BaseAgent$"));
        EasyMock.replay(header, req);
        HttpContext ctx = EasyMock.createMock(HttpContext.class);
        UserAgentHttpRequestInterceptor interceptor = new UserAgentHttpRequestInterceptor();

        interceptor.process(req, ctx);

        EasyMock.verify(header, req);
    }
}
