package org.cyclopsgroup.gitcon;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This implementation of {@link ResourceRepository} extends {@link StaticLocalResourceRepository}
 * by adding a recurring task that sync local files with remote {@link FileSystemSource} once a
 * while. It makes sure that when files in remote source are changed, the local copy gets the change
 * eventually.
 */
public class DynamicLocalResourceRepository extends StaticLocalResourceRepository {
  private static final int DEFAULT_UPDATE_INTERVAL_SECONDS = 300;

  private static final Log LOG = LogFactory.getLog(DynamicLocalResourceRepository.class);

  private volatile boolean closing = false;

  private final Random random = new Random();

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private volatile int updateIntervalSeconds = DEFAULT_UPDATE_INTERVAL_SECONDS;

  private final Runnable updateTask =
      new Runnable() {
        @Override
        public void run() {
          try {
            getSource().updateWorkingDirectory(getWorkingDirectory());
          } catch (Throwable e) {
            LOG.error("Update operation failed: " + e.getMessage(), e);
          } finally {
            if (!closing) {
              scheduleNextCheck();
            }
          }
        }
      };

  protected DynamicLocalResourceRepository(File workingDir, FileSystemSource source) {
    super(workingDir, source);
  }

  protected DynamicLocalResourceRepository(FileSystemSource source) {
    super(source);
  }

  @Override
  public void close() throws IOException {
    closing = true;
    List<Runnable> abandoned = scheduler.shutdownNow();
    LOG.info("These tasks are abandoned as repository is closed: " + abandoned);

    super.close();
  }

  /**
   * The default checking interval is {@value #DEFAULT_UPDATE_INTERVAL_SECONDS} seconds.
   *
   * @return Number of seconds between recurring check
   */
  public int getUpdateIntervalSeconds() {
    return updateIntervalSeconds;
  }

  @Override
  public void init() throws Exception {
    super.init();
    scheduleNextCheck();
  }

  private void scheduleNextCheck() {
    int delay = Math.min(5, updateIntervalSeconds / 2 + random.nextInt(updateIntervalSeconds));
    scheduler.schedule(updateTask, delay, TimeUnit.SECONDS);
    LOG.info("Next check is scheduled after " + delay + " seconds");
  }

  /**
   * Setting the checking interval. Modification of checking interval takes effect after the next
   * run dynamically.
   *
   * @param updateIntervalSeconds New value of checking interval in secons
   */
  public void setUpdateIntervalSeconds(int updateIntervalSeconds) {
    Validate.isTrue(updateIntervalSeconds > 0, "Invalid minutes" + updateIntervalSeconds);
    this.updateIntervalSeconds = updateIntervalSeconds;
  }
}
