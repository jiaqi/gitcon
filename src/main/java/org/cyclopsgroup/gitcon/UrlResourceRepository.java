package org.cyclopsgroup.gitcon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

public class UrlResourceRepository
    implements ResourceRepository
{
    private final String baseUrl;

    public UrlResourceRepository( String baseUrl )
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Reader openToRead( String filePath )
        throws IOException
    {
        return new InputStreamReader(
                                      new URL( baseUrl + filePath ).openStream() );
    }
}
