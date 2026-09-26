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

package com.urswolfer.gerrit.client.rest.contract;

import com.google.gerrit.extensions.api.accounts.EmailInput;
import com.urswolfer.gerrit.client.rest.http.common.FakeGerritServer;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Pins how the values a caller passes are put into the URL.
 *
 * <p>Many of these used to go out as given. Each case shows one of three things: a plain value
 * goes out as before; a value that used to break the request - throw, split into several path
 * segments, or be cut off - is escaped; and a value the caller already encoded to work around
 * that is not encoded a second time.
 */
public class EncodingContractTest {

    private static final String CHANGE_ID = "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940";

    // change queries

    /**
     * A space is not allowed in a URL, so this used to throw an IllegalArgumentException.
     */
    @Test
    public void aMultiWordQueryIsEscapedInsteadOfThrowing() throws Exception {
        FakeGerritServer server = changesQuery("status:open%20owner:self");
        server.api().changes().query("status:open owner:self").get();
        server.verify();
    }

    /**
     * Until now the only way to send a multi-word query; it has to keep working unchanged.
     */
    @Test
    public void aQueryWrittenWithPlusForSpaceGoesOutUnchanged() throws Exception {
        FakeGerritServer server = changesQuery("status:open+owner:self");
        server.api().changes().query("status:open+owner:self").get();
        server.verify();
    }

    @Test
    public void aQueryTheCallerEncodedGoesOutUnchanged() throws Exception {
        FakeGerritServer server = changesQuery("status%3Aopen%20owner%3Aself");
        server.api().changes().query("status%3Aopen%20owner%3Aself").get();
        server.verify();
    }

    /**
     * An {@code &} used to end the query and start a new parameter.
     */
    @Test
    public void anAmpersandStaysInsideTheQuery() throws Exception {
        FakeGerritServer server = changesQuery("message:a%26b");
        server.api().changes().query("message:a&b").get();
        server.verify();
    }

    /**
     * A {@code #} used to turn the rest of the URL into a fragment, which is never sent: here the
     * limit was dropped along with the end of the query.
     */
    @Test
    public void aHashDoesNotCutTheUrlOff() throws Exception {
        FakeGerritServer server = changesQuery("message:%231&n=5");
        server.api().changes().query("message:#1").withLimit(5).get();
        server.verify();
    }

    /**
     * A {@code %} that does not start an escape used to make the URL invalid.
     */
    @Test
    public void aStrayPercentIsEscaped() throws Exception {
        FakeGerritServer server = changesQuery("message:50%25");
        server.api().changes().query("message:50%").get();
        server.verify();
    }

    @Test
    public void labelPredicatesGoOutUnchanged() throws Exception {
        FakeGerritServer server = changesQuery("label:Code-Review=2");
        server.api().changes().query("label:Code-Review=2").get();
        server.verify();
    }

    // branches and tags

    @Test
    public void aPlainBranchNameGoesOutUnchanged() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/branches/master", "projects/branch.json");
        server.api().projects().name("p").branch("master").get();
        server.verify();
    }

    /**
     * This used to delete the branch {@code feature}: the {@code #1} became a fragment.
     */
    @Test
    public void aHashInABranchNameDoesNotReachAnotherBranch() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("DELETE", "/projects/p/branches/feature%231");
        server.api().projects().name("p").branch("feature#1").delete();
        server.verify();
    }

    /**
     * Git refs cannot contain a space, so a {@code +} in one is a literal plus. Gerrit decodes a
     * bare {@code +} as a space.
     */
    @Test
    public void aPlusInABranchNameIsALiteralPlus() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/branches/c%2B%2B", "projects/branch.json");
        server.api().projects().name("p").branch("c++").get();
        server.verify();
    }

    @Test
    public void aTagRefIsOnePathSegment() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/tags/refs%2Ftags%2Fv1.0", "projects/tag.json");
        server.api().projects().name("p").tag("refs/tags/v1.0").get();
        server.verify();
    }

    // emails

    @Test
    public void aPlainEmailGoesOutUnchanged() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("DELETE", "/accounts/self/emails/john@example.com");
        server.api().accounts().self().email("john@example.com").delete();
        server.verify();
    }

    /**
     * Used to reach {@code john tag@example.com}, since Gerrit decodes a bare {@code +} as a space.
     */
    @Test
    public void aPlusInAnEmailIsALiteralPlus() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("DELETE", "/accounts/self/emails/john%2Btag@example.com");
        server.api().accounts().self().email("john+tag@example.com").delete();
        server.verify();
    }

    @Test
    public void createEmailEscapesTheAddressToo() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("PUT", "/accounts/self/emails/john%2Btag@example.com");
        EmailInput input = new EmailInput();
        input.email = "john+tag@example.com";
        server.api().accounts().self().createEmail(input);
        server.verify();
    }

    // change edit files

    /**
     * {@code getFile} used to send its path as given: a directory separator split it across
     * several URL segments, and a space threw. It is now one path segment, matching Gerrit's own
     * doc examples for this endpoint family ({@code PUT .../edit/path%2fto%2ffile}).
     */
    @Test
    public void aChangeEditFilePathIsOnePathSegment() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubRaw("GET", "/changes/" + CHANGE_ID + "/edit/dir%2FMy%20File.txt", "text/plain", "x",
                Collections.<String, String>emptyMap());
        server.api().changes().id(CHANGE_ID).edit().getFile("dir/My File.txt");
        server.verify();
    }

    /**
     * A path a caller already encoded to work around the bug above - for instance one that spells
     * its own slash as {@code %2F} - must not be encoded a second time. Unlike {@code getFile},
     * {@code modifyFile} and {@code deleteFile} have always run every path through
     * {@code Url.encode}, which has no such guarantee; this fix does not touch them.
     */
    @Test
    public void anAlreadyEncodedChangeEditFilePathIsNotEncodedAgain() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubRaw("GET", "/changes/" + CHANGE_ID + "/edit/dir%2FMy%20File.txt", "text/plain", "x",
                Collections.<String, String>emptyMap());
        server.api().changes().id(CHANGE_ID).edit().getFile("dir%2FMy%20File.txt");
        server.verify();
    }

    // ref patterns

    /**
     * Ref names cannot contain a space, so a {@code +} in a branch regex is the quantifier. As a
     * bare {@code +} Gerrit would read a space and match nothing, with no error.
     */
    @Test
    public void aPlusInABranchRegexIsTheQuantifier() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/branches?r=%5Erelease-%5B0-9%5D%2B$", "projects/branches.json");
        server.api().projects().name("p").branches().withRegex("^release-[0-9]+$").get();
        server.verify();
    }

    @Test
    public void aPlusInABranchSubstringIsALiteralPlus() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/branches?m=c%2B%2B", "projects/branches.json");
        server.api().projects().name("p").branches().withSubstring("c++").get();
        server.verify();
    }

    @Test
    public void aBranchRegexTheCallerEncodedGoesOutUnchanged() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/p/branches?r=%5Erelease-%5B0-9%5D%2B%24", "projects/branches.json");
        server.api().projects().name("p").branches().withRegex("%5Erelease-%5B0-9%5D%2B%24").get();
        server.verify();
    }

    // change ids
    //
    // Gerrit hands a change id back (ChangeInfo.id) already encoded with Url.encode, and its docs
    // require anyone building one by hand to encode it too. So id(String) sends it unchanged, and
    // the typed id(project, ...) overloads encode with Url.encode to match the server.

    @Test
    public void anIdGivenAsAStringGoesOutCompletelyUnchanged() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("POST", "/changes/my+project~master~I1/abandon");
        server.api().changes().id("my+project~master~I1").abandon();
        server.verify();
    }

    @Test
    public void aSpaceInATypedChangeIdProjectIsEncodedTheSameWayGerritEncodesIt() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stubEmpty("POST", "/changes/my+project~master~I1/abandon");
        server.api().changes().id("my project", "master", "I1").abandon();
        server.verify();
    }

    // project listing

    /**
     * A branch to show in a project list is a ref name, so it cannot contain a space either.
     */
    @Test
    public void aPlusInAShowBranchIsALiteralPlus() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/?b=feature%2B1", "projects/projects.json");
        server.api().projects().list().addShowBranch("feature+1").get();
        server.verify();
    }

    /**
     * A project name prefix is matched against project names, which - like a ref name - cannot
     * contain a space, so a {@code +} in one is the literal plus it must be.
     */
    @Test
    public void aPlusInAProjectPrefixIsALiteralPlus() throws Exception {
        FakeGerritServer server = new FakeGerritServer()
            .stub("GET", "/projects/?p=lib%2Bcore", "projects/projects.json");
        server.api().projects().list().withPrefix("lib+core").get();
        server.verify();
    }

    private static FakeGerritServer changesQuery(String encodedQuery) {
        return new FakeGerritServer().stub("GET", "/changes/?q=" + encodedQuery, "changes/changes.json");
    }
}
