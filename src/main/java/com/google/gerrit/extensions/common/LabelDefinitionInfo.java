// Copyright (C) 2019 The Android Open Source Project
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

import java.util.List;
import java.util.Map;

public class LabelDefinitionInfo {
  public String name;
  public String description;
  public String projectName;
  public String function;
  public Map<String, String> values;
  public short defaultValue;
  public List<String> branches;
  public Boolean canOverride;

  // The individual copy-rule flags below were replaced upstream by the single copyCondition
  // expression further down. They are kept here, deprecated, for source/binary compatibility with
  // existing callers.

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyAnyScore;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyMinScore;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyMaxScore;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyAllScoresIfNoChange;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyAllScoresIfNoCodeChange;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyAllScoresOnTrivialRebase;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public Boolean copyAllScoresOnMergeFirstParentUpdate;

  /** @deprecated removed upstream in favor of {@link #copyCondition}. */
  @Deprecated public List<Short> copyValues;

  public String copyCondition;
  public Boolean allowPostSubmit;
  public Boolean ignoreSelfApproval;
}
