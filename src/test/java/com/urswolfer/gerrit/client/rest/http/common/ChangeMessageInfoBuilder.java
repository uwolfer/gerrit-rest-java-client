/*
 * Copyright 2013-2020 Urs Wolfer
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

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeMessageInfo;

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
