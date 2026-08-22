// Copyright (C) 2025 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.extensions.api.changes;

import static com.google.common.base.Preconditions.checkState;

import com.google.gerrit.extensions.restapi.Url;
import java.util.Objects;

/**
 * Identifier for a change to be used in the {@link Changes} API to get the {@link ChangeApi} for a
 * change.
 *
 * <p>Supports any change identifier that is supported by the REST API, including {@code
 * <project>~<changeNumber>}, {@code <changeId>} (I-hash), {@code <project>~<branch>~<changeId>}
 * (aka as triplet) and {@code <changeNumber>} (numeric change ID).
 */
public final class ChangeIdentifier {
  private final String id;

  private ChangeIdentifier(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  @Override
  public final String toString() {
    return id();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ChangeIdentifier)) {
      return false;
    }
    return id.equals(((ChangeIdentifier) o).id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  /**
   * Creates a change identifier in the format {@code <project>~<changeNumber>}.
   *
   * <p>Always uniquely identifies one change.
   *
   * <p>This is the preferred identifier for identifying changes.
   *
   * @param projectName the name of the project that contains the change
   * @param numericChangeId the numeric change ID (aka as change number)
   */
  public static ChangeIdentifier byProjectAndNumericChangeId(
      String projectName, int numericChangeId) {
    return new ChangeIdentifier(Url.encode(projectName) + "~" + numericChangeId);
  }

  /**
   * Creates a change identifier in the format {@code <project>~<branch>~<changeId>}.
   *
   * <p>Always uniquely identifies one change.
   *
   * @param projectName the name of the project that contains the change
   * @param branchName the name of the branch that contains the change, may be the short branch name
   *     without the {@code refs/heads/} prefix.
   * @param changeId the I-hash of the change
   */
  public static ChangeIdentifier byTriplet(String projectName, String branchName, String changeId) {
    checkState(changeId.startsWith("I"), "expected changeId to be the I-hash");
    return new ChangeIdentifier(
        Url.encode(projectName) + "~" + Url.encode(branchName) + "~" + changeId);
  }

  /**
   * Creates a change identifier in the format {@code <changeId>}.
   *
   * <p>May not always identify a single change (e.g. it matches multiple changes if a change has
   * been cherry-picked).
   *
   * @param changeId the I-hash of the change
   */
  public static ChangeIdentifier byChangeId(String changeId) {
    checkState(changeId.startsWith("I"), "expected changeId to be the I-hash");
    return new ChangeIdentifier(changeId);
  }

  /**
   * Creates a change identifier in the format {@code <changeNumber>}.
   *
   * <p>May not always identify a single change (e.g. when changes have been imported from another
   * Gerrit instance).
   *
   * @param numericChangeId the numeric change ID (aka as change number)
   */
  public static ChangeIdentifier byNumericChangeId(int numericChangeId) {
    return new ChangeIdentifier(Integer.valueOf(numericChangeId).toString());
  }
}
