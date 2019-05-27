package org.cyclopsgroup.gitcon.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.base.Preconditions;

/** Resource repository that read file from Github with GraphQL Github API V4. */
public class GithubResourceRepository implements ResourceRepository {
  private static final Pattern GCS_PATTERN = Pattern.compile("^gs://((\\w|-|\\.)+)/(.+)$");

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

  private static String readGcsIfApplicable(String string) {
    Matcher m = GCS_PATTERN.matcher(string);
    if (!m.matches()) {
      return string;
    }
    Preconditions.checkState(m.groupCount() == 3, "Invalid groups %s found, 3 is expected.",
        m.groupCount());

    String bucketName = m.group(1);
    String objectKey = m.group(3);
    Storage storage = StorageOptions.getDefaultInstance().getService();
    return new String(storage.readAllBytes(bucketName, objectKey), StandardCharsets.UTF_8);
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
    this.accessToken = readGcsIfApplicable(accessToken);
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
    String document = new JSONObject(content).getJSONObject("data").getJSONObject("repository")
        .getJSONObject("content").getString("text");
    try (InputStream in = new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))) {
      consumer.consume(in);
    }
  }
}
