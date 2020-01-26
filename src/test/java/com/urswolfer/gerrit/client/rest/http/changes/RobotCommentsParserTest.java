package com.urswolfer.gerrit.client.rest.http.changes;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.truth.Truth;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.RobotCommentInfo;
import com.google.gson.JsonElement;
import com.urswolfer.gerrit.client.rest.http.common.*;
import org.testng.annotations.Test;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author EFregnan
 */
public class RobotCommentsParserTest extends AbstractParserTest {
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

    private RobotCommentsParser robotCommentsParser = new RobotCommentsParser(getGson());

    @Test
    public void testParseRobotCommentsFileName() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        Truth.assertThat(robotComments.keySet()).isEqualTo(ROBOT_COMMENT_INFOS.keySet());
    }

    @Test
    public void testParseCommentInfosForFile() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        Function<List<RobotCommentInfo>, Integer> listSizeFunction = robotCommentInfos -> robotCommentInfos.size();
        SortedMap<String, Integer> commentsPerFile = Maps.transformValues(robotComments, listSizeFunction);
        SortedMap<String, Integer> expectedCommentsPerFile = Maps.transformValues(ROBOT_COMMENT_INFOS, listSizeFunction);

        Truth.assertThat(commentsPerFile).isEqualTo(expectedCommentsPerFile);
    }

    @Test
    public void testParseCommentInfos() throws Exception {
        TreeMap<String, List<RobotCommentInfo>> robotComments = parseRobotComments();
        GerritAssert.assertRobotCommentsEquals(robotComments, ROBOT_COMMENT_INFOS);
    }

    private TreeMap<String, List<RobotCommentInfo>> parseRobotComments() throws Exception {
        JsonElement jsonElement = getJsonElement("robotcomments.json");
        return robotCommentsParser.parseRobotCommentInfos(jsonElement);
    }
}
