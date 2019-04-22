package org.cyclopsgroup.gitcon.github;

import java.io.IOException;
import java.io.InputStream;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.ResourceRepository;

/**
 * Example query:
 * 
 * query { 
 *   repository(owner: "jiaqi", name: "cloudpave-runtime-config") {
 *     content:object(expression: "master:cyclone-service/cyclone-service-local.properties") {
 *      ... on Blob {
 *         text
 *       }
 *     }
 *   }
 * }
 */
public class GithubResourceRepository implements ResourceRepository {
  private class ResourceImpl extends Resource {
    private final String blobPath;

    private ResourceImpl(String blobPath) {
      this.blobPath = blobPath;
    }

    @Override
    public InputStream openToRead() throws IOException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Resource reference(String relativePath) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  private final String accessToken;
  private final String branchName;
  private final String githubUser;
  private final String repositoryName;

  public GithubResourceRepository(String accessToken, String githubUser, String repositoryName,
      String branchName) {
    this.accessToken = accessToken;
    this.githubUser = githubUser;
    this.repositoryName = repositoryName;
    this.branchName = branchName;
  }

  @Override
  public Resource getResource(String filePath) {
    if (filePath.startsWith("/")) {
      filePath = filePath.substring(1);
    }
    return new ResourceImpl(filePath);
  }
}
