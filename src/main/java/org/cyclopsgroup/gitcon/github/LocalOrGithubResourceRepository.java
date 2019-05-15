package org.cyclopsgroup.gitcon.github;

import java.io.File;
import org.cyclopsgroup.gitcon.FileSystemResourceRepository;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.ResourceRepository;

public class LocalOrGithubResourceRepository implements ResourceRepository {
  private final ResourceRepository delegate;

  private static ResourceRepository createFileSystemRepository() {
    String dir = System.getProperty("gitcon.dir", "src/main/config");
    return new FileSystemResourceRepository(new File(dir));
  }

  private static ResourceRepository createGithubRepository() {
    return null;
  }

  public LocalOrGithubResourceRepository() {
    String type = System.getProperty("gitcon.type", "file");
    if (type.equalsIgnoreCase("github")) {
      this.delegate = createGithubRepository();
    } else {
      this.delegate = createFileSystemRepository();
    }
  }

  @Override
  public Resource getResource(String filePath) {
    return delegate.getResource(filePath);
  }
}
