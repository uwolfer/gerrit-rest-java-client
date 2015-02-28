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

package com.urswolfer.gerrit.client.rest.http.common;

import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.client.Side;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.CommentInfo;

/**
 * @author Thomas Forrer
 */
public class CommentInfoBuilder extends AbstractBuilder {
    private final CommentInfo commentInfo = new CommentInfo();

    public CommentInfo get() {
        return commentInfo;
    }

    public CommentInfoBuilder withAuthor(AccountInfo author) {
        commentInfo.author = author;
        return this;
    }

    public CommentInfoBuilder withId(String id) {
        commentInfo.id = id;
        return this;
    }

    public CommentInfoBuilder withPath(String path) {
        commentInfo.path = path;
        return this;
    }

    public CommentInfoBuilder withSide(Side side) {
        commentInfo.side = side;
        return this;
    }

    public CommentInfoBuilder withLine(int line) {
        commentInfo.line = line;
        return this;
    }

    public CommentInfoBuilder withRange(Comment.Range range) {
        commentInfo.range = range;
        return this;
    }

    public CommentInfoBuilder withInReplyTo(String inReplyTo) {
        commentInfo.inReplyTo = inReplyTo;
        return this;
    }

    public CommentInfoBuilder withUpdated(String updated) {
        commentInfo.updated = timestamp(updated);
        return this;
    }

    public CommentInfoBuilder withMessage(String message) {
        commentInfo.message = message;
        return this;
    }
}
