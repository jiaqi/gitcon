package org.cyclopsgroup.gitcon.github;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cyclopsgroup.gitcon.FileSystemResourceRepository;
import org.cyclopsgroup.gitcon.Resource;
import org.cyclopsgroup.gitcon.ResourceRepository;

public class OmniResourceRepository implements ResourceRepository {
  private static final Pattern FILE_PATTERN = Pattern.compile("^file:(.+)$");
  private static final Pattern GITHUB_PATTERN =
      Pattern.compile("^github\\.com:((\\w|-)+)/((\\w|-)+)(@(.+))?$");

  private static final ResourceRepository createDelegate(String path) throws IOException {
    Matcher m = matchWithGroups(path, FILE_PATTERN, 1);
    if (m.matches()) {
      return new FileSystemResourceRepository(new File(m.group(1)));
    }
    m = matchWithGroups(path, GITHUB_PATTERN, 6);
    if (m.matches()) {
      return new GithubResourceRepository(m.group(1), m.group(3), Strings.nullToEmpty(m.group(6)));
    }
    throw new IllegalArgumentException("Unrecognized repository path " + path);
  }

  private static Matcher matchWithGroups(String string, Pattern pattern, int groups) {
    Matcher m = pattern.matcher(string);
    if (m.matches() && m.groupCount() != groups) {
      throw new IllegalStateException(
          String.format(
              "Unexpected number of groups %s is found from input %s against pattern %s.",
              m.groupCount(), string, pattern));
    }
    return m;
  }

  private final ResourceRepository repository;

  public OmniResourceRepository(String repositoryPath) throws IOException {
    this.repository = createDelegate(repositoryPath);
  }

  @Override
  public Resource getResource(String filePath) {
    return repository.getResource(filePath);
  }

  ResourceRepository getDelegateRepository() {
    return repository;
  }
}
