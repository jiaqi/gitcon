package org.cyclopsgroup.gitcon;

import java.io.File;

public interface Source
{
    void initWorkingDirectory( File workingDirectory )
        throws Exception;

    void updateWorkingDirectory( File workingDirectory )
        throws Exception;
}
