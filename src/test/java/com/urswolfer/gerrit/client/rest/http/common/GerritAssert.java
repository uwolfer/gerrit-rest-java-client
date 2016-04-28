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

import com.google.common.truth.Truth;
import com.google.gerrit.extensions.api.projects.BranchInfo;
import com.google.gerrit.extensions.api.projects.TagInfo;
import com.google.gerrit.extensions.common.*;
import com.thoughtworks.xstream.XStream;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Forrer
 */
public class GerritAssert {

    /**
     * Assert that two {@link com.google.gerrit.extensions.common.ChangeInfo}s are equal, i.e. all their properties
     * are equal.
     *
     * Note: This is currently achieved by streaming both objects in an XML output, since the
     * {@link com.google.gerrit.extensions.common.ChangeInfo} class does not implement <code>equals()</code> and
     * <code>hashCode()</code> methods.
     */
    public static void assertEquals(ChangeInfo actual, ChangeInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(ProjectInfo actual, ProjectInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(BranchInfo actual, BranchInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(TagInfo actual, TagInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(AccountInfo actual, AccountInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(GroupInfo actual, GroupInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(TreeMap<String, List<CommentInfo>> actual, TreeMap<String, List<CommentInfo>> expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(Map<String, FileInfo> actual, Map<String, FileInfo> expected) {
        assertXmlOutputEqual(actual, expected);
    }

    public static void assertEquals(DiffInfo actual, DiffInfo expected) {
        assertXmlOutputEqual(actual, expected);
    }

    private static void assertXmlOutputEqual(Object actual, Object expected) {
        XStream xStream = new XStream();
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.aliasSystemAttribute(null, "class"); // we only care about the content (but not internal attributes)
        xStream.aliasSystemAttribute(null, "resolves-to");
        String actualXml = xStream.toXML(actual);
        String expectedXml = xStream.toXML(expected);

        Truth.assertThat(actualXml).isEqualTo(expectedXml);
    }
}
