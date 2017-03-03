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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.api.changes.Changes;
import com.google.gerrit.extensions.client.ListChangesOption;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeInput;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.GerritRestClient;
import com.urswolfer.gerrit.client.rest.http.common.GerritRestClientBuilder;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * @author Thomas Forrer
 */
public class ChangesRestClientTest {
    private static final JsonElement MOCK_JSON_ELEMENT = EasyMock.createMock(JsonElement.class);

    private static final Function<ChangesQueryTestCase, ChangesQueryTestCase[]> WRAP_IN_ARRAY_FUNCTION =
            new Function<ChangesQueryTestCase, ChangesQueryTestCase[]>() {
                @Override
                public ChangesQueryTestCase[] apply(ChangesQueryTestCase testCase) {
                    return new ChangesQueryTestCase[]{testCase};
                }
            };

    @DataProvider(name = "ChangesQueryTestCases")
    public Iterator<ChangesQueryTestCase[]> getChangesQueryTestCases() {
        return Iterables.transform(Arrays.asList(
                queryParameter(
                        new TestQueryRequest().withQuery("is:open")
                ).expectUrl("/changes/?q=is:open"),
                queryParameter(
                        new TestQueryRequest().withQuery("is:open+is:watched")
                ).expectUrl("/changes/?q=is:open+is:watched"),
                queryParameter(
                        new TestQueryRequest().withLimit(10)
                ).expectUrl("/changes/?n=10"),
                queryParameter(
                        new TestQueryRequest().withQuery("is:open").withLimit(10)
                ).expectUrl("/changes/?q=is:open&n=10"),
                queryParameter(
                        new TestQueryRequest().withOption(ListChangesOption.LABELS)
                ).expectUrl("/changes/?o=LABELS"),
                queryParameter(
                        new TestQueryRequest().withStart(50)
                ).expectUrl("/changes/?S=50"),
                queryParameter(
                        new TestQueryRequest().withSortkey("003460ab0001ae15")
                ).expectUrl("/changes/?N=003460ab0001ae15"),
                queryParameter(
                        new TestQueryRequest().withQuery("is:open")
                                .withLimit(10)
                                .withOption(ListChangesOption.CURRENT_FILES)
                                .withStart(30)
                ).expectUrl("/changes/?q=is:open&n=10&S=30&o=CURRENT_FILES"),
                queryParameter(
                    new TestQueryRequest().withQuery("is:merged is:watched").encode()
                ).expectUrl("/changes/?q=is%3Amerged+is%3Awatched")
        ), WRAP_IN_ARRAY_FUNCTION).iterator();
    }

    @Test(dataProvider = "ChangesQueryTestCases")
    public void testQueryWithParameter(ChangesQueryTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = setupGerritRestClient(testCase);
        ChangesParser changesParser = setupChangesParser();

        ChangesRestClient changes = new ChangesRestClient(gerritRestClient, changesParser, null, null, null, null, null, null);

        Changes.QueryRequest queryRequest = changes.query();
        testCase.queryParameter.apply(queryRequest).get();

        EasyMock.verify(gerritRestClient, changesParser);
    }

    @Test
    public void testQueryWithString() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder()
                .expectGet("/changes/?q=is:open", MOCK_JSON_ELEMENT)
                .get();
        ChangesParser changesParser = setupChangesParser();
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);

        ChangesRestClient changesRestClient = new ChangesRestClient(gerritRestClient, changesParser, commentsParser, null, null, null, null, null);
        changesRestClient.query("is:open").get();

        EasyMock.verify(gerritRestClient);
    }

    @Test
    public void testIdAsInt() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().get();
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);

        ChangesRestClient changesRestClient = new ChangesRestClient(gerritRestClient, changesParser, commentsParser, null, null, null, null, null);

        ChangeApi changeApi = changesRestClient.id(123);

        Truth.assertThat(changeApi.id()).isEqualTo("123");
    }

    @Test
    public void testIdAsTriplet() throws Exception {
        GerritRestClient gerritRestClient = new GerritRestClientBuilder().get();
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        CommentsParser commentsParser = EasyMock.createMock(CommentsParser.class);

        ChangesRestClient changesRestClient = new ChangesRestClient(gerritRestClient, changesParser, commentsParser, null, null, null, null, null);

        ChangeApi changeApi = changesRestClient.id("packages%2Ftest", "master", "Ieabd72e73f3da0df90fd6e8cba8f6c5dd7d120df");

        Truth.assertThat(changeApi.id()).isEqualTo("packages%2Ftest~master~Ieabd72e73f3da0df90fd6e8cba8f6c5dd7d120df");
    }

    @Test
    public void testQuery() throws Exception {
        ChangesQueryTestCase testCase = new ChangesQueryTestCase().expectUrl("/changes/");
        GerritRestClient gerritRestClient = setupGerritRestClient(testCase);
        ChangesParser changesParser = setupChangesParser();

        ChangesRestClient changes = new ChangesRestClient(gerritRestClient, changesParser, null, null, null, null, null, null);

        changes.query().get();

        EasyMock.verify(gerritRestClient, changesParser);
    }

    @Test
    public void testCreate() throws Exception {
        ChangesCreateTestCase testCase = new ChangesCreateTestCase().expectUrl("/changes/");
        ChangeInput changeInput = new ChangeInput();
        changeInput.branch = "master";
        String changeInputJsonString = "{}";
        GerritRestClient gerritRestClient = setupGerritRestClient(testCase, changeInputJsonString);
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.id = "id";
        ChangesParser changesParser = setupChangesParserForCreate(changeInput,
            changeInputJsonString, changeInfo);

        ChangesRestClient changes = new ChangesRestClient(gerritRestClient, changesParser, null, null, null, null, null, null);

        ChangeApi changeApi = changes.create(changeInput);

        Truth.assertThat(changeApi.id()).isEqualTo(changeInfo.id);
        EasyMock.verify(gerritRestClient, changesParser);
    }

    private GerritRestClient setupGerritRestClient(ChangesQueryTestCase testCase) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);

        // this test does not care about json parsing, just return a mocked json element...
        EasyMock.expect(gerritRestClient.getRequest(testCase.expectedUrl))
                .andReturn(MOCK_JSON_ELEMENT)
                .once();

        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private GerritRestClient setupGerritRestClient(ChangesCreateTestCase testCase, String body) throws Exception {
        GerritRestClient gerritRestClient = EasyMock.createMock(GerritRestClient.class);

        EasyMock.expect(gerritRestClient.postRequest(testCase.expectedUrl, body))
            .andReturn(MOCK_JSON_ELEMENT)
            .once();

        EasyMock.replay(gerritRestClient);
        return gerritRestClient;
    }

    private ChangesParser setupChangesParser() throws Exception {
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        EasyMock.expect(changesParser.parseChangeInfos(MOCK_JSON_ELEMENT))
                .andReturn(Lists.<ChangeInfo>newArrayList())
                .once();
        EasyMock.replay(changesParser);
        return changesParser;
    }

    private ChangesParser setupChangesParserForCreate(ChangeInput changeInput,
        String changeInputJsonString, ChangeInfo changeInfo) throws Exception {
        ChangesParser changesParser = EasyMock.createMock(ChangesParser.class);
        EasyMock.expect(changesParser.generateChangeInput(changeInput))
            .andReturn(changeInputJsonString)
            .once();
        EasyMock.expect(changesParser.parseSingleChangeInfo(MOCK_JSON_ELEMENT))
            .andReturn(changeInfo)
            .once();
        EasyMock.replay(changesParser);
        return changesParser;
    }

    private static ChangesQueryTestCase queryParameter(TestQueryRequest parameter) {
        return new ChangesQueryTestCase().withQueryParameter(parameter);
    }

    private static final class ChangesQueryTestCase {
        private TestQueryRequest queryParameter;

        private String expectedUrl;

        private ChangesQueryTestCase withQueryParameter(TestQueryRequest queryParameter) {
            this.queryParameter = queryParameter;
            return this;
        }

        private ChangesQueryTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        @Override
        public String toString() {
            return expectedUrl;
        }
    }

    private static final class TestQueryRequest {
        private String query = null;
        private boolean encode = false;
        private Integer limit = null;
        private Integer start = null;
        private String sortkey = null;
        private EnumSet<ListChangesOption> options = EnumSet.noneOf(ListChangesOption.class);

        public TestQueryRequest withQuery(String query) {
            this.query = query;
            return this;
        }

        public TestQueryRequest encode() {
            encode = true;
            return this;
        }

        public TestQueryRequest withLimit(int limit) {
            this.limit = limit;
            return this;
        }

        public TestQueryRequest withStart(int start) {
            this.start = start;
            return this;
        }

        public TestQueryRequest withSortkey(String sortkey) {
            this.sortkey = sortkey;
            return this;
        }

        public TestQueryRequest withOption(ListChangesOption options) {
            this.options.add(options);
            return this;
        }

        public Changes.QueryRequest apply(Changes.QueryRequest queryRequest) {
            if (query != null) {
                queryRequest.withQuery(query);
            }
            if (encode) {
                queryRequest.encode();
            }
            if (limit != null) {
                queryRequest.withLimit(limit);
            }
            if (start != null) {
                queryRequest.withStart(start);
            }
            if (sortkey != null) {
                queryRequest.withSortkey(sortkey);
            }
            if (!options.isEmpty()) {
                queryRequest.withOptions(options);
            }
            return queryRequest;
        }
    }

    private static final class ChangesCreateTestCase {

        private String expectedUrl;

        private ChangesCreateTestCase expectUrl(String expectedUrl) {
            this.expectedUrl = expectedUrl;
            return this;
        }

        @Override
        public String toString() {
            return expectedUrl;
        }
    }
}
