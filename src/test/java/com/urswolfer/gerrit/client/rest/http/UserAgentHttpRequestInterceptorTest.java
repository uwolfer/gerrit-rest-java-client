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
