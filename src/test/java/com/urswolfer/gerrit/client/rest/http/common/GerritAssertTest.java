/*
 * Copyright 2013-2026 Urs Wolfer
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

import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.RevisionInfo;
import com.google.gerrit.extensions.common.RobotCommentInfo;
import org.testng.annotations.Test;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * {@link GerritAssert} is what a dozen parsing tests rely on to say two objects are the same, so it
 * has to notice when they are not. Each case here is a difference it must catch, or a sameness it
 * must not reject.
 */
public class GerritAssertTest {

    @Test
    public void equalObjectsPass() {
        GerritAssert.assertEquals(account(1, "Jane"), account(1, "Jane"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aDifferentFieldFails() {
        GerritAssert.assertEquals(account(1, "Jane"), account(1, "John"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aFieldSetOnOnlyOneSideFails() {
        AccountInfo withEmail = account(1, "Jane");
        withEmail.email = "jane@example.com";
        GerritAssert.assertEquals(withEmail, account(1, "Jane"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aDifferenceDeepInsideFails() {
        GerritAssert.assertEquals(change("abc", revision(1)), change("abc", revision(2)));
    }

    /**
     * {@code RevisionInfo.isCurrent} is transient, which a JSON-based comparison would skip.
     */
    @Test(expectedExceptions = AssertionError.class)
    public void aTransientFieldIsCompared() {
        RevisionInfo current = revision(1);
        current.isCurrent = true;
        GerritAssert.assertEquals(change("abc", current), change("abc", revision(1)));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void timestampsAreComparedToTheNanosecond() {
        Timestamp later = new Timestamp(1_000_000L);
        later.setNanos(later.getNanos() + 1);
        GerritAssert.assertEquals(new Timestamp(1_000_000L), later);
    }

    // types

    /**
     * A parser returning the base class where a subclass was expected must not pass just because
     * the subclass's own fields happen to be unset.
     */
    @Test(expectedExceptions = AssertionError.class)
    public void aSubclassIsNotItsBaseClass() {
        CommentInfo comment = new CommentInfo();
        comment.id = "1";
        RobotCommentInfo robotComment = new RobotCommentInfo();
        robotComment.id = "1";
        GerritAssert.assertEquals(comment, robotComment);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void numberTypesAreCompared() {
        GerritAssert.assertEquals(1, 1L);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aListIsNotASet() {
        GerritAssert.assertEquals(Collections.singletonList("a"), new LinkedHashSet<>(Collections.singletonList("a")));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aMapIsNotASortedMap() {
        GerritAssert.assertEquals(new HashMap<>(Collections.singletonMap("a", 1)),
            new TreeMap<>(Collections.singletonMap("a", 1)));
    }

    /**
     * Which List or Map class holds the values is the parser's business, not the test's.
     */
    @Test
    public void collectionImplementationsAreInterchangeable() {
        GerritAssert.assertEquals(new ArrayList<>(Arrays.asList("a", "b")), ImmutableList.of("a", "b"));
        GerritAssert.assertEquals(new LinkedHashMap<>(Collections.singletonMap("a", 1)), Collections.singletonMap("a", 1));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void mapKeyTypesAreCompared() {
        GerritAssert.assertEquals(Collections.singletonMap(1, "v"), Collections.singletonMap("1", "v"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void differentTypesFailEvenWhenBothAreEmpty() {
        GerritAssert.assertEquals(new ChangeInfo(), new AccountInfo(null));
    }

    // nulls

    @Test(expectedExceptions = AssertionError.class)
    public void aKeyMappedToNullIsNotAnAbsentKey() {
        GerritAssert.assertEquals(Collections.singletonMap("x", null), Collections.emptyMap());
    }

    @Test
    public void nullsCompareEqual() {
        GerritAssert.assertEquals(null, null);
        GerritAssert.assertEquals(Collections.singletonMap("x", null), Collections.singletonMap("x", null));
    }

    // order and contents

    @Test(expectedExceptions = AssertionError.class)
    public void listOrderMatters() {
        GerritAssert.assertEquals(Arrays.asList(account(1, "a"), account(2, "b")),
            Arrays.asList(account(2, "b"), account(1, "a")));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void mapOrderMatters() {
        Map<String, Integer> ab = new LinkedHashMap<>();
        ab.put("a", 1);
        ab.put("b", 2);
        Map<String, Integer> ba = new LinkedHashMap<>();
        ba.put("b", 2);
        ba.put("a", 1);
        GerritAssert.assertEquals(ab, ba);
    }

    @Test(expectedExceptions = AssertionError.class)
    public void setContentsAreCompared() {
        GerritAssert.assertEquals(new LinkedHashSet<>(Arrays.asList("a", "b")),
            new LinkedHashSet<>(Arrays.asList("a", "c")));
    }

    // values a JSON writer would get wrong or refuse

    @Test
    public void notANumberEqualsItself() {
        GerritAssert.assertEquals(Double.NaN, Double.NaN);
    }

    /**
     * Upstream Gerrit is moving its types from Timestamp to Instant; neither may need reflection
     * into java.base.
     */
    @Test
    public void jdkValueTypesAreComparedWithoutReflectingIntoThem() {
        GerritAssert.assertEquals(Instant.ofEpochSecond(1), Instant.ofEpochSecond(1));
        GerritAssert.assertEquals(Optional.of("x"), Optional.of("x"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void optionalContentsAreCompared() {
        GerritAssert.assertEquals(Optional.of("x"), Optional.empty());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void anUnknownJdkTypeIsAnErrorNotAGuess() {
        GerritAssert.assertEquals(URI.create("http://a"), URI.create("http://a"));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void arrayComponentTypesAreCompared() {
        GerritAssert.assertEquals(new int[] {1}, new Integer[] {1});
    }

    @Test(expectedExceptions = AssertionError.class)
    public void aStringArrayIsNotAnObjectArray() {
        GerritAssert.assertEquals(new String[] {"a"}, new Object[] {"a"});
    }

    @Test
    public void equalArraysPass() {
        GerritAssert.assertEquals(new int[] {1, 2}, new int[] {1, 2});
    }

    /**
     * Double-brace fixtures are anonymous subclasses. Both sides here come from the one anonymous
     * class, so only the field value differs.
     */
    @Test(expectedExceptions = AssertionError.class)
    public void anonymousSubclassesAreComparedByTheirFields() {
        GerritAssert.assertEquals(anonymousAccount("Jane", "x"), anonymousAccount("John", "x"));
    }

    /**
     * What an anonymous class captures - the enclosing test, a local variable - lives in synthetic
     * fields the compiler adds, which are not part of the value.
     */
    @Test
    public void whatAnAnonymousClassCapturesIsNotCompared() {
        GerritAssert.assertEquals(anonymousAccount("Jane", "x"), anonymousAccount("Jane", "y"));
    }

    @SuppressWarnings("serial")
    private AccountInfo anonymousAccount(String accountName, String captured) {
        return new AccountInfo(1) {{
            name = accountName;
            Objects.requireNonNull(captured); // referenced, so the compiler captures it
        }};
    }

    private static AccountInfo account(int id, String name) {
        AccountInfo account = new AccountInfo(id);
        account.name = name;
        return account;
    }

    private static RevisionInfo revision(int number) {
        RevisionInfo revision = new RevisionInfo();
        revision._number = number;
        return revision;
    }

    private static ChangeInfo change(String id, RevisionInfo revision) {
        ChangeInfo change = new ChangeInfo();
        change.id = id;
        change.revisions = Collections.singletonMap("sha1", revision);
        return change;
    }
}
