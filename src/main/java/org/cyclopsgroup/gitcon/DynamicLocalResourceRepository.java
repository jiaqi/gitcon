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

public class DynamicLocalResourceRepository
    extends StaticLocalResourceRepository
{
    private static final int DEFAULT_UPDATE_INTERVAL_SECONDS = 300;

    private static final Log LOG =
        LogFactory.getLog( DynamicLocalResourceRepository.class );

    private volatile boolean closing = false;

    private final Random random = new Random();

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    private volatile int updateIntervalSeconds =
        DEFAULT_UPDATE_INTERVAL_SECONDS;

    private final Runnable updateTask = new Runnable()
    {
        public void run()
        {
            try
            {
                getSource().updateWorkingDirectory( getWorkingDirectory() );
            }
            catch ( Throwable e )
            {
                LOG.error( "Update operation failed: " + e.getMessage(), e );
            }
            finally
            {
                if ( !closing )
                {
                    scheduleNextCheck();
                }
            }
        }
    };

    protected DynamicLocalResourceRepository( File workingDir,
                                            Source source )
    {
        super( workingDir, source );
    }

    protected DynamicLocalResourceRepository( Source source )
    {
        super( source );
    }

    /**
     * @inheirtDoc
     */
    @Override
    public void close()
        throws IOException
    {
        closing = true;
        List<Runnable> abandoned = scheduler.shutdownNow();
        LOG.info( "These tasks are abandoned as repository is closed: "
            + abandoned );

        super.close();
    }

    public int getUpdateIntervalSeconds()
    {
        return updateIntervalSeconds;
    }

    public void init()
        throws Exception
    {
        super.init();
        scheduleNextCheck();
    }

    private void scheduleNextCheck()
    {
        int delay =
            Math.min( 5,
                      updateIntervalSeconds / 2
                          + random.nextInt( updateIntervalSeconds ) );
        scheduler.schedule( updateTask, delay, TimeUnit.SECONDS );
        LOG.info( "Next check is scheduled after " + delay + " seconds" );
    }

    public void setUpdateIntervalSeconds( int updateIntervalSeconds )
    {
        Validate.isTrue( updateIntervalSeconds > 0, "Invalid minutes"
            + updateIntervalSeconds );
        this.updateIntervalSeconds = updateIntervalSeconds;
    }
}
