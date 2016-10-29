// Copyright (C) 2016 The Android Open Source Project
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
package com.google.gerrit.extensions.api.access;

import com.google.common.base.Objects;

import java.util.Map;

public class PermissionInfo {
  public String label;
  public Boolean exclusive;
  public Map<String, PermissionRuleInfo> rules;

  public PermissionInfo(String label, Boolean exclusive) {
    this.label = label;
    this.exclusive = exclusive;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PermissionInfo) {
      PermissionInfo p = (PermissionInfo) obj;
      return Objects.equal(label, p.label)
          && Objects.equal(exclusive, p.exclusive)
          && Objects.equal(rules, p.rules);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(label, exclusive, rules);
  }
}
