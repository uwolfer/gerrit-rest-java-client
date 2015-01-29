package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.SuggestedReviewerInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.testng.annotations.Test;

import java.util.List;

public class SuggestedReviewerInfoParserTest extends AbstractParserTest {
    private final SuggestedReviewerInfoParser accountsParser = new SuggestedReviewerInfoParser(getGson());

    @Test
    public void testParseSuggestReviewerInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewer.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = accountsParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(1);
    }

    @Test
    public void testParseSuggestReviewersInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("suggestreviewers.json");
        List<SuggestedReviewerInfo> suggestedReviewerInfos = accountsParser.parseSuggestReviewerInfos(jsonElement);
        Truth.assertThat(suggestedReviewerInfos).hasSize(2);
    }
}
