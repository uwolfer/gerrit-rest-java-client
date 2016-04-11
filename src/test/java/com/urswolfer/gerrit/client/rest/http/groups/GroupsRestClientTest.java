package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * @author Shawn Stafford
 */
public class GroupsRestClientTest {
    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final GroupInfo MOCK_GROUP_INFO = EasyMock.createMock(GroupInfo.class);

    @Test
    public void testId() throws Exception {
        GerritRestClient gerritRestClient = gerritRestClientExpectGet("/groups/jdoe");
        GroupsParser groupsParser = getGroupsParser();
        GroupsRestClient groupsRestClient = new GroupsRestClient(gerritRestClient, groupsParser);
        groupsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    private GerritRestClient gerritRestClientExpectGet(String expectedUrl) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);
        EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
            .andReturn(MOCK_JSON_ELEMENT).once();
        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GroupsParser getGroupsParser() throws Exception {
        GroupsParser groupsParser = EasyMock.createMock(GroupsParser.class);
        EasyMock.expect(groupsParser.parseGroupInfo(MOCK_JSON_ELEMENT))
                .andReturn(MOCK_GROUP_INFO).once();
        EasyMock.replay(groupsParser);
        return groupsParser;
    }
}
