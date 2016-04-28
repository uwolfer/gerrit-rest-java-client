/*
 * Copyright 2013-2016 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.groups;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.client.ListGroupsOption;
import com.google.gerrit.extensions.common.GroupInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author Shawn Stafford
 */
public class GroupsRestClientTest {
    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);
    private static final GroupInfo MOCK_GROUP_INFO = EasyMock.createMock(GroupInfo.class);

    @Test
    public void testId() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/jdoe", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfo(MOCK_JSON_ELEMENT, MOCK_GROUP_INFO)
            .get();
        GroupsRestClient groupsRestClient = new GroupsRestClient(gerritRestClient, groupsParser);
        groupsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testCreate() throws Exception {
        String groupName = "foo";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGetGson()
            .expectPut("/groups/" + groupName, "{\"name\":\"foo\"}", MOCK_JSON_ELEMENT)
            .get();
        GroupsParser groupsParser = new GroupsParserBuilder()
            .expectParseGroupInfo(MOCK_JSON_ELEMENT, MOCK_GROUP_INFO)
            .get();
        GroupsRestClient groupsRestClient = new GroupsRestClient(gerritRestClient, groupsParser);
        groupsRestClient.create(groupName);

        EasyMock.verify(gerritRestClient, groupsParser);
    }

    @Test
    public void testListGroups() throws Exception {
        GroupListTestCase testCase = listTestCase().expectUrl("/groups/");
        testCase.execute().verify();
    }

    @Test(dataProvider = "ListGroupsTestCases")
    public void testListGroupsWithParameter(GroupListTestCase testCase) throws Exception {
        testCase.execute().verify();
    }

    @DataProvider(name = "ListGroupsTestCases")
    public Iterator<GroupListTestCase[]> listGroupTestCases() throws Exception {
        return Iterables.transform(Arrays.asList(
            listTestCase().withListParameter(
                new TestListRequest()
            ).expectUrl("/groups/"),
            listTestCase().withListParameter(
                new TestListRequest().withOwned(true)
            ).expectUrl("/groups/?owned"),
            listTestCase().withListParameter(
                new TestListRequest().withOwned(false)
            ).expectUrl("/groups/"),
            listTestCase().withListParameter(
                new TestListRequest().withLimit(10)
            ).expectUrl("/groups/?n=10"),
            listTestCase().withListParameter(
                new TestListRequest().withSuggest("foo")
            ).expectUrl("/groups/?suggest=foo"),
            listTestCase().withListParameter(
                new TestListRequest().withStart(5)
            ).expectUrl("/groups/?S=5"),
            listTestCase().withListParameter(
                new TestListRequest()
                    .withSuggest("bar")
                    .withLimit(15)
                    .withStart(10)
                    .withOwned(true)
            ).expectUrl("/groups/?n=15&S=10&owned&suggest=bar")
        ), new Function<GroupListTestCase, GroupListTestCase[]>() {
            @Override
            public GroupListTestCase[] apply(GroupListTestCase testCase) {
                return new GroupListTestCase[]{testCase};
            }
        }).iterator();
    }

    private static GroupListTestCase listTestCase() {
        return new GroupListTestCase();
    }

    private static final class GroupListTestCase {
        private TestListRequest listParameter = new TestListRequest();
        private String expectedUrl;
        private JsonElement mockJsonElement = EasyMock.createMock(JsonElement.class);
        private GerritRestClient gerritRestClient;
        private GroupsParser groupsParser;

        public GroupListTestCase withListParameter(TestListRequest listParameter) {
            this.listParameter = listParameter;
            return this;
        }

        public GroupListTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        public GroupListTestCase execute() throws Exception {
            GroupsRestClient groupsRestClient = getGroupsRestClient();
            Groups.ListRequest list = groupsRestClient.list();
            listParameter.apply(list).get();
            return this;
        }

        public void verify() {
            EasyMock.verify(gerritRestClient, groupsParser);
        }

        public GroupsRestClient getGroupsRestClient() throws Exception {
            return new GroupsRestClient(
                setupGerritRestClient(),
                setupGroupsParser()
            );
        }

        public GerritRestClient setupGerritRestClient() throws Exception {
            gerritRestClient = EasyMock.createMock(GerritRestClient.class);
            EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
                .andReturn(mockJsonElement)
                .once();
            EasyMock.replay(gerritRestClient);
            return gerritRestClient;
        }

        public GroupsParser setupGroupsParser() throws Exception {
            groupsParser = EasyMock.createMock(GroupsParser.class);
            EasyMock.expect(groupsParser.parseGroupInfos(mockJsonElement))
                .andReturn(new ArrayList<GroupInfo>())
                .once();
            EasyMock.replay(groupsParser);
            return groupsParser;
        }

        @Override
        public String toString() {
            return expectedUrl;
        }
    }

    private final static class TestListRequest {
        private EnumSet<ListGroupsOption> options =
            EnumSet.noneOf(ListGroupsOption.class);
        private List<String> projects = new ArrayList<String>();
        private List<String> groups = new ArrayList<String>();

        private Boolean visibleToAll;
        private String user;
        private Boolean owned;
        private Integer limit;
        private Integer start;
        private String substring;
        private String suggest;

        public TestListRequest withOwned(boolean owned) {
            this.owned = owned;
            return this;
        }

        public TestListRequest withSuggest(String suggest) {
            this.suggest = suggest;
            return this;
        }

        public TestListRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TestListRequest withStart(int start) {
            this.start = start;
            return this;
        }

        public Groups.ListRequest apply(Groups.ListRequest target) {
            if (limit != null) {
                target.withLimit(limit);
            }
            if (start != null) {
                target.withStart(start);
            }
            if (owned != null) {
                target.withOwned(owned);
            }
            if (suggest != null) {
                target.withSuggest(suggest);
            }
            return target;
        }
    }
}
