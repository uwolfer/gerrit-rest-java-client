package com.urswolfer.gerrit.client.rest.http.common;

import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.client.Side;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.FixSuggestionInfo;
import com.google.gerrit.extensions.common.RobotCommentInfo;

import java.util.List;
import java.util.Map;

/**
 * @author EFregnan
 */
public class RobotCommentInfoBuilder extends AbstractBuilder {
    private final RobotCommentInfo robotCommentInfo = new RobotCommentInfo();

    public RobotCommentInfo get() {
        return robotCommentInfo;
    }

    public RobotCommentInfoBuilder withAuthor(AccountInfo author) {
        robotCommentInfo.author = author;
        return this;
    }

    public RobotCommentInfoBuilder withId(String id) {
        robotCommentInfo.id = id;
        return this;
    }

    public RobotCommentInfoBuilder withPath(String path) {
        robotCommentInfo.path = path;
        return this;
    }

    public RobotCommentInfoBuilder withSide(Side side) {
        robotCommentInfo.side = side;
        return this;
    }

    public RobotCommentInfoBuilder withLine(int line) {
        robotCommentInfo.line = line;
        return this;
    }

    public RobotCommentInfoBuilder withRange(Comment.Range range) {
        robotCommentInfo.range = range;
        return this;
    }

    public RobotCommentInfoBuilder withInReplyTo(String inReplyTo) {
        robotCommentInfo.inReplyTo = inReplyTo;
        return this;
    }

    public RobotCommentInfoBuilder withUpdated(String updated) {
        robotCommentInfo.updated = timestamp(updated);
        return this;
    }

    public RobotCommentInfoBuilder withMessage(String message) {
        robotCommentInfo.message = message;
        return this;
    }

    public RobotCommentInfoBuilder withRobotId(String robotId) {
        robotCommentInfo.robotId = robotId;
        return this;
    }

    public RobotCommentInfoBuilder withRobotRunId(String robotRunId) {
        robotCommentInfo.robotRunId = robotRunId;
        return this;
    }

    public RobotCommentInfoBuilder withUrl(String url) {
        robotCommentInfo.url = url;
        return this;
    }

    public RobotCommentInfoBuilder withProperties(Map<String, String> properties) {
        robotCommentInfo.properties = properties;
        return this;
    }

    public RobotCommentInfoBuilder withFixSuggestions(List<FixSuggestionInfo> fixSuggestions) {
        robotCommentInfo.fixSuggestions = fixSuggestions;
        return this;
    }

}
