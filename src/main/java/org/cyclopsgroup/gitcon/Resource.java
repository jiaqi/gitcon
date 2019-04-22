package org.cyclopsgroup.gitcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;

public abstract class Resource {
  private static class FileResource extends Resource {
    private final File file;

    private FileResource(File file) {
      Validate.notNull(file, "File can't be NULL");
      this.file = file;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Resource reference(String relativePath) {
      String parentPath = file.getParentFile().getAbsolutePath();
      if (!parentPath.endsWith(SystemUtils.FILE_SEPARATOR)) {
        parentPath += SystemUtils.FILE_SEPARATOR;
      }
      return new FileResource(new File(parentPath + relativePath));
    }

    /**
     * @inheritDoc
     */
    @Override
    public InputStream openToRead() throws IOException {
      return new FileInputStream(file);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String toString() {
      return "file://" + file;
    }
  }

  public static Resource fromFile(File file) {
    return new FileResource(file);
  }

  public abstract Resource reference(String relativePath);

  public abstract InputStream openToRead() throws IOException;
}
