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

import com.google.gerrit.extensions.client.SubmitType;
import com.google.gerrit.extensions.common.MergeableInfo;

/**
 * @author EFregnan
 */
public class MergeableInfoBuilder extends AbstractBuilder {
    private final MergeableInfo mergeableInfo = new MergeableInfo();

    public MergeableInfo get() {
        return mergeableInfo;
    }

    public MergeableInfoBuilder withSubmitType(SubmitType submitType) {
        mergeableInfo.submitType = submitType;
        return this;
    }

    public MergeableInfoBuilder withStrategy(String strategy) {
        mergeableInfo.strategy = strategy;
        return this;
    }

    public MergeableInfoBuilder withMergeable(boolean mergeable) {
        mergeableInfo.mergeable = mergeable;
        return this;
    }
}
