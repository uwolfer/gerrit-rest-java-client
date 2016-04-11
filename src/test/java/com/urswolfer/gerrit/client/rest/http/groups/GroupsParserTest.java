package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gerrit.extensions.common.GroupOptionsInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Shawn Stafford
 */
public class GroupsParserTest extends AbstractParserTest {
    private final GroupsParser groupsParser = new GroupsParser(getGson());

    private final GroupInfo testGroup;

    public GroupsParserTest() {
        testGroup = new GroupInfo();
        testGroup.id = "6a1e70e1a88782771a91808c8af9bbb7a9871389";
        testGroup.name = "Administrators";
        testGroup.url = "#/admin/groups/uuid-6a1e70e1a88782771a91808c8af9bbb7a9871389";
        testGroup.description = "Gerrit Site Administrators";
        testGroup.groupId = new Integer(1);
        testGroup.owner = "Administrators";
        testGroup.ownerId = "6a1e70e1a88782771a91808c8af9bbb7a9871389";

        testGroup.options = new GroupOptionsInfo();
    }

    @Test
    public void testParseGroupInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("group.json");

        GroupInfo groupInfo = groupsParser.parseGroupInfo(jsonElement);

        GerritAssert.assertEquals(groupInfo, testGroup);
    }

    @Test
    public void testParseGroupInfoWithNullJsonElement() throws Exception {
        GroupInfo groupInfo = groupsParser.parseGroupInfo(null);

        Truth.assertThat(groupInfo).isNull();
    }

    @Test
    public void testParseGroupInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("groups.json");
        List<GroupInfo> groupInfos = groupsParser.parseGroupInfos(jsonElement);
        Truth.assertThat(groupInfos).hasSize(6);
    }

}
