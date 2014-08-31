package org.cyclopsgroup.gitcon.spring;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.cyclopsgroup.gitcon.ResourceRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * A spring {@link BeanFactory} that creates {@link Properties} based on a file
 * in given {@link ResourceRepository}
 */
public class GitconPropertiesBeanFactory
    implements FactoryBean<Properties>
{
    private final String filePath;

    private final ResourceRepository repo;

    /**
     * @param repo Source repository where properties file lives
     * @param filePath The path of properties file
     */
    public GitconPropertiesBeanFactory( ResourceRepository repo, String filePath )
    {
        this.repo = repo;
        this.filePath = filePath;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Properties getObject()
        throws IOException
    {
        Properties props = new Properties();
        Reader in = repo.openToRead( filePath );
        try
        {
            props.load( in );
            return props;
        }
        finally
        {
            IOUtils.closeQuietly( in );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class<Properties> getObjectType()
    {
        return Properties.class;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean isSingleton()
    {
        return true;
    }
}
