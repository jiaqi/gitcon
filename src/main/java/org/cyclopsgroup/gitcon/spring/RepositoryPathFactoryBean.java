package org.cyclopsgroup.gitcon.spring;

import org.cyclopsgroup.gitcon.LocalResourceRepository;
import org.springframework.beans.factory.FactoryBean;

public class RepositoryPathFactoryBean implements FactoryBean<String> {
  private final LocalResourceRepository repository;

  public RepositoryPathFactoryBean(LocalResourceRepository repository) {
    this.repository = repository;
  }

  @Override
  public String getObject() {
    return repository.getRepositoryDirectory().getAbsolutePath();
  }

  @Override
  public Class<String> getObjectType() {
    return String.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
