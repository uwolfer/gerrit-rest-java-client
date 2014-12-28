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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.AbstractParserTest;
import com.urswolfer.gerrit.client.rest.http.common.AccountInfoBuilder;
import com.urswolfer.gerrit.client.rest.http.common.CommentInfoBuilder;
import com.urswolfer.gerrit.client.rest.http.common.GerritAssert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Thomas Forrer
 */
public class CommentsParserTest extends AbstractParserTest {
    private static final TreeMap<String, List<CommentInfo>> COMMENT_INFOS = Maps.newTreeMap();

    static {
        AccountInfo accountInfo = new AccountInfoBuilder()
                .withName("Thomas Forrer")
                .withEmail("forrert@gmail.com")
                .withUsername("forrert")
                .withAccountId(1000000)
                .get();

        COMMENT_INFOS.put("playingfield.iml", Lists.newArrayList(
                new CommentInfoBuilder()
                        .withId("da33351e_8109fa2e")
                        .withLine(1)
                        .withMessage("is this really needed?")
                        .withUpdated("2014-05-28 19:14:50")
                        .withAuthor(accountInfo)
                        .get()
        ));
        COMMENT_INFOS.put("src/ch/tf/playingfield/PlayingField.java", Lists.newArrayList(
                new CommentInfoBuilder()
                        .withId("da33351e_41fff201")
                        .withMessage("looks good to me!")
                        .withUpdated("2014-05-28 19:14:50")
                        .withAuthor(accountInfo)
                        .get(),
                new CommentInfoBuilder()
                        .withId("da33351e_21046e14")
                        .withMessage("please reformat imports")
                        .withUpdated("2014-05-28 19:14:50")
                        .withLine(4)
                        .withAuthor(accountInfo)
                        .get()
        ));
    }

    private CommentsParser commentsParser = new CommentsParser(getGson());

    @Test
    public void testParseCommentsFileName() throws Exception {
        TreeMap<String, List<CommentInfo>> comments = parseComments();
        Truth.assertThat((Iterable) comments.keySet()).isEqualTo(COMMENT_INFOS.keySet());
    }

    @Test
    public void testParseCommentInfosForFile() throws Exception {
        TreeMap<String, List<CommentInfo>> comments = parseComments();
        Function<List<CommentInfo>, Integer> listSizeFunction = new Function<List<CommentInfo>, Integer>() {
            @Override
            public Integer apply(List<CommentInfo> commentInfos) {
                return commentInfos.size();
            }
        };
        SortedMap<String, Integer> commentsPerFile = Maps.transformValues(comments, listSizeFunction);
        SortedMap<String, Integer> expectedCommentsPerFile = Maps.transformValues(COMMENT_INFOS, listSizeFunction);

        Truth.assertThat(commentsPerFile).isEqualTo(expectedCommentsPerFile);
    }

    @Test
    public void testParseCommentInfos() throws Exception {
        TreeMap<String, List<CommentInfo>> comments = parseComments();
        GerritAssert.assertEquals(comments, COMMENT_INFOS);
    }

    private TreeMap<String, List<CommentInfo>> parseComments() throws Exception {
        JsonElement jsonElement = getJsonElement("comments.json");
        return commentsParser.parseCommentInfos(jsonElement);
    }
}
