package org.cyclopsgroup.gitcon.jgit;

import java.io.File;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Class that invokes {@link GitCall} with special logic around invocations
 */
abstract class JGitCallExecutor
{
    private static class Direct
        extends JGitCallExecutor
    {
        @Override
        <T> T invokeCall( JGitCall<T> call )
            throws GitAPIException
        {
            return call.call();
        }
    }

    private static class KeySessionFactory
        extends JschConfigSessionFactory
    {
        private final String privateKeyPath;

        private KeySessionFactory( String keyPath )
        {
            this.privateKeyPath = keyPath;
        }

        @Override
        protected void configure( OpenSshConfig.Host host, Session session )
        {
            session.setConfig( "StrictHostKeyChecking", "no" );
        }

        @Override
        protected JSch getJSch( final OpenSshConfig.Host hostConfig, FS fs )
            throws JSchException
        {
            JSch jsch = super.getJSch( hostConfig, fs );
            jsch.removeAllIdentity();
            jsch.addIdentity( privateKeyPath );
            return jsch;
        }
    }

    private static class SshContext
        extends JGitCallExecutor
    {
        private final KeySessionFactory context;

        private SshContext( KeySessionFactory context )
        {
            this.context = context;
        }

        @Override
        <T> T invokeCall( JGitCall<T> call )
            throws GitAPIException
        {
            if ( !new File( context.privateKeyPath ).canRead() )
            {
                throw new IllegalArgumentException( "Private key "
                    + context.privateKeyPath + " is not accessible" );
            }
            synchronized ( JGitCallExecutor.class )
            {
                SshSessionFactory original = SshSessionFactory.getInstance();
                SshSessionFactory.setInstance( context );
                try
                {
                    return call.call();
                }
                finally
                {
                    SshSessionFactory.setInstance( original );
                }
            }
        }
    }

    private static class Synchronized
        extends JGitCallExecutor
    {
        private final JGitCallExecutor executor;

        private Synchronized( JGitCallExecutor executor )
        {
            this.executor = executor;
        }

        @Override
        <T> T invokeCall( JGitCall<T> call )
            throws GitAPIException
        {
            synchronized ( JGitCallExecutor.class )
            {
                return executor.invokeCall( call );
            }
        }
    }

    private static final JGitCallExecutor DIRECT_INSTANCE = new Direct();

    /**
     * @return A no-op implementation that invokes calls directly
     */
    static JGitCallExecutor direct()
    {
        return DIRECT_INSTANCE;
    }

    /**
     * @param executor Given executor
     * @return A wrapper of given executor that makes sure invocation is
     *         synchronized globally
     */
    static JGitCallExecutor synchronize( JGitCallExecutor executor )
    {
        return new Synchronized( executor );
    }

    /**
     * Because JGit relies on JSch to handle SSH transport, which binds SSH key
     * via static instance, there's no elegant way to allow multiple SSH keys in
     * the same JVM. This decorator of executor binds given SSH private key with
     * a global lock before each invocation, and restore to previously used SSH
     * key after invocation.
     *
     * @param sshPrivateKey Path to SSH private key
     * @return Executor that knows to setup Jsch to use given SSH private key
     *         before each call, and retore it after the invocation.
     */
    static JGitCallExecutor withSshPrivateKey( String sshPrivateKey )
    {
        return new SshContext( new KeySessionFactory( sshPrivateKey ) );
    }

    /**
     * Invoke given call
     *
     * @param call The call to invoke
     * @return Some result
     * @throws GitAPIException Allows JGit exceptions
     */
    abstract <T> T invokeCall( JGitCall<T> call )
        throws GitAPIException;
}
