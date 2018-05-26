// Copyright (C) 2014 The Android Open Source Project
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

import java.sql.Timestamp;

public class ChangeMessageInfo {
  public String id;
  public String tag;
  public AccountInfo author;
  public AccountInfo realAuthor;
  public Timestamp date;
  public String message;
  public Integer _revisionNumber;

  @Override
  public boolean equals(Object o) {
    if (o instanceof ChangeMessageInfo) {
      ChangeMessageInfo cmi = (ChangeMessageInfo) o;
      return Objects.equal(id, cmi.id)
          && Objects.equal(tag, cmi.tag)
          && Objects.equal(author, cmi.author)
          && Objects.equal(realAuthor, cmi.realAuthor)
          && Objects.equal(date, cmi.date)
          && Objects.equal(message, cmi.message)
          && Objects.equal(_revisionNumber, cmi._revisionNumber);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, tag, author, realAuthor, date, message, _revisionNumber);
  }
}
