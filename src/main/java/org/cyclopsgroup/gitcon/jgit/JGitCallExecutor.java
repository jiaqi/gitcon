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

    static JGitCallExecutor direct()
    {
        return DIRECT_INSTANCE;
    }

    static JGitCallExecutor synchronize( JGitCallExecutor executor )
    {
        return new Synchronized( executor );
    }

    static JGitCallExecutor withSshPrivateKey( String sshPrivateKey )
    {
        return new SshContext( new KeySessionFactory( sshPrivateKey ) );
    }

    abstract <T> T invokeCall( JGitCall<T> call )
        throws GitAPIException;
}
