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

package com.google.gerrit.extensions.api.changes;

import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;

/**
 * Support for the REST endpoints provided by the Gerrit Verify Status plugin
 * @author zaro0508
 *
 */
public interface VerifyStatusApi {
  void verifications(VerifyInput verifyInput) throws RestApiException;


  /**
   * A default implementation which allows source compatibility
   * when adding new methods to the interface.
   **/
  public class NotImplemented implements VerifyStatusApi {
    @Override
    public void verifications(VerifyInput verifyInput) throws RestApiException {
      throw new NotImplementedException();
    }
  }
}