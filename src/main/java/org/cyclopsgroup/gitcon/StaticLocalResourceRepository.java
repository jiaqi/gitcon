package org.cyclopsgroup.gitcon;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.kaufman.LocateableResource;

/**
 * Implementation of {@link ResourceRepository} based on a local file system root. It relies on an
 * implementation of {@link FileSystemSource} to get files from remote into the file system root.
 */
public class StaticLocalResourceRepository implements Closeable, LocalResourceRepository {
  private static final Log LOG = LogFactory.getLog(StaticLocalResourceRepository.class);

  /**
   * Create a temporary directory under system temdir
   *
   * @return A temporary directory, which does not exist yet
   */
  public static File createTempDirectory() {
    return new File(
        SystemUtils.JAVA_IO_TMPDIR
            + "/"
            + StaticLocalResourceRepository.class.getSimpleName()
            + "-"
            + RandomStringUtils.randomAlphabetic(8)
            + "-working-dir");
  }

  private final FileSystemSource source;

  private File sourceDirectory;

  private final File workingDirectory;

  /**
   * Constructor with given working directory and file source and a given working directory. Working
   * directory will be wiped out in {@link #close()}
   *
   * @param directory Given working directory
   * @param source Source of files
   * @see #StaticLocalResourceRepository(FileSystemSource)
   */
  public StaticLocalResourceRepository(File directory, FileSystemSource source) {
    this.workingDirectory = directory;
    this.source = source;
  }

  /**
   * Constructor with a source that is responsible for getting files into the working directory. The
   * constructor will create a working directory using {@link #createTempDirectory()} method and
   * wipe out the directory in {@link #close()} call.
   *
   * @param source A source that gets files
   */
  public StaticLocalResourceRepository(FileSystemSource source) {
    this(createTempDirectory(), source);
  }

  /** @inheirtDoc */
  @Override
  public void close() throws IOException {
    wipeWorkingDir();
  }

  @Override
  public File getRepositoryDirectory() {
    return sourceDirectory;
  }

  @Override
  public LocateableResource getResource(String filePath) {
    return LocateableResource.fromFile(
        new File(sourceDirectory + SystemUtils.FILE_SEPARATOR + filePath));
  }

  /** @return The file source */
  public final FileSystemSource getSource() {
    return source;
  }

  /** @return The actual working directory, the root of local repository */
  public final File getWorkingDirectory() {
    return workingDirectory;
  }

  /**
   * This method should be called before instance can be used. In the call, the file source gets
   * files from remote and save them into a directory under {@link #workingDirectory}. It does it by
   * calling {@link FileSystemSource#initWorkingDirectory(File)}
   *
   * @throws Exception Allows any type of exception
   */
  public void init() throws Exception {
    wipeWorkingDir();
    if (workingDirectory.mkdirs()) {
      LOG.info("Made local temporary directory " + workingDirectory);
    }

    sourceDirectory = source.initWorkingDirectory(workingDirectory);

    File[] dirs = workingDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
    if (dirs.length == 0) {
      LOG.warn("Nothing is found in directory " + workingDirectory);
    }
  }

  private void wipeWorkingDir() throws IOException {
    if (workingDirectory.isDirectory()) {
      LOG.info("Clean up local directory " + workingDirectory);
      FileUtils.deleteDirectory(workingDirectory);
    }
  }
}
