package org.cyclopsgroup.gitcon;

import java.io.IOException;
import java.io.Reader;

public interface ResourceRepository
{
    Reader openToRead( String filePath )
        throws IOException;
}
