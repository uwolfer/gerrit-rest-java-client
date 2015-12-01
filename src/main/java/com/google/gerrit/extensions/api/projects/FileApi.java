package com.google.gerrit.extensions.api.projects;

import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.NotImplementedException;
import com.google.gerrit.extensions.restapi.RestApiException;

public interface FileApi {

  /**
   * Returns the content of the file from the HEAD revision as a Base64 encoded <code>BinaryResult</code>. 
   * A {@code RestApiException} will be thrown if the file not exists.
   */
  BinaryResult content() throws RestApiException;

  /**
   * A default implementation which allows source compatibility when adding new
   * methods to the interface.
   **/
  public class NotImplemented implements FileApi {
    @Override
    public BinaryResult content() throws RestApiException {
      throw new NotImplementedException();
    }
  }
}
