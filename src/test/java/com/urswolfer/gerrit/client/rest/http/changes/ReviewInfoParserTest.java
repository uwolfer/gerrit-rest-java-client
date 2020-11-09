package com.urswolfer.gerrit.client.rest.http.changes;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.google.gerrit.extensions.common.FileInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;

import junit.framework.TestCase;

public class ReviewInfoParserTest extends AbstractParserTest {
    private static final List<String> REVIEW_INFO= Lists.newArrayList("/COMMIT_MSG",
            "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java",
            null // apparently the gerrit api includes a trailing comma
                 // see https://gerrit-review.googlesource.com/Documentation/rest-api-changes.html#list-files
    );

    private final ReviewInfoParser reviewInfoParser = new ReviewInfoParser(getGson());

    @Test
    public void testParseReviewInfos() throws Exception {
        JsonElement jsonElement = getJsonElement("files-reviewed.json");

        List<String> reviewed = reviewInfoParser.parseFileInfos(jsonElement);
        GerritAssert.assertEquals(reviewed, REVIEW_INFO);
    }

}