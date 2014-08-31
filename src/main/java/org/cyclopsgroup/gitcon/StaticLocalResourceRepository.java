package org.cyclopsgroup.gitcon;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of {@link ResourceRepository} based on a local git repository
 */
public class StaticLocalResourceRepository
    implements Closeable, ResourceRepository
{
    private static final Log LOG =
        LogFactory.getLog( StaticLocalResourceRepository.class );

    public static File createTempDirectory()
    {
        return new File( SystemUtils.JAVA_IO_TMPDIR + "/"
            + StaticLocalResourceRepository.class.getSimpleName() + "-"
            + RandomStringUtils.randomAlphabetic( 8 ) + "-working-dir" );
    }

    private final File workingDirectory;

    private File sourceDirectory;

    private final Source source;

    public StaticLocalResourceRepository( Source source )
    {
        this( createTempDirectory(), source );

    }

    public StaticLocalResourceRepository( File directory, Source source )
    {
        this.workingDirectory = directory;
        this.source = source;
    }

    /**
     * @inheirtDoc
     */
    @Override
    public void close()
        throws IOException
    {
        wipeWorkingDir();
    }

    public final File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public final Source getSource()
    {
        return source;
    }

    public void init()
        throws Exception
    {
        wipeWorkingDir();
        if ( workingDirectory.mkdirs() )
        {
            LOG.info( "Made local temporary directory " + workingDirectory );
        }

        sourceDirectory = source.initWorkingDirectory( workingDirectory );

        File[] dirs =
            workingDirectory.listFiles( (FileFilter) FileFilterUtils.directoryFileFilter() );
        if ( dirs.length == 0 )
        {
            LOG.warn( "Nothing is found in directory " + workingDirectory );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Reader openToRead( String filePath )
        throws IOException
    {
        return new FileReader( new File( sourceDirectory
            + SystemUtils.FILE_SEPARATOR + filePath ) );
    }

    private void wipeWorkingDir()
        throws IOException
    {
        if ( workingDirectory.isDirectory() )
        {
            LOG.info( "Clean up local directory " + workingDirectory );
            FileUtils.deleteDirectory( workingDirectory );
        }
    }
}
