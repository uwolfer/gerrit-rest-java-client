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

import com.google.gerrit.extensions.common.ActionInfo;

/**
 * @author EFregnan
 */
public class ActionInfoBuilder {
    private final ActionInfo actionInfo = new ActionInfo();

    public ActionInfo get() {
        return actionInfo;
    }

    public ActionInfoBuilder withMethod(String method) {
        actionInfo.method = method;
        return this;
    }

    public ActionInfoBuilder withLabel(String label) {
        actionInfo.label = label;
        return this;
    }

    public ActionInfoBuilder withTitle(String title) {
        actionInfo.title = title;
        return this;
    }

    public ActionInfoBuilder withEnabled(boolean enabled) {
        actionInfo.enabled = enabled;
        return this;
    }
}
