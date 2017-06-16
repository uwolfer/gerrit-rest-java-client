package com.urswolfer.gerrit.client.rest.http.changes;

import org.testng.annotations.*;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;

public class IncludedInInfoParserTest extends AbstractParserTest {
    private final IncludedInInfoParser includedInInfoParser = new IncludedInInfoParser(getGson());

    @Test
    public void testParseEditInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("includedin.json");
        IncludedInInfo includedInInfo = includedInInfoParser.parseIncludedInInfos(jsonElement);
        Truth.assertThat(includedInInfo.branches).hasSize(4);
        Truth.assertThat(includedInInfo.tags).hasSize(3);
        Truth.assertThat(includedInInfo.branches).containsAllOf("integration/master", "integration/releases/2.12", "master", "releases/2.12");
        Truth.assertThat(includedInInfo.tags).containsAllOf("2017-12_v2.12", "2017-12_v2.13", "v2.13.1_xyz");
    }

}
