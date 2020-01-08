package org.cyclopsgroup.gitcon.spring;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.gitcon.ExpressionUtils;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.ResourceRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * A spring {@link BeanFactory} that creates {@link Properties} based on a file in given {@link
 * ResourceRepository}. The properties file is populated using {@link ExtendedProperties}, where
 * file inclusion and variable replacement are supported.
 */
public class GitconPropertiesBeanFactory implements FactoryBean<Properties> {
  private static final Log LOG = LogFactory.getLog(GitconPropertiesBeanFactory.class);

  private final String filePath;

  private final ResourceRepository repo;

  /**
   * @param repo Source repository where properties file lives
   * @param filePath The path of properties file
   */
  public GitconPropertiesBeanFactory(ResourceRepository repo, String filePath) {
    this.repo = repo;
    this.filePath = filePath;
  }

  @Override
  public Properties getObject() throws IOException {
    Resource resource = repo.getResource(ExpressionUtils.populate(filePath));
    LOG.info("Reading extended properties from file " + resource);
    return resource.readAsProperties();
  }

  @Override
  public Class<Properties> getObjectType() {
    return Properties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
