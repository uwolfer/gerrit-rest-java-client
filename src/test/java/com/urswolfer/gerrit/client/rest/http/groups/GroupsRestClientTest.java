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

package com.urswolfer.gerrit.client.rest.http.groups;

import static com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest.restContext;

import com.google.gerrit.extensions.api.groups.Groups;
import com.google.gerrit.extensions.api.groups.Groups.QueryRequest;
import com.google.gerrit.extensions.client.ListGroupsOption;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Shawn Stafford
 */
public class GroupsRestClientTest {
    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    @Test
    public void testId() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectGet("/groups/jdoe", EMPTY_JSON_OBJECT)
            .get();
        GroupsRestClient groupsRestClient = new GroupsRestClient(restContext(gerritRestClient));
        groupsRestClient.id("jdoe").get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testCreate() throws Exception {
        String groupName = "foo";
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
            .expectPut("/groups/" + groupName, "{\"name\":\"foo\"}", EMPTY_JSON_OBJECT)
            .get();
        GroupsRestClient groupsRestClient = new GroupsRestClient(restContext(gerritRestClient));
        groupsRestClient.create(groupName);

        EasyMock.verify(gerritRestClient);
    }

    /**
     * The list parameters the client cannot send yet must say so rather than return an unfiltered
     * list, and must do it before any request goes out.
     */
    @Test(dataProvider = "UnsupportedListParameters", expectedExceptions = NotImplementedException.class)
    public void testUnsupportedListParameterIsRejected(String parameter, Consumer<Groups.ListRequest> setter)
            throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().get();
        Groups.ListRequest list = new GroupsRestClient(restContext(gerritRestClient)).list();
        setter.accept(list);
        try {
            list.get();
        } finally {
            EasyMock.verify(gerritRestClient);
        }
    }

    @DataProvider(name = "UnsupportedListParameters")
    public Object[][] unsupportedListParameters() {
        return new Object[][] {
            {"options", (Consumer<Groups.ListRequest>) list -> list.addOption(ListGroupsOption.MEMBERS)},
            {"project", (Consumer<Groups.ListRequest>) list -> list.withProject("p")},
            {"group", (Consumer<Groups.ListRequest>) list -> list.addGroup("uuid")},
            {"visible-to-all", (Consumer<Groups.ListRequest>) list -> list.withVisibleToAll(true)},
            {"user", (Consumer<Groups.ListRequest>) list -> list.withUser("jdoe")},
            {"substring", (Consumer<Groups.ListRequest>) list -> list.withSubstring("dev")},
        };
    }

    @Test(expectedExceptions = NotImplementedException.class)
    public void testQueryWithOptionsIsRejected() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().get();
        Groups.QueryRequest query = new GroupsRestClient(restContext(gerritRestClient)).query()
            .withOption(ListGroupsOption.MEMBERS);
        try {
            query.get();
        } finally {
            EasyMock.verify(gerritRestClient);
        }
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
        return Arrays.asList(
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
        ).stream().map(testCase -> new GroupListTestCase[]{testCase}).iterator();
    }

    private static GroupListTestCase listTestCase() {
        return new GroupListTestCase();
    }

    private static final class GroupListTestCase {
        private TestListRequest listParameter = new TestListRequest();
        private String expectedUrl;
        private JsonElement mockJsonElement = new JsonObject();
        private GerritRestClient gerritRestClient;

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
            EasyMock.verify(gerritRestClient);
        }

        public GroupsRestClient getGroupsRestClient() throws Exception {
            return new GroupsRestClient(restContext(setupGerritRestClient()));
        }

        public GerritRestClient setupGerritRestClient() throws Exception {
            gerritRestClient = EasyMock.createMock(GerritRestClient.class);
            EasyMock.expect(gerritRestClient.getRequest(expectedUrl))
                .andReturn(mockJsonElement)
                .once();
            EasyMock.replay(gerritRestClient);
            return gerritRestClient;
        }


        @Override
        public String toString() {
            return expectedUrl;
        }
    }

    private final static class TestListRequest {
        private Boolean owned;
        private Integer limit;
        private Integer start;
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

    @Test
    public void testQueryGroups() throws Exception {
        GroupQueryTestCase testCase = queryTestCase().expectUrl("/groups/");
        testCase.execute().verify();
    }

    @Test
    public void testQueryGroupsWithQueryParam() throws Exception {
        GroupQueryTestCase testCase = queryTestCase().expectUrl("/groups/?query=Q");
        GroupsRestClient groupsRestClient = testCase.getGroupsRestClient();
        groupsRestClient.query("Q").get();
        testCase.verify();
    }

    @Test(dataProvider = "QueryGroupsTestCases")
    public void testQueryGroupsWithParameter(GroupQueryTestCase testCase) throws Exception {
        testCase.execute().verify();
    }

    @DataProvider(name = "QueryGroupsTestCases")
    public Iterator<GroupQueryTestCase[]> queryGroupTestCases() throws Exception {
        return Arrays.asList(queryTestCase().withQueryParameter(new TestQueryRequest()).expectUrl("/groups/"),
                queryTestCase().withQueryParameter(new TestQueryRequest().withQuery("inname:test"))
                    .expectUrl("/groups/?query=inname:test"),
                queryTestCase()
                    .withQueryParameter(new TestQueryRequest().withQuery("inname:test")
                        .withLimit(25).withStart(50))
                    .expectUrl("/groups/?query=inname:test&limit=25&start=50"))
            .stream().map(testCase -> new GroupQueryTestCase[]{testCase})
            .iterator();
    }

    private static GroupQueryTestCase queryTestCase() {
        return new GroupQueryTestCase();
    }

    private static final class GroupQueryTestCase {
        private TestQueryRequest queryParameter = new TestQueryRequest();
        private String expectedUrl;
        private JsonElement mockJsonElement = new JsonObject();
        private GerritRestClient gerritRestClient;

        public GroupQueryTestCase withQueryParameter(TestQueryRequest listParameter) {
            this.queryParameter = listParameter;
            return this;
        }

        public GroupQueryTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        public GroupQueryTestCase execute() throws Exception {
            GroupsRestClient groupsRestClient = getGroupsRestClient();
            Groups.QueryRequest query = groupsRestClient.query();
            queryParameter.apply(query).get();
            return this;
        }

        public void verify() {
            EasyMock.verify(gerritRestClient);
        }

        public GroupsRestClient getGroupsRestClient() throws Exception {
            return new GroupsRestClient(restContext(setupGerritRestClient()));
        }

        public GerritRestClient setupGerritRestClient() throws Exception {
            gerritRestClient = EasyMock.createMock(GerritRestClient.class);
            EasyMock.expect(gerritRestClient.getRequest(expectedUrl)).andReturn(mockJsonElement).once();
            EasyMock.replay(gerritRestClient);
            return gerritRestClient;
        }

        @Override
        public String toString() {
            return expectedUrl;
        }
    }

    private final static class TestQueryRequest {
        private Integer limit;
        private Integer start;
        private String query;

        public TestQueryRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TestQueryRequest withStart(int start) {
            this.start = start;
            return this;
        }

        public TestQueryRequest withQuery(String query) {
            this.query = query;
            return this;
        }

        public QueryRequest apply(Groups.QueryRequest target) {
            if (limit != null) {
                target.withLimit(limit);
            }
            if (start != null) {
                target.withStart(start);
            }
            if (query != null) {
                target.withQuery(query);
            }
            return target;
        }
    }
}
