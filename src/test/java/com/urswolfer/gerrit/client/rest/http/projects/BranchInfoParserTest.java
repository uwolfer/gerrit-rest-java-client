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

package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.collect.Lists;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.BranchInfoBuilder;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * @author Tim Coulson
 */
public class BranchInfoParserTest extends AbstractParserTest {
    private static final List<BranchInfo> BRANCH_INFO_LIST = Lists.newArrayList();

    static {
        BRANCH_INFO_LIST.add(new BranchInfoBuilder()
                .withRef("HEAD")
                .withRevision("master")
                .canDelete(false)
                .get());
        BRANCH_INFO_LIST.add(new BranchInfoBuilder()
                .withRef("refs/heads/master")
                .withRevision("a6e054e9c220e4fa16802ccab074a00d384067fc")
                .canDelete(false)
                .get());
        BRANCH_INFO_LIST.add(new BranchInfoBuilder()
                .withRef("refs/heads/test")
                .withRevision("a6e054e9c220e4fa16802ccab074a00d384067fc")
                .canDelete(false)
                .get());
    }

    private final BranchInfoParser branchInfoParser = new BranchInfoParser(getGson());

    @Test
    public void testParseBranchInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("branches.json");

        List<BranchInfo> branchInfos = branchInfoParser.parseBranchInfos(jsonElement);

        Truth.assertThat(branchInfos.size()).isEqualTo(3);
        for (int i = 0; i < branchInfos.size(); i++) {
            BranchInfo actual = branchInfos.get(i);
            BranchInfo expected = BRANCH_INFO_LIST.get(i);
            GerritAssert.assertEquals(actual, expected);
        }
    }

    @Test
    public void testParseBranchInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("branch.json");

        List<BranchInfo> branchInfos = branchInfoParser.parseBranchInfos(jsonElement);

        Truth.assertThat(branchInfos.size()).isEqualTo(1);
        GerritAssert.assertEquals(branchInfos.get(0), new BranchInfoBuilder()
            .withRef("refs/heads/master")
            .withRevision("5b80af780ae31bee6609ebc1bbab9ce6fd004dbb")
            .get());
    }
}
