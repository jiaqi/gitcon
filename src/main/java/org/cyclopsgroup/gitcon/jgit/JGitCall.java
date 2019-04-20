package org.cyclopsgroup.gitcon.jgit;

import org.eclipse.jgit.api.errors.GitAPIException;

interface JGitCall<T> {
  T call() throws GitAPIException;
}
