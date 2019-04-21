package org.cyclopsgroup.gitcon;

import java.io.File;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.cyclopsgroup.gitcon.io.ExpressionUtils;
import org.cyclopsgroup.gitcon.io.LocateableResource;

/** A simple implementation that points to a local directory to get resources */
public class FileSystemResourceRepository implements LocalResourceRepository {
  private File rootDirectory;

  public FileSystemResourceRepository(File rootDirectory) {
    Validate.isTrue(rootDirectory.isDirectory(),
        "Root directory is not a directory: " + rootDirectory);
    this.rootDirectory = rootDirectory;
  }

  public FileSystemResourceRepository(String rootDirectory) {
    this(new File(ExpressionUtils.populate(rootDirectory)));
  }

  @Override
  public File getRepositoryDirectory() {
    return rootDirectory;
  }

  @Override
  public LocateableResource getResource(String filePath) {
    return LocateableResource.fromFile(
        new File(rootDirectory.getAbsolutePath() + SystemUtils.FILE_SEPARATOR + filePath));
  }
}
