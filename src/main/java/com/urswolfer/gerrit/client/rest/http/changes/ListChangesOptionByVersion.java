/*
 * Copyright (C) 2020 GerritForge Ltd.
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

import com.google.gerrit.extensions.client.ListChangesOption;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ListChangesOptionByVersion {
    static final Map<String, ListChangesOption> MAX_CHANGE_OPTION_BY_VERSION = new HashMap<>();
    static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\..*");

    static {
        put("2.6", ListChangesOption.DETAILED_ACCOUNTS);
        put("2.7", ListChangesOption.MESSAGES);
        put("2.8", ListChangesOption.DOWNLOAD_COMMANDS);
        put("2.9", ListChangesOption.DOWNLOAD_COMMANDS);
        put("2.10", ListChangesOption.WEB_LINKS);
        put("2.11", ListChangesOption.CHANGE_ACTIONS);
        put("2.12", ListChangesOption.PUSH_CERTIFICATES);
        put("2.13", ListChangesOption.REVIEWER_UPDATES);
        put("2.14", ListChangesOption.SUBMITTABLE);
        put("2.15", ListChangesOption.TRACKING_IDS);
        put("2.16", ListChangesOption.SKIP_MERGEABLE);
    }

    private static void put(String version, ListChangesOption maxOption) {
        MAX_CHANGE_OPTION_BY_VERSION.put(version, maxOption);
    }

    static boolean isSupportedOnVersion(ListChangesOption listChangesOption, String gerritVersion) {
        Matcher versionMatcher = VERSION_PATTERN.matcher(gerritVersion);
        if (!versionMatcher.matches()) {
            // Gerrit development version, assuming to be the latest and greatest
            return true;
        }

        ListChangesOption maxOption = MAX_CHANGE_OPTION_BY_VERSION.get(versionMatcher.group(1) + "." + versionMatcher.group(2));
        if (maxOption == null) {
            // No known restrictions on known older versions, assuming to be the latest and greatest
            return true;
        }

        return listChangesOption.getValue() <= maxOption.getValue();
    }

    static EnumSet<ListChangesOption> allSupported(String gerritVersion) {
        EnumSet<ListChangesOption> changesOptions = EnumSet.noneOf(ListChangesOption.class);
        for (Iterator<ListChangesOption> optionsIter = EnumSet.allOf(ListChangesOption.class).iterator(); optionsIter.hasNext(); ) {
            ListChangesOption option = optionsIter.next();
            if (isSupportedOnVersion(option, gerritVersion) &&
                // ListChangesOption.CHECK is for triggering a Gerrit consistency check, which is not the purpose of
                // getting change details through all possible options.
                option != ListChangesOption.CHECK) {
                changesOptions.add(option);
            }
        }
        return changesOptions;
    }
}
