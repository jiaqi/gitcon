package org.cyclopsgroup.gitcon.github;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.Resource.CheckedStreamConsumer;
import org.cyclopsgroup.gitcon.ResourceRepository;

/**
 * Example query:
 *
 * <p>query { repository(owner: "jiaqi", name: "cloudpave-runtime-config") {
 * content:object(expression: "master:cyclone-service/cyclone-service-local.properties") { ... on
 * Blob { text } } } }
 */
public class GithubResourceRepository implements ResourceRepository {
  private class ResourceImpl extends Resource {
    private final String blobPath;

    private ResourceImpl(String blobPath) {
      this.blobPath = blobPath;
    }

    @Override
    public void read(CheckedStreamConsumer consumer) throws IOException {
      post(consumer, blobPath);
    }

    @Override
    public Resource reference(String relativePath) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  private String branchName = "master";

  private final String githubUser;
  private final String repositoryName;
  private final String accessToken;

  public GithubResourceRepository(String githubUser, String repositoryName, String accessToken) {
    this.githubUser = githubUser;
    this.repositoryName = repositoryName;
    this.accessToken = accessToken;
  }

  @Override
  public Resource getResource(String filePath) {
    if (filePath.startsWith("/")) {
      filePath = filePath.substring(1);
    }
    return new ResourceImpl(filePath);
  }

  /** @param branchName the branchName to set */
  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  private void post(CheckedStreamConsumer consumer, String filePath) throws IOException {
    String bodyFormat =
        IOUtils.toString(getClass().getResource("get_blob_content.gql"), StandardCharsets.UTF_8);
    String data =
        String.format(bodyFormat, githubUser, repositoryName, branchName, filePath)
            .replaceAll("\\s+", " ");
    System.out.println(data);

    HttpPost post = new HttpPost("https://api.github.com/graphql");
    post.setEntity(new StringEntity(data));
    if (!accessToken.isEmpty()) {
      post.addHeader("Authorization", "bearer " + accessToken);
    }
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      try (CloseableHttpResponse response = client.execute(post)) {
        consumer.consume(response.getEntity().getContent());
      }
    }
  }
}
