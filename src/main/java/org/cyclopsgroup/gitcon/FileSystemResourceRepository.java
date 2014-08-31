package org.cyclopsgroup.gitcon;

import java.io.File;

import org.apache.commons.lang.SystemUtils;

/**
 * Implementation of {@link ResourceRepository} based on a local git repository
 */
public class FileSystemResourceRepository
    implements ResourceRepository
{
    private final File workingDirectory;

    public FileSystemResourceRepository( File directory )
    {
        this.workingDirectory = directory;
    }

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
