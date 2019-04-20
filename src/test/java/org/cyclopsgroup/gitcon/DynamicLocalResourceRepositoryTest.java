package org.cyclopsgroup.gitcon;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DynamicLocalResourceRepositoryTest {
  private Mockery mock;

  private FileSystemSource source;

  private File workingDirectory;

  private DynamicLocalResourceRepository repo;

  @Before
  public void setUpMocks() {
    mock = new Mockery();
    source = mock.mock(FileSystemSource.class);
    workingDirectory = DynamicLocalResourceRepository.createTempDirectory();
    repo = new DynamicLocalResourceRepository(workingDirectory, source);
  }

  @After
  public void assertMocks() throws IOException {
    repo.close();
    mock.assertIsSatisfied();
  }

  @Test
  public void testInitAndUpdate() throws Exception {
    repo.setUpdateIntervalSeconds(5);
    mock.checking(
        new Expectations() {
          {
            oneOf(source).initWorkingDirectory(workingDirectory);

            atLeast(1).of(source).updateWorkingDirectory(workingDirectory);
          }
        });
    repo.init();
    Thread.sleep(8000L);
  }
}
