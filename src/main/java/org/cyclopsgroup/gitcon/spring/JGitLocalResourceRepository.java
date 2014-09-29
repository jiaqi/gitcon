package org.cyclopsgroup.gitcon.spring;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.cyclopsgroup.gitcon.LocalResourceRepository;
import org.cyclopsgroup.gitcon.StaticLocalResourceRepository;
import org.cyclopsgroup.gitcon.jgit.JGitSource;
import org.cyclopsgroup.kaufman.LocateableResource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A convenient class that combines {@link JGitSource} and
 * {@link StaticLocalResourceRepository}
 */
public class JGitLocalResourceRepository
    extends JGitSource
    implements Closeable, LocalResourceRepository, InitializingBean,
    DisposableBean
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

    /**
     * @inheritDoc
     */
    @Override
    public void afterPropertiesSet()
        throws Exception
    {
        localRepo.init();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void close()
        throws IOException
    {
        localRepo.close();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void destroy()
        throws IOException
    {
        localRepo.close();
    }

    /**
     * @inheritDoc
     */
    @Override
    public File getRepositoryDirectory()
    {
        return localRepo.getRepositoryDirectory();
    }

    /**
     * @inheritDoc
     */
    @Override
    public LocateableResource getResource( String filePath )
    {
        return localRepo.getResource( filePath );
    }
}
