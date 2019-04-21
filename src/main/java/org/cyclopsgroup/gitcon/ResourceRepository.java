package org.cyclopsgroup.gitcon;

import org.cyclopsgroup.gitcon.io.LocateableResource;

/** Interface that expose files for given file path */
public interface ResourceRepository {
  /**
   * Get file object for given relative file path in repository
   *
   * @param filePath File path relative to the root of repository
   * @return A file object. File may not exist if file path points to no where, in which case this
   *     call does not fail.
   */
  LocateableResource getResource(String filePath);
}
