package org.cyclopsgroup.gitcon;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;

public class PropertyLoadingUtils {
  static Properties subset(Properties source, String prefix) {
    Properties props = new Properties();
    for (Object keyObject : source.keySet()) {
      String key = (String) keyObject;
      if (!key.startsWith(prefix + ".")) {
        continue;
      }
      String shortKey = StringUtils.removeStart(key, prefix + ".");
      props.setProperty(shortKey, source.getProperty(key));
    }
    return props;
  }

  public static Properties loadWithInclusion(Resource resource) throws IOException {
    Properties source = new Properties();
    resource.read(source::load);
    String includeProperty = source.getProperty("include", null);
    source.remove("include");
    if (StringUtils.isBlank(includeProperty)) {
      return source;
    }

    String[] includes = StringUtils.split(includeProperty, ',');
    Properties result = new Properties();
    for (String include : includes) {
      Properties props = loadWithInclusion(resource.reference(include));
      result.putAll(props);
    }
    result.putAll(source);
    return result;
  }
}
