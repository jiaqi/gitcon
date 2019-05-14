package org.cyclopsgroup.gitcon.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.json.JSONException;
import org.json.JSONObject;

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
      try {
        post(consumer, blobPath);
      } catch (JSONException e) {
        throw new IOException("Can't handle JSON.", e);
      }
    }

    @Override
    public Resource reference(String path) {
      if (path.startsWith("/")) {
        return new ResourceImpl(path.substring(1));
      }
      int lastSlash = blobPath.lastIndexOf("/");
      if (lastSlash == -1) {
        return new ResourceImpl(path);
      }
      String fullPath = blobPath.substring(0, lastSlash + 1) + path;
      return new ResourceImpl(fullPath);
    }
  }

  private String branchName = "master";

  private final String githubUser;
  private final String repositoryName;
  private final String accessToken;
  private final String bodyFormat;

  public GithubResourceRepository(String githubUser, String repositoryName, String accessToken)
      throws IOException {
    this.githubUser = githubUser;
    this.repositoryName = repositoryName;
    this.accessToken = accessToken;
    this.bodyFormat =
        IOUtils.toString(getClass().getResource("get_blob_content.gql"), StandardCharsets.UTF_8);
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

  private void post(CheckedStreamConsumer consumer, String filePath)
      throws IOException, JSONException {
    JSONObject data = new JSONObject();
    data.put("query", String.format(bodyFormat, githubUser, repositoryName, branchName, filePath));
    data.toString().replaceAll("\\s+", " ");

    HttpPost post = new HttpPost("https://api.github.com/graphql");
    post.setEntity(new StringEntity(data.toString().replaceAll("\\s+", " ")));
    if (!accessToken.isEmpty()) {
      post.addHeader("Authorization", "bearer " + accessToken);
    }

    String content;
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      try (CloseableHttpResponse response = client.execute(post)) {
        content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
      }
    }
    String document =
        new JSONObject(content)
            .getJSONObject("data")
            .getJSONObject("repository")
            .getJSONObject("content")
            .getString("text");
    try (InputStream in = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))) {
      consumer.consume(in);
    }
  }

  public static void main(String[] args) throws IOException {
    new GithubResourceRepository("jiaqi", "cloudpave-runtime-config", "<token>")
        .getResource("README.md")
        .read(i -> System.out.println(IOUtils.toString(i, StandardCharsets.UTF_8)));
  }
}
