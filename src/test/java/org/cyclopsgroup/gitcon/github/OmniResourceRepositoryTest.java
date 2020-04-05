package org.cyclopsgroup.gitcon.github;

import static com.google.common.truth.Truth.assertThat;
import java.io.File;
import java.io.IOException;
import org.cyclopsgroup.gitcon.FileSystemResourceRepository;
import org.junit.Test;

public class OmniResourceRepositoryTest {
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
    assertThat(repo.getDelegateRepository()).isInstanceOf(GithubResourceRepository.class);
    GithubResourceRepository githubRepo = (GithubResourceRepository) repo.getDelegateRepository();
    assertThat(githubRepo.getGithubUser()).isEqualTo("joe");
    assertThat(githubRepo.getRepositoryName()).isEqualTo("johnson");
    assertThat(githubRepo.getAccessToken()).isEmpty();
  }

  @Test
  public void testGithubRepositoryWithToken() throws IOException {
    OmniResourceRepository repo = new OmniResourceRepository("github.com:joe/johnson@abc123");
    assertThat(repo.getDelegateRepository()).isInstanceOf(GithubResourceRepository.class);
    GithubResourceRepository githubRepo = (GithubResourceRepository) repo.getDelegateRepository();
    assertThat(githubRepo.getGithubUser()).isEqualTo("joe");
    assertThat(githubRepo.getRepositoryName()).isEqualTo("johnson");
    assertThat(githubRepo.getAccessToken()).isEqualTo("abc123");
  }
}
