package org.cyclopsgroup.gitcon.github;

import java.io.File;
import java.io.IOException;
import org.cyclopsgroup.gitcon.FileSystemResourceRepository;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.ResourceRepository;

public class LocalOrGithubResourceRepository implements ResourceRepository {
  private static ResourceRepository createFileSystemRepository() {
    return new FileSystemResourceRepository(new File(getPropertyOrFail("gitcon.dir")));
  }

  private static String getPropertyOrFail(String name) {
    String value = System.getProperty(name);
    if (value == null) {
      throw new IllegalArgumentException(
          "System property " + name + " is required but not defined.");
    }
    return value;
  }

  private final ResourceRepository delegate;

  public LocalOrGithubResourceRepository(String githubUser, String githubProject,
      String accessToken) throws IOException {
    String type = System.getProperty("gitcon.type", "github");
    switch (type) {
      case "github":
        this.delegate = new GithubResourceRepository(githubUser, githubProject, accessToken);
        break;
      case "local":
        this.delegate = createFileSystemRepository();
        break;
      default:
        throw new IllegalStateException("Unexpected github.type value " + type);
    }
  }

  @Override
  public Resource getResource(String filePath) {
    return delegate.getResource(filePath);
  }
}
