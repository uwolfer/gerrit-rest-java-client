// Copyright (C) 2021 The Android Open Source Project
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

package com.google.gerrit.extensions.common;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/** The difference between two {@link ChangeInfo}s returned by {@code ChangeInfoDiffer}. */
public final class ChangeInfoDifference {

  private final ChangeInfo oldChangeInfo;
  private final ChangeInfo newChangeInfo;
  private final ChangeInfo added;
  private final ChangeInfo removed;

  private ChangeInfoDifference(
      ChangeInfo oldChangeInfo, ChangeInfo newChangeInfo, ChangeInfo added, ChangeInfo removed) {
    this.oldChangeInfo = oldChangeInfo;
    this.newChangeInfo = newChangeInfo;
    this.added = added;
    this.removed = removed;
  }

  public ChangeInfo oldChangeInfo() {
    return oldChangeInfo;
  }

  public ChangeInfo newChangeInfo() {
    return newChangeInfo;
  }

  public ChangeInfo added() {
    return added;
  }

  public ChangeInfo removed() {
    return removed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChangeInfoDifference)) {
      return false;
    }
    ChangeInfoDifference that = (ChangeInfoDifference) o;
    return Objects.equals(oldChangeInfo, that.oldChangeInfo)
        && Objects.equals(newChangeInfo, that.newChangeInfo)
        && Objects.equals(added, that.added)
        && Objects.equals(removed, that.removed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(oldChangeInfo, newChangeInfo, added, removed);
  }

  @Override
  public String toString() {
    return "ChangeInfoDifference{"
        + "oldChangeInfo="
        + oldChangeInfo
        + ", newChangeInfo="
        + newChangeInfo
        + ", added="
        + added
        + ", removed="
        + removed
        + "}";
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ChangeInfo oldChangeInfo;
    private ChangeInfo newChangeInfo;
    private ChangeInfo added;
    private ChangeInfo removed;

    private Builder() {}

    public Builder setOldChangeInfo(ChangeInfo oldChangeInfo) {
      this.oldChangeInfo = oldChangeInfo;
      return this;
    }

    public Builder setNewChangeInfo(ChangeInfo newChangeInfo) {
      this.newChangeInfo = newChangeInfo;
      return this;
    }

    public Builder setAdded(ChangeInfo added) {
      this.added = added;
      return this;
    }

    public Builder setRemoved(ChangeInfo removed) {
      this.removed = removed;
      return this;
    }

    public ChangeInfoDifference build() {
      return new ChangeInfoDifference(
          requireNonNull(oldChangeInfo, "oldChangeInfo"),
          requireNonNull(newChangeInfo, "newChangeInfo"),
          requireNonNull(added, "added"),
          requireNonNull(removed, "removed"));
    }
  }
}
