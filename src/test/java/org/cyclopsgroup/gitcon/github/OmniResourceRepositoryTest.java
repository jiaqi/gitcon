package org.cyclopsgroup.gitcon.github;

import static com.google.common.truth.Truth.assertThat;
import java.io.File;
import java.io.IOException;
import org.cyclopsgroup.gitcon.FileSystemResourceRepository;
import org.junit.Test;

public class OmniResourceRepositoryTest {
  private static GithubResourceRepository verifyNames(OmniResourceRepository repo,
      String expectedUser, String expectedRepo) {
    assertThat(repo.getDelegateRepository()).isInstanceOf(GithubResourceRepository.class);
    GithubResourceRepository githubRepo = (GithubResourceRepository) repo.getDelegateRepository();
    assertThat(githubRepo.getGithubUser()).isEqualTo(expectedUser);
    assertThat(githubRepo.getRepositoryName()).isEqualTo(expectedRepo);
    return githubRepo;
  }

  @Test
  public void testFileRepository() throws IOException {
    OmniResourceRepository repo = new OmniResourceRepository("file:/tmp");
    assertThat(repo.getDelegateRepository()).isInstanceOf(FileSystemResourceRepository.class);
    File dir =
        ((FileSystemResourceRepository) repo.getDelegateRepository()).getRepositoryDirectory();
    assertThat(dir).isEqualTo(new File("/tmp"));
  }

  @Test
  public void testGithubRepositoryWithoutToken() throws IOException {
    OmniResourceRepository repo = new OmniResourceRepository("github.com:joe/johnson");
    GithubResourceRepository githubRepo = verifyNames(repo, "joe", "johnson");
    assertThat(githubRepo.getAccessToken()).isEmpty();
  }

  @Test
  public void testGithubRepositoryWithToken() throws IOException {
    OmniResourceRepository repo = new OmniResourceRepository("github.com:joe/johnson@abc123");
    GithubResourceRepository githubRepo = verifyNames(repo, "joe", "johnson");
    assertThat(githubRepo.getAccessToken()).isEqualTo("abc123");
  }
}
