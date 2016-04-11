package com.urswolfer.gerrit.client.rest.http.groups;

import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import org.easymock.EasyMock;

/**
 * @author Shawn Stafford
 */
public class GroupApiRestClientTest {

    private GroupsRestClient getGroupsRestClient(GerritRestClient gerritRestClient) {
        GroupsParser groupsParser = EasyMock.createMock(GroupsParser.class);
        return new GroupsRestClient(gerritRestClient, groupsParser);
    }

}
