package com.urswolfer.gerrit.client.rest.http.projects.parsers;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

public class ProjectCommitInfoParserTest extends AbstractParserTest {

    String[] includedInBranches = {"master", "branch1"};
    String[] includedInTags = {"tag1"};

    @Test
    public void testParseSingleCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("commit.json");

        CommitInfo commitInfo = projectCommitInfoParser.parseSingleCommitInfo(jsonElement.getAsJsonObject());
        Truth.assertThat(commitInfo.message).isEqualTo("Use an EventBus to manage star icons  Image widgets that need to ...");
    }

    @Test
    public void testIncludedIn() throws Exception {
        JsonElement jsonElement = getJsonElement("includedIn.json");
        IncludedInInfo includedInInfo = projectCommitInfoParser.parseIncludedInInfo(jsonElement);
        Truth.assertThat(includedInInfo.branches).hasSize(2);
        Truth.assertThat(includedInInfo.tags).hasSize(1);
        Truth.assertThat(includedInInfo.branches).containsExactlyElementsIn(includedInBranches);
        Truth.assertThat(includedInInfo.tags).containsExactlyElementsIn(includedInTags);

    }

    private ProjectCommitInfoParser projectCommitInfoParser = new ProjectCommitInfoParser(getGson());

}
