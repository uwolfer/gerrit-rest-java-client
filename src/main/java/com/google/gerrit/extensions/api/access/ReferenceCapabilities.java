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

public enum ReferenceCapabilities {
  ABANDON("abandon"),
  CREATE_REFERENCE("create reference"),
  DELETE_REFERENCE("delete reference"),
  FORGE_AUTHOR("forge author"),
  FORGE_COMMITTER("forge committer"),
  FORGE_SERVER("forge server"),
  OWNER("owner"),
  PUSH("push"),
  DIRECT_PUSH("direct push"),
  ADD_PATCH_SET("add patch set"),
  UPLOAD_TO_CODE_REVIEW("upload to code review"),
  PUSH_MERGE_COMMITS("push merge commits"),
  CREATE_ANNOTATED_TAG("create annotated tag"),
  CREATE_SIGNED_TAG("create signed tag"),
  READ("read"),
  REBASE("rebase"),
  REVIEW_LABELS("review label"),
  SUBMIT("submit"),
  SUBMIT_ON_BEHALF_OF("submit (on behalf of)"),
  VIEW_PRIVATE_CHANGES("view private changes"),
  DELETE_OWN_CHANGES("delete own changes"),
  DELETE_CHANGES("delete changes"),
  EDIT_TOPIC_NAME("edit topic name"),
  EDIT_HASHTAGS("edit hashtags"),
  EDIT_ASSIGNEE("edit assignee");

  final private String capability;

  ReferenceCapabilities(String label) {
    this.capability = label;
  }

  @Override
  public String toString() {
    return capability;
  }
}
