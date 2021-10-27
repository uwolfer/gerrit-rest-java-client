/*
 * Copyright 2013-2021 Urs Wolfer
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

package com.urswolfer.gerrit.client.rest.http.changes.parsers;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeMessageInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.RobotCommentInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

import java.util.ArrayList;
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

    private static final TreeMap<String, List<RobotCommentInfo>> ROBOT_COMMENT_INFOS = Maps.newTreeMap();

    static {
        AccountInfo accountInfo = new AccountInfoBuilder()
            .withName("CodeAnalyzer")
            .withEmail("code.analyzer@example.com")
            .withUsername("C_Analyzer")
            .withAccountId(1000000)
            .get();

        ROBOT_COMMENT_INFOS.put("playingfield.iml", Lists.newArrayList(
            new RobotCommentInfoBuilder()
                .withId("TvcXrmjM")
                .withLine(1)
                .withMessage("Unused import")
                .withUpdated("2014-05-28 19:14:50")
                .withAuthor(accountInfo)
                .withRobotId("importChecker")
                .withRobotRunId("12x1375aa8626ea7149792831fe2ed85e80s1r03")
                .get()
        ));
        ROBOT_COMMENT_INFOS.put("src/ch/tf/playingfield/PlayingField.java", Lists.newArrayList(
            new RobotCommentInfoBuilder()
                .withId("BsaWsvaK")
                .withLine(4)
                .withMessage("Reformat imports")
                .withUpdated("2014-05-28 19:04:21")
                .withAuthor(accountInfo)
                .withRobotId("importChecker")
                .withRobotRunId("12x1375aa8626ea7149792831fe2ed85e80s1r03")
                .get(),
            new RobotCommentInfoBuilder()
                .withId("TalZsvaK")
                .withLine(12)
                .withMessage("Wrong indentation")
                .withUpdated("2014-05-28 19:14:50")
                .withAuthor(accountInfo)
                .withRobotId("styleChecker")
                .withRobotRunId("24x1375aa8626ea7149792831fe2ed85e80bst12")
                .get()
        ));
    }

    private static final List<ChangeMessageInfo> MESSAGES_INFOS = new ArrayList<>();

    static {
        AccountInfo accountInfo = new AccountInfoBuilder()
            .withName("EFregnan")
            .withEmail("abc@gmail.com")
            .withUsername("efregnan")
            .withAccountId(1000000)
            .get();

        MESSAGES_INFOS.add(new ChangeMessageInfoBuilder()
            .withId("EAF")
            .withMessage("Patch Set 1: This is the first message.")
            .withDate("2019-11-28 22:28:50")
            .withAuthor(accountInfo)
            .withRevisionNumber(1)
            .get()
        );
        MESSAGES_INFOS.add(new ChangeMessageInfoBuilder()
            .withId("YH-egE")
            .withMessage("i think so")
            .withDate("2019-11-28 22:33:12")
            .withAuthor(accountInfo)
            .withRevisionNumber(2)
            .get()
        );
    }

    private CommentsParser commentsParser = new CommentsParser(getGson());

    @Test
    public void testParseCommentsFileName() throws Exception {
        TreeMap<String, List<CommentInfo>> comments = parseComments();
        Truth.assertThat(comments.keySet()).isEqualTo(COMMENT_INFOS.keySet());
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

    @Test
    public void testParseRobotCommentsFileName() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        Truth.assertThat(robotComments.keySet()).isEqualTo(ROBOT_COMMENT_INFOS.keySet());
    }

    @Test
    public void testParseRobotCommentInfosForFile() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        Function<List<RobotCommentInfo>, Integer> listSizeFunction = robotCommentInfos -> robotCommentInfos.size();
        SortedMap<String, Integer> commentsPerFile = Maps.transformValues(robotComments, listSizeFunction);
        SortedMap<String, Integer> expectedCommentsPerFile = Maps.transformValues(ROBOT_COMMENT_INFOS, listSizeFunction);

        Truth.assertThat(commentsPerFile).isEqualTo(expectedCommentsPerFile);
    }

    @Test
    public void testParseRobotCommentInfos() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        GerritAssert.assertRobotCommentsEquals(robotComments, ROBOT_COMMENT_INFOS);
    }

    @Test
    public void testParseMessageInfos() throws Exception {
        List<ChangeMessageInfo> messages = parseMessages();
        GerritAssert.assertEquals(messages, MESSAGES_INFOS);
    }

    private TreeMap<String, List<CommentInfo>> parseComments() throws Exception {
        JsonElement jsonElement = getJsonElement("comments.json");
        return commentsParser.parseCommentInfos(jsonElement);
    }

    private TreeMap<String, List<RobotCommentInfo>> parseRobotComments() throws Exception {
        JsonElement jsonElement = getJsonElement("robotcomments.json");
        return commentsParser.parseRobotCommentInfos(jsonElement);
    }

    private List<ChangeMessageInfo> parseMessages() throws Exception {
        JsonElement jsonElement = getJsonElement("messages.json");
        return commentsParser.parseChangeMessageInfos(jsonElement);
    }
}
