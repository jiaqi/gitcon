package org.cyclopsgroup.gitcon;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.cyclopsgroup.kaufman.LocateableResource;
import org.cyclopsgroup.kaufman.aws.ExpressionUtils;

/**
 * A simple implementation that points to a local directory to get resources
 */
public class FileSystemResourceRepository
    implements LocalResourceRepository
{
    private File rootDirectory;

    public FileSystemResourceRepository( File rootDirectory )
    {
        Validate.isTrue( rootDirectory.isDirectory(), "Root directory "
            + rootDirectory + " is not a directory" );
        this.rootDirectory = rootDirectory;
    }

    public FileSystemResourceRepository( String rootDirectory )
    {
        this( new File( ExpressionUtils.populate( rootDirectory ) ) );
    }

    /**
     * @inheritDoc
     */
    @Override
    public File getRepositoryDirectory()
    {
        return rootDirectory;
    }

    /**
     * @inheritDoc
     */
    @Override
    public LocateableResource getResource( String filePath )
    {
        return LocateableResource.fromFile( new File(
                                                      rootDirectory.getAbsolutePath()
                                                          + SystemUtils.FILE_SEPARATOR
                                                          + filePath ) );
    }
}
