package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.CommitInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import org.junit.Test;

import java.util.List;

/**
 * @author Chenglong Sun
 */
public class CommitInfoParserTest extends AbstractParserTest {
    private final CommitInfoParser commitInfoParser = new CommitInfoParser(getGson());

    @Test
    public void testParseCommitInfo() throws Exception {
        JsonElement jsonElement = getJsonElement("commit.json");
        List<CommitInfo> commitInfos = commitInfoParser.parseCommitInfos(jsonElement);
        Truth.assertThat(commitInfos).hasSize(1);
        Truth.assertThat(commitInfos.get(0).message.equals("Use an EventBus to manage star icons  Image widgets that need to ..."));
    }
}
