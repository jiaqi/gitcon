package org.cyclopsgroup.gitcon.jgit;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JGitSourceTest {
  private File workingDirectory;

  @Before
  public void setUpDirectory() {
    workingDirectory =
        new File(
            SystemUtils.JAVA_IO_TMPDIR
                + SystemUtils.FILE_SEPARATOR
                + getClass().getSimpleName()
                + "-"
                + RandomStringUtils.randomAlphabetic(8));
    workingDirectory.mkdirs();
  }

  @After
  public void tearDownDirectory() throws IOException {
    FileUtils.deleteDirectory(workingDirectory);
  }

  @Test
  @Ignore("Changing to key-based authentication")
  public void testWithSpecifiedSshKey() throws GitAPIException, IOException {
    JGitSource source = new JGitSource("git@bitbucket.org:jiaqi/gitcon-demo-config.git");
    source.setSshIdentity("src/main/resources/META-INF/gitcon/gitconreader-ssh.key");
    source.initWorkingDirectory(workingDirectory);
  }

  @Test
  @Ignore("Changing to key-based authentication")
  public void testWithBuildInSshKey() throws GitAPIException, IOException {
    JGitSource source = new JGitSource("git@bitbucket.org:jiaqi/gitcon-demo-config.git");
    source.setBuildInSshIdentityUsed(true);
    source.initWorkingDirectory(workingDirectory);
  }
}
