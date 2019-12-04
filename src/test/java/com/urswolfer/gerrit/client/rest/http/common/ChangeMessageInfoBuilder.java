package com.urswolfer.gerrit.client.rest.http.common;

import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.client.Side;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeMessageInfo;

import java.sql.Timestamp;

/**
 * @author EFregnan
 */
public class ChangeMessageInfoBuilder extends AbstractBuilder {
    private final ChangeMessageInfo changeMessageInfo = new ChangeMessageInfo();

    public ChangeMessageInfo get() {
        return changeMessageInfo;
    }

    public ChangeMessageInfoBuilder withAuthor(AccountInfo author) {
        changeMessageInfo.author = author;
        return this;
    }

    public ChangeMessageInfoBuilder withId(String id) {
        changeMessageInfo.id = id;
        return this;
    }

    public ChangeMessageInfoBuilder withTag(String tag) {
        changeMessageInfo.tag = tag;
        return this;
    }

    public ChangeMessageInfoBuilder withDate(String date) {
        changeMessageInfo.date = timestamp(date);
        return this;
    }

    public ChangeMessageInfoBuilder withRealAuthor(AccountInfo realAuthor) {
        changeMessageInfo.realAuthor = realAuthor;
        return this;
    }

    public ChangeMessageInfoBuilder withMessage(String message) {
        changeMessageInfo.message = message;
        return this;
    }

    public ChangeMessageInfoBuilder withRevisionNumber(int revisionNumber) {
        changeMessageInfo._revisionNumber = revisionNumber;
        return this;
    }
}
