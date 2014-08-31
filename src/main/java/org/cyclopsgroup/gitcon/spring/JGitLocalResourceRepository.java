package org.cyclopsgroup.gitcon.spring;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.cyclopsgroup.gitcon.ResourceRepository;
import org.cyclopsgroup.gitcon.StaticLocalResourceRepository;
import org.cyclopsgroup.gitcon.jgit.JGitSource;

/**
 * A convenient class that combines {@link JGitSource} and
 * {@link StaticLocalResourceRepository}
 */
public class JGitLocalResourceRepository
    extends JGitSource
    implements Closeable, ResourceRepository
{
    private final StaticLocalResourceRepository localRepo;

    public JGitLocalResourceRepository( String repoUri )
    {
        super( repoUri );
        this.localRepo = new StaticLocalResourceRepository( this );
    }

    public JGitLocalResourceRepository( String repoUri, File workingDirectory )
    {
        super( repoUri );
        this.localRepo =
            new StaticLocalResourceRepository( workingDirectory, this );
    }

    @Override
    public void close()
        throws IOException
    {
        localRepo.close();
    }

    @Override
    public Reader openToRead( String filePath )
        throws IOException
    {
        return localRepo.openToRead( filePath );
    }
}
