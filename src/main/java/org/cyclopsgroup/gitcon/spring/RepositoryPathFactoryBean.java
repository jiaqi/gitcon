package org.cyclopsgroup.gitcon.spring;

import org.cyclopsgroup.gitcon.LocalResourceRepository;
import org.springframework.beans.factory.FactoryBean;

public class RepositoryPathFactoryBean
    implements FactoryBean<String>
{
    private final LocalResourceRepository repository;

    public RepositoryPathFactoryBean( LocalResourceRepository repository )
    {
        this.repository = repository;
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getObject()
    {
        return repository.getRepositoryDirectory().getAbsolutePath();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class<String> getObjectType()
    {
        return String.class;
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
