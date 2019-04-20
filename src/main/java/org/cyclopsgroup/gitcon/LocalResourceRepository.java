package org.cyclopsgroup.gitcon;

import java.io.File;

/** A resource repo that is aware of local root directory */
public interface LocalResourceRepository extends ResourceRepository {
  /**
   * Get root directory of local repository
   *
   * @return Root directory of local repository
   */
  File getRepositoryDirectory();
}
