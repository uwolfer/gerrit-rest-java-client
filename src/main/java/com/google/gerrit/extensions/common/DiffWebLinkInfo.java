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

public class DiffWebLinkInfo extends WebLinkInfo {
  public Boolean showOnSideBySideDiffView;
  public Boolean showOnUnifiedDiffView;

  public static DiffWebLinkInfo forSideBySideDiffView(String name, String imageUrl, String url) {
    return new DiffWebLinkInfo(name, imageUrl, url, true, false);
  }

  /** @deprecated removed upstream; kept for source/binary compatibility with existing callers. */
  @Deprecated
  public static DiffWebLinkInfo forSideBySideDiffView(
      String name, String imageUrl, String url, String target) {
    DiffWebLinkInfo info = forSideBySideDiffView(name, imageUrl, url);
    info.target = target;
    return info;
  }

  public static DiffWebLinkInfo forUnifiedDiffView(String name, String imageUrl, String url) {
    return new DiffWebLinkInfo(name, imageUrl, url, false, true);
  }

  /** @deprecated removed upstream; kept for source/binary compatibility with existing callers. */
  @Deprecated
  public static DiffWebLinkInfo forUnifiedDiffView(
      String name, String imageUrl, String url, String target) {
    DiffWebLinkInfo info = forUnifiedDiffView(name, imageUrl, url);
    info.target = target;
    return info;
  }

  public static DiffWebLinkInfo forSideBySideAndUnifiedDiffView(
      String name, String imageUrl, String url) {
    return new DiffWebLinkInfo(name, imageUrl, url, true, true);
  }

  /** @deprecated removed upstream; kept for source/binary compatibility with existing callers. */
  @Deprecated
  public static DiffWebLinkInfo forSideBySideAndUnifiedDiffView(
      String name, String imageUrl, String url, String target) {
    DiffWebLinkInfo info = forSideBySideAndUnifiedDiffView(name, imageUrl, url);
    info.target = target;
    return info;
  }

  private DiffWebLinkInfo(
      String name,
      String imageUrl,
      String url,
      boolean showOnSideBySideDiffView,
      boolean showOnUnifiedDiffView) {
    super(name, imageUrl, url);
    this.showOnSideBySideDiffView = showOnSideBySideDiffView ? true : null;
    this.showOnUnifiedDiffView = showOnUnifiedDiffView ? true : null;
  }
}
