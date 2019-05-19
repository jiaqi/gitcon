package org.cyclopsgroup.gitcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public abstract class Resource {
  public interface CheckedStreamConsumer {
    void consume(InputStream in) throws IOException;
  }

  private static class FileResource extends Resource {
    private final File file;

    private FileResource(File file) {
      Validate.notNull(file, "File can't be NULL");
      this.file = file;
    }

    @Override
    public void read(CheckedStreamConsumer consumer) throws IOException {
      try (InputStream in = new FileInputStream(file)) {
        consumer.consume(in);
      }
    }

    @Override
    public Resource reference(String relativePath) {
      String parentPath = file.getParentFile().getAbsolutePath();
      if (!parentPath.endsWith(SystemUtils.FILE_SEPARATOR)) {
        parentPath += SystemUtils.FILE_SEPARATOR;
      }
      return new FileResource(new File(parentPath + relativePath));
    }

    @Override
    public String toString() {
      return "file://" + file;
    }
  }

  public static Resource fromFile(File file) {
    return new FileResource(file);
  }

  public abstract void read(CheckedStreamConsumer consumer) throws IOException;

  public Properties readAsProperties() throws IOException {
    Properties source = new Properties();
    read(source::load);
    String includeProperty = source.getProperty("include", null);
    source.remove("include");
    if (StringUtils.isBlank(includeProperty)) {
      return source;
    }

    String[] includes = StringUtils.split(includeProperty, ',');
    Properties result = new Properties();
    for (String include : includes) {
      Properties props = reference(include).readAsProperties();
      result.putAll(props);
    }
    result.putAll(source);
    return result;
  }

  public abstract Resource reference(String relativePath);
}
