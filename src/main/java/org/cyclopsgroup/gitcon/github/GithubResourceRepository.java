package org.cyclopsgroup.gitcon.github;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;
import org.cyclopsgroup.gitcon.Resource;
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
    public InputStream openToRead() throws IOException {
      return post(blobPath);
    }

    @Override
    public Resource reference(String relativePath) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  private String accessToken = "";
  private String branchName = "master";

  private final String githubUser;
  private final String repositoryName;

  public GithubResourceRepository(String githubUser, String repositoryName) {
    this.githubUser = githubUser;
    this.repositoryName = repositoryName;
  }

  @Override
  public Resource getResource(String filePath) {
    if (filePath.startsWith("/")) {
      filePath = filePath.substring(1);
    }
    return new ResourceImpl(filePath);
  }

  /** @param accessToken the accessToken to set */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /** @param branchName the branchName to set */
  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  private InputStream post(String filePath) throws IOException {
    URL target = new URL("https://api.github.com/graphql");
    HttpsURLConnection con = (HttpsURLConnection) target.openConnection();

    con.setRequestMethod("POST");
    if (!accessToken.isEmpty()) {
      con.setRequestProperty("Authorization", "bearer " + accessToken);
    }
    con.setDoOutput(true);

    String bodyFormat =
        IOUtils.toString(getClass().getResource("get_blob_content.gql"), StandardCharsets.UTF_8);
    String data = String.format(bodyFormat, githubUser, repositoryName, branchName, filePath);

    con.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
    con.getOutputStream().flush();
    con.getOutputStream().close();

    InputStream in =
        new FilterInputStream(con.getInputStream()) {
          @Override
          public void close() throws IOException {
            try {
              super.close();
            } finally {
              con.disconnect();
            }
          }
        };
    return in;
  }

  public static void main(String[] args) throws IOException {
    GithubResourceRepository repo = new GithubResourceRepository("jiaqi", "jcli");
    String s = IOUtils.toString(repo.getResource("pom.xml").openToRead(), StandardCharsets.UTF_8);
    System.out.println(s);
  }
}
