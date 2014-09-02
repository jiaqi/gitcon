package org.cyclopsgroup.gitcon;

import java.io.File;

import org.apache.commons.lang.SystemUtils;

/**
 * A simple implementation of {@link ResourceRepository} based on a local file
 * system directory.
 */
public class FileSystemResourceRepository
    implements ResourceRepository
{
    private final File workingDirectory;

    /**
     * @param directory The root of local file system where configuration files
     *            are stored
     */
    public FileSystemResourceRepository( File directory )
    {
        this.workingDirectory = directory;
    }

    /**
     * @return The root of local file system where configuration files are
     *         stored
     */
    public final File getWorkingDirectory()
    {
        return workingDirectory;
    }

    /**
     * @inheritDoc
     */
    @Override
    public File getResource( String filePath )
    {
        return new File( workingDirectory + SystemUtils.FILE_SEPARATOR
            + filePath );
    }
}
