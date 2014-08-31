package org.cyclopsgroup.gitcon.jgit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.gitcon.Source;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Implementation of {@link Source} based on a local git repository
 */
public class JGitSource
    implements Source
{
    private static final Log LOG = LogFactory.getLog( JGitSource.class );

    private volatile String branchOrCommit;

    private Boolean buildInSshIdentityUsed;

    private CredentialsProvider credentialsProvider;

    private JGitCallExecutor executor =
        JGitCallExecutor.synchronize( JGitCallExecutor.direct() );

    private Git git;

    private final String repoUri;

    /**
     * Constructor that uses a working directory under system temporary
     * directory
     *
     * @param repoUri Git repository URI
     */
    public JGitSource( String repoUri )
    {
        Validate.notNull( repoUri, "Git repository URI can not be NULL" );
        this.repoUri = repoUri;
    }

    public String getBranchOrCommmit()
    {
        return branchOrCommit;
    }

    public void initWorkingDirectory( File workingDirectory )
        throws GitAPIException, IOException
    {
        // Create git repo local directory
        File sourceDirectory =
            new File( workingDirectory.getAbsolutePath()
                + SystemUtils.FILE_SEPARATOR + "gitrepo" );
        if ( sourceDirectory.mkdirs() )
        {
            LOG.info( "Created GIT repo directory " + sourceDirectory );
        }

        // Create local file for build-in SSH private key
        if ( buildInSshIdentityUsed == Boolean.TRUE )
        {
            File sshKey =
                new File( workingDirectory.getAbsolutePath()
                    + SystemUtils.FILE_SEPARATOR + "gitconreader-ssh.key" );
            FileUtils.copyURLToFile( getClass().getClassLoader().getResource( "META-INF/gitcon/gitconreader-ssh.key" ),
                                     sshKey );
            LOG.info( "Build-in SSH private key is copied into " + sshKey );
            this.executor =
                JGitCallExecutor.withSshPrivateKey( sshKey.getAbsolutePath() );
            LOG.info( "JGit executor is set with build-in SSH key " + sshKey );
        }

        // Clone the repo
        final CloneCommand clone =
            Git.cloneRepository().setDirectory( sourceDirectory ).setURI( repoUri );
        if ( credentialsProvider != null )
        {
            clone.setCredentialsProvider( credentialsProvider );

        }
        LOG.info( "Running git clone " + repoUri + " against "
            + sourceDirectory );

        git = executor.invokeCall( new JGitCall<Git>()
        {
            @Override
            public Git call()
                throws GitAPIException
            {
                return clone.call();
            }
        } );

        // If branch or commit is specified, call git checkout
        if ( branchOrCommit != null )
        {
            LOG.info( "Calling git checkout " + branchOrCommit + " ..." );
            Ref result = executor.invokeCall( new JGitCall<Ref>()
            {
                public Ref call()
                    throws GitAPIException
                {
                    return git.checkout().setName( branchOrCommit ).call();
                }
            } );
            LOG.info( "Git checkout returned " + result );
        }
    }

    public void setBranchOrCommit( String branch )
    {
        this.branchOrCommit = branch;
    }

    public void setBuildInSshIdentityUsed( boolean buildInSshIdentityUsed )
    {
        this.buildInSshIdentityUsed = buildInSshIdentityUsed;
    }

    public void setSshIdentity( String privateKeyPath )
    {
        if ( buildInSshIdentityUsed == Boolean.TRUE )
        {
            throw new IllegalStateException(
                                             "Build-in identity is used, so no other identity is required." );
        }
        this.executor = JGitCallExecutor.withSshPrivateKey( privateKeyPath );
        LOG.info( "JGit executor is set with SSH private key " + privateKeyPath );
    }

    public void setSshIdentityFile( File privateKeyFile )
    {
        setSshIdentity( privateKeyFile.getAbsolutePath() );
    }

    public void setUserPassword( String userAndPassword )
    {
        int position = userAndPassword.indexOf( ':' );
        Validate.isTrue( position > 0
                             && position < userAndPassword.length() - 1,
                         "Input must be <username>:<password>, but it is "
                             + userAndPassword );
        setUserPassword( userAndPassword.substring( 0, position ),
                         userAndPassword.substring( position + 1 ) );
    }

    public void setUserPassword( String user, String password )
    {
        Validate.notNull( user, "User name must be supplied" );
        Validate.notNull( password, "Password must be supplied" );

        if ( credentialsProvider != null )
        {
            throw new IllegalStateException(
                                             "Credentials provider can only be set for once. It's already "
                                                 + credentialsProvider );
        }

        this.credentialsProvider =
            new UsernamePasswordCredentialsProvider( user, password );
        LOG.info( "Credentials provider is set to user/password instance" );
    }

    /**
     * Update local repository by running a <code>git pull</code> command
     *
     * @throws GitAPIException Allows JGit exceptions
     */
    public void updateWorkingDirectory( File workingDirectory )
        throws GitAPIException
    {
        LOG.info( "Running a git pull command ... " );
        PullResult result = executor.invokeCall( new JGitCall<PullResult>()
        {
            public PullResult call()
                throws GitAPIException
            {
                return git.pull().call();
            }
        } );
        LOG.info( "Pull command returned " + result );
    }
}
