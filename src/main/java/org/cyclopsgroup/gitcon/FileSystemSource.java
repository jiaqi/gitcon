package org.cyclopsgroup.gitcon;

import java.io.File;

/**
 * A source that gets files from a logical repository into physical local file
 * system
 */
public interface FileSystemSource
{
    /**
     * Get files from logical source into local working directory when
     * application starts
     *
     * @param workingDirectory Local working directory, the root of local
     *            repository
     * @return The root of copied files from logical source under working
     *         directory. It's not necessarily the same working directory, since
     *         the implementation of {@link FileSystemSource} may choose to download
     *         remote files into a subdirectory under working directory, in
     *         which case the subdirectory is returned.
     * @throws Exception Allows any exception
     */
    File initWorkingDirectory( File workingDirectory )
        throws Exception;

    /**
     * Get modified files from logic source into local working directory. This
     * call gets remote incremental modifications and is expected to be called
     * repeatedly.
     *
     * @param workingDirectory Local working directory
     * @throws Exception Allows any exception
     */
    void updateWorkingDirectory( File workingDirectory )
        throws Exception;
}
