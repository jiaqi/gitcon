package org.cyclopsgroup.gitcon.jgit;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyclopsgroup.gitcon.FileSystemSource;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Implementation of {@link FileSystemSource} that gets file from a Git repository. There are
 * several ways of authenticating access to git.
 *
 * <ol>
 *   <li>Username and password is supported, but it's the least recommended approach. User can call
 *       {@link JGitSource#setUserPassword(String, String)} or {@link
 *       JGitSource#setUserPassword(String)} to pass username and password to the instance.
 *   <li>If the OS default SSH for current run-as-user is good to access Git repo, nothing needs to
 *       be configured and default setting will pick up system SSH key in {@literal
 *       $user.home/.ssh/}.
 *   <li>Specify an SSH private key by calling {@link JGitSource#setSshIdentity(String)} or {@link
 *       JGitSource#setSshIdentityFile(File)}
 *   <li>Gitcon library comes with a build-in SSH private key for user {@literal gitconreader} in
 *       both Github and BitBucket. Calling {@link JGitSource#setBuildInSshIdentityUsed(boolean)}
 *       with {@literal true} or {@link JGitSource#setSshIdentity(String)} with {@literal buildin}
 *       tells the class to use the build-in private key. Beaware that since everyone can get this
 *       build-in private key from Gitcon jar file, exposing your Git repo to user {@literal
 *       gitconreader} is equivalent to exposing it to public.
 * </ol>
 */
public class JGitSource implements FileSystemSource {
  private static final Log LOG = LogFactory.getLog(JGitSource.class);

  private volatile String branchOrCommit;

  private String sshIdentity;

  private Boolean buildInSshIdentityUsed;

  private CredentialsProvider credentialsProvider;

  private JGitCallExecutor executor;

  private Git git;

  private final String repoUri;

  /**
   * Constructor that uses a working directory under system temporary directory
   *
   * @param repoUri Git repository URI
   */
  public JGitSource(String repoUri) {
    Validate.notNull(repoUri, "Git repository URI can not be NULL");
    this.repoUri = repoUri;
  }

  public String getBranchOrCommmit() {
    return branchOrCommit;
  }

  @Override
  public File initWorkingDirectory(File workingDirectory) throws GitAPIException, IOException {
    // Create git repo local directory
    File sourceDirectory =
        new File(workingDirectory.getAbsolutePath() + SystemUtils.FILE_SEPARATOR + "gitrepo");
    if (sourceDirectory.mkdirs()) {
      LOG.info("Created GIT repo directory " + sourceDirectory);
    }

    // Create local file for build-in SSH private key
    File sshKey = null;
    if (buildInSshIdentityUsed == Boolean.TRUE
        || StringUtils.equalsIgnoreCase(sshIdentity, "buildin")) {
      sshKey =
          new File(
              workingDirectory.getAbsolutePath()
                  + SystemUtils.FILE_SEPARATOR
                  + "gitconreader-ssh.key");
      FileUtils.copyURLToFile(
          getClass().getClassLoader().getResource("META-INF/gitcon/gitconreader-ssh.key"), sshKey);
      LOG.info("Build-in SSH private key is copied into " + sshKey);

    } else if (sshIdentity != null && !sshIdentity.equalsIgnoreCase("default")) {
      sshKey = new File(sshIdentity);
      LOG.info("About to use specified SSH key " + sshIdentity);
    } else {
      LOG.info("Default system SSH key will apply");
    }

    if (sshKey == null) {
      executor = JGitCallExecutor.synchronize(JGitCallExecutor.direct());
    } else if (!sshKey.canRead()) {
      throw new IllegalStateException(
          "Configured SSH private key " + sshKey + " is not accessible");
    } else {
      executor = JGitCallExecutor.withSshPrivateKey(sshKey.getAbsolutePath());
      LOG.info("JGit executor is set with build-in SSH key " + sshKey);
    }

    // Clone the repo
    final CloneCommand clone = Git.cloneRepository().setDirectory(sourceDirectory).setURI(repoUri);
    if (credentialsProvider != null) {
      clone.setCredentialsProvider(credentialsProvider);
    }
    LOG.info("Running git clone " + repoUri + " against " + sourceDirectory);
    git = executor.invokeCall(clone::call);

    // If branch or commit is specified, call git checkout
    if (branchOrCommit != null) {
      LOG.info("Calling git checkout " + branchOrCommit + " ...");
      Ref result = executor.invokeCall(() -> git.checkout().setName(branchOrCommit).call());
      LOG.info("Git checkout returned " + result);
    }
    return sourceDirectory;
  }

  /**
   * Caller sets branchOrCommit in order to checkout files from a non-default branch or specified
   * commit. When specified, a {@literal git checkout} command will be called right after {@literal
   * git clone} when application starts.
   *
   * @param branch Branch or commit in Git repository
   */
  public void setBranchOrCommit(String branch) {
    this.branchOrCommit = branch;
  }

  /**
   * Gitcon jar file comes with a build-in SSH private key for user {@literal gitconreader} in both
   * Github and BitBucket. This method tells {@link JGitSource} to use the build-in SSH key.
   *
   * @param buildInSshIdentityUsed True to use the build-in SSH priavate key
   */
  public void setBuildInSshIdentityUsed(boolean buildInSshIdentityUsed) {
    this.buildInSshIdentityUsed = buildInSshIdentityUsed;
  }

  /**
   * Use specified SSH private key for authentication
   *
   * @param privateKeyPath Path to SSH private key. However if value is {@literal default}, the
   *     default OS SSH key will be used. If value is {@literal buildin}, a build-in SSH private
   *     key, which is registered for user {@literal gitconreader} in Github and BitBucket will be
   *     used.
   * @see #setBuildInSshIdentityUsed(boolean)
   */
  public void setSshIdentity(String privateKeyPath) {
    this.sshIdentity = privateKeyPath;
  }

  /**
   * An alternative of {@link #setSshIdentity(String)} that takes a {@link File} instead of file
   * path.
   *
   * @param privateKeyFile SSH private key file object
   */
  public void setSshIdentityFile(File privateKeyFile) {
    setSshIdentity(privateKeyFile == null ? null : privateKeyFile.getAbsolutePath());
  }

  /**
   * An alternative method of {@link #setUserPassword(String, String)} which takes parameters from
   * one single string
   *
   * @param userAndPassword One string in form of {@literal <user>:<password>}
   */
  public void setUserPassword(String userAndPassword) {
    int position = userAndPassword.indexOf(':');
    Validate.isTrue(
        position > 0 && position < userAndPassword.length() - 1,
        "Input must be <username>:<password>, but it is " + userAndPassword);
    setUserPassword(
        userAndPassword.substring(0, position), userAndPassword.substring(position + 1));
  }

  /**
   * Set user and password used to authenticate access to Git repository. It's often used when Git
   * repo URI starts with {@literal https} where basic authentication is used.
   *
   * @param user Login user name
   * @param password Login password
   */
  public void setUserPassword(String user, String password) {
    Validate.notNull(user, "User name must be supplied");
    Validate.notNull(password, "Password must be supplied");

    if (credentialsProvider != null) {
      throw new IllegalStateException(
          "Credentials provider can only be set for once. It's already " + credentialsProvider);
    }

    this.credentialsProvider = new UsernamePasswordCredentialsProvider(user, password);
    LOG.info("Credentials provider is set to user/password instance");
  }

  /**
   * Update local repository by running a {@literal git pull} command
   *
   * @throws GitAPIException Allows JGit exceptions
   */
  @Override
  public void updateWorkingDirectory(File workingDirectory) throws GitAPIException {
    LOG.info("Running a git pull command ... ");
    PullResult result = executor.invokeCall(() -> git.pull().call());
    LOG.info("Pull command returned " + result);
  }
}
