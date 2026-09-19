package com.urswolfer.gerrit.client.rest.http.projects;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractJsonTest;
import org.testng.annotations.Test;
import com.urswolfer.gerrit.client.rest.gson.GerritJson;

public class ProjectCommitInfoJsonTest extends AbstractJsonTest {

    String[] includedInBranches = {"master", "branch1"};
    String[] includedInTags = {"tag1"};

    @Test
    public void testParseSingleCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/commit.json");

        CommitInfo commitInfo = gerritJson.as(jsonElement.getAsJsonObject(), CommitInfo.class);
        Truth.assertThat(commitInfo.message).isEqualTo("Use an EventBus to manage star icons  Image widgets that need to ...");
    }

    @Test
    public void testIncludedIn() throws Exception {
        JsonElement jsonElement = getJsonElement("parsers/includedIn.json");
        IncludedInInfo includedInInfo = gerritJson.as(jsonElement, IncludedInInfo.class);
        Truth.assertThat(includedInInfo.branches).hasSize(2);
        Truth.assertThat(includedInInfo.tags).hasSize(1);
        Truth.assertThat(includedInInfo.branches).containsExactlyElementsIn(includedInBranches);
        Truth.assertThat(includedInInfo.tags).containsExactlyElementsIn(includedInTags);

    }

    private GerritJson gerritJson = new GerritJson(getGson());

}
