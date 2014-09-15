package org.cyclopsgroup.gitcon.spring;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.cyclopsgroup.gitcon.ResourceRepository;
import org.cyclopsgroup.kaufman.LocateableResource;
import org.springframework.beans.factory.FactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GitconJacksonBeanFactory<T>
    implements FactoryBean<T>
{
    private final ResourceRepository repo;

    private final String path;

    private final Class<T> beanType;

    public GitconJacksonBeanFactory( ResourceRepository repo, String path,
                                     Class<T> beanType )
    {
        this.repo = repo;
        this.path = path;
        this.beanType = beanType;
    }

    /**
     * @inheritDoc
     */
    @Override
    public T getObject()
        throws IOException
    {
        LocateableResource resource = repo.getResource( path );
        InputStream in = resource.openToRead();
        try
        {
            return new ObjectMapper().readValue( in, beanType );
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
    public Class<T> getObjectType()
    {
        return beanType;
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
