// Copyright (C) 2018 The Android Open Source Project
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

import com.google.common.base.Objects;

import java.util.Map;

public class SubmitRequirementInfo {
  public final String status;
  public final String fallbackText;
  public final String type;
  public final Map<String, String> data;

  public SubmitRequirementInfo(
      String status, String fallbackText, String type, Map<String, String> data) {
    this.status = status;
    this.fallbackText = fallbackText;
    this.type = type;
    this.data = data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SubmitRequirementInfo)) {
      return false;
    }
    SubmitRequirementInfo that = (SubmitRequirementInfo) o;
    return Objects.equal(status, that.status)
        && Objects.equal(fallbackText, that.fallbackText)
        && Objects.equal(type, that.type)
        && Objects.equal(data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(status, fallbackText, type, data);
  }
}
