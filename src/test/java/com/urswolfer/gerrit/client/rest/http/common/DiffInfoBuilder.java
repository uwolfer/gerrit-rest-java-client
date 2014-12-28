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

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.common.ChangeType;
import com.google.gerrit.extensions.common.DiffInfo;

/**
 * @author Thomas Forrer
 */
public class DiffInfoBuilder {
    private DiffInfo diffInfo = new DiffInfo();

    public DiffInfo get() {
        return diffInfo;
    }

    public DiffInfoBuilder withMetaA(DiffInfo.FileMeta metaA) {
        diffInfo.metaA = metaA;
        return this;
    }

    public DiffInfoBuilder withMetaB(DiffInfo.FileMeta metaB) {
        diffInfo.metaB = metaB;
        return this;
    }

    public DiffInfoBuilder withIntralineStatus(DiffInfo.IntraLineStatus intralineStatus) {
        diffInfo.intralineStatus = intralineStatus;
        return this;
    }

    public DiffInfoBuilder withChangeType(ChangeType changeType) {
        diffInfo.changeType = changeType;
        return this;
    }

    public DiffInfoBuilder addDiffHeader(String diffHeader) {
        if (diffInfo.diffHeader == null) {
            diffInfo.diffHeader = Lists.newArrayList();
        }
        diffInfo.diffHeader.add(diffHeader);
        return this;
    }

    public ContentEntryBuilder withContent() {
        return new ContentEntryBuilder(this);
    }

    private DiffInfoBuilder addContent(DiffInfo.ContentEntry content) {
        if (diffInfo.content == null) {
            diffInfo.content = Lists.newArrayList();
        }
        diffInfo.content.add(content);
        return this;
    }

    public static final class ContentEntryBuilder {
        private final DiffInfoBuilder parent;
        private final DiffInfo.ContentEntry contentEntry = new DiffInfo.ContentEntry();

        private ContentEntryBuilder(DiffInfoBuilder parent) {
            this.parent = parent;
        }

        public ContentEntryBuilder withAb(String line) {
            if (contentEntry.ab == null) {
                contentEntry.ab = Lists.newArrayList();
            }
            contentEntry.ab.add(line);
            return this;
        }

        public ContentEntryBuilder withA(String line) {
            if (contentEntry.a == null) {
                contentEntry.a = Lists.newArrayList();
            }
            contentEntry.a.add(line);
            return this;
        }

        public ContentEntryBuilder withB(String line) {
            if (contentEntry.b == null) {
                contentEntry.b = Lists.newArrayList();
            }
            contentEntry.b.add(line);
            return this;
        }

        public DiffInfoBuilder done() {
            return parent.addContent(contentEntry);
        }
    }
}
