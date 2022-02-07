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

public enum GlobalCapabilities {
  ACCESS_DATABASE("access database"),
  ADMINISTRATE_SERVER("administrate server"),
  BATCH_CHANGES_LIMIT("batch changes limit"),
  CREATE_ACCOUNT("create account"),
  CREATE_GROUP("create group"),
  CREATE_PROJECT("create project"),
  EMAIL_REVIEWERS("email reviewers"),
  FLUSH_CACHES("flush caches"),
  KILL_TASK("kill task"),
  MAINTAIN_SERVER("maintain server"),
  MODIFY_ACCOUNT("modify account"),
  PRIORITY("priority"),
  QUERY_LIMIT("query limit"),
  READ_AS("read as"),
  RUN_AS("run as"),
  RUN_GARBAGE_COLLECTION("run garbage collection"),
  STREAM_EVENTS("stream events"),
  VIEW_ACCESS("view access"),
  VIEW_ALL_ACCOUNTS("view all accounts"),
  VIEW_CACHES("view caches"),
  VIEW_CONNECTIONS("view connections"),
  VIEW_PLUGINS("view plugins"),
  VIEW_QUEUE("view queue");

  final private String capability;

  GlobalCapabilities(String label) {
    this.capability = label;
  }

  @Override
  public String toString() {
    return capability;
  }
}
