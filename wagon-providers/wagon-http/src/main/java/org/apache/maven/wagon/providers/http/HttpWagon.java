package org.apache.maven.wagon.providers.http;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.util.DateParser;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.wagon.*;
import org.apache.maven.wagon.resource.Resource;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="michal.maczka@dimatics.com">Michal Maczka</a>
 * @version $Id$
 */
public class HttpWagon
    extends AbstractWagon
{
    private final static int DEFAULT_NUMBER_OF_ATTEMPTS = 3;

    private final static int SC_NULL = -1;

    private HttpClient client = null;

    private int numberOfAttempts = DEFAULT_NUMBER_OF_ATTEMPTS;

    public void openConnection()
    {
        client = new HttpClient( new MultiThreadedHttpConnectionManager() );

        final AuthenticationInfo authInfo = getRepository().getAuthenticationInfo();

        String username = null;

        String password = null;

        if ( authInfo != null )
        {
            username = authInfo.getUserName();

            password = authInfo.getPassword();
        }

        String host = getRepository().getHost();

        if ( StringUtils.isNotEmpty( username ) && StringUtils.isNotEmpty( password ) )
        {
            Credentials creds = new UsernamePasswordCredentials( username, password );

            client.getState().setCredentials( null, host, creds );
        }

        HostConfiguration hc = new HostConfiguration();

        ProxyInfo proxyInfo = getRepository().getProxyInfo();

        if ( proxyInfo != null )
        {
            String proxyUsername = proxyInfo.getUserName();

            String proxyPassword = proxyInfo.getPassword();

            String proxyHost = proxyInfo.getHost();

            if ( StringUtils.isNotEmpty( proxyUsername )
                && StringUtils.isNotEmpty( proxyPassword )
                && StringUtils.isNotEmpty( proxyHost ) )
            {
                Credentials creds = new UsernamePasswordCredentials( username, password );

                client.getState().setProxyCredentials( null, proxyHost, creds );
            }
        }

        hc.setHost( host );

        //start a session with the webserver
        client.setHostConfiguration( hc );
    }

    // put
    public void put( File source, String resourceName )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        String url = getRepository().getUrl() + "/" + resourceName;

        PutMethod putMethod = new PutMethod( url );

        Resource resource = new Resource( resourceName );

        try
        {
            InputStream is = new PutInputStream( source, resource, this, getTransferEventSupport()  );

            putMethod.setRequestBody( is );
        }
        catch ( FileNotFoundException e )
        {
            fireTransferError( resource, e );

            throw new ResourceDoesNotExistException( "Source file does not exist: " + source, e );
        }

        int statusCode = SC_NULL;

        int attempt = 0;

        fireTransferDebug( "about to execute client for put" );

        // We will retry up to NumberOfAttempts times.
        while ( ( statusCode == SC_NULL ) && ( attempt < getNumberOfAttempts() ) )
        {
            try
            {
                firePutStarted( resource, source );

                statusCode = client.executeMethod( putMethod );

                firePutCompleted( resource, source );

            }
            catch ( HttpRecoverableException e )
            {
                attempt++;

                continue;
            }
            catch ( IOException e )
            {
                throw new TransferFailedException( e.getMessage(), e );
            }
        }

        fireTransferDebug( url + " - Status code: " + statusCode );

        // Check that we didn't run out of retries.
        switch ( statusCode )
        {
            case HttpStatus.SC_OK:
                break;

             case HttpStatus.SC_CREATED:
                break;

            case SC_NULL:
                throw new ResourceDoesNotExistException( "File: " + url + " does not extist" );

            case HttpStatus.SC_FORBIDDEN:
                throw new AuthorizationException( "Access denided to: " + url );

            case HttpStatus.SC_NOT_FOUND:
                throw new ResourceDoesNotExistException( "File: " + url + " does not exist" );

                //add more entries here
            default :
                throw new TransferFailedException( "Failed to trasfer file: " + url + ". Return code is: " + statusCode );
        }

        putMethod.releaseConnection();

        firePutCompleted( resource, source );
    }

    public void closeConnection()
    {
    }

    public void get( String resourceName, File destination )
       throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        get( resourceName, destination, 0, false );
    }

    public boolean getIfNewer( String resourceName, File destination, long timestamp ) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        return get( resourceName, destination, timestamp, true );
    }

    /**
     *
     * @param resourceName
     * @param destination
     * @param timestamp
     * @param newerRequired
     * @return
     * @throws TransferFailedException
     * @throws ResourceDoesNotExistException
     * @throws AuthorizationException
     *
     * @return <code>true</code> if newer version was downloaded, <code>false</code> otherwise.
     */
    public boolean get( String resourceName, File destination, long timestamp, boolean newerRequired )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {

        boolean retValue = false;

        String url = getRepository().getUrl() + "/" + resourceName;

        GetMethod getMethod = new GetMethod( url );

        getMethod.addRequestHeader( "Cache-control", "no-cache" );

        getMethod.addRequestHeader( "Cache-store", "no-store" );

        getMethod.addRequestHeader( "Pragma", "no-cache" );

        getMethod.addRequestHeader( "Expires", "0" );

        int statusCode = SC_NULL;

        int attempt = 0;

        // We will retry up to NumberOfAttempts times.
        while ( ( statusCode == SC_NULL ) && ( attempt < getNumberOfAttempts() ) )
        {
            try
            {
                // execute the getMethod.
                statusCode = client.executeMethod( getMethod );
            }
            catch ( HttpRecoverableException e )
            {
                attempt++;

                continue;
            }
            catch ( IOException e )
            {
                throw new TransferFailedException( e.getMessage(), e );
            }
        }

        fireTransferDebug( url + " - Status code: " + statusCode );

        // Check that we didn't run out of retries.
        switch ( statusCode )
        {
            case HttpStatus.SC_OK:
                break;

            case SC_NULL:
                throw new ResourceDoesNotExistException( "File: " + url + " does not extist" );

            case HttpStatus.SC_FORBIDDEN:
                throw new AuthorizationException( "Access denided to: " + url );

            case HttpStatus.SC_NOT_FOUND:
                throw new ResourceDoesNotExistException( "File: " + url + " does not exist" );

                //add more entries here
            default :
                throw new TransferFailedException( "Failed to trasfer file: "
                                                   + url
                                                   + ". Return code is: "
                                                   + statusCode );
        }

        Resource resource = new Resource( resourceName );

        InputStream is = null;

        Header contentLengthHeader = getMethod.getResponseHeader( "Content-Length" );

        if ( contentLengthHeader != null )
        {
            try
            {
                long contentLength = Integer.valueOf( contentLengthHeader.getValue() ).intValue();

                resource.setContentLength( contentLength );
            }
            catch ( NumberFormatException e )
            {
                fireTransferDebug( "error parsing content length header '" + contentLengthHeader.getValue() + "' " + e );
            }
        }

        Header lastModifiedHeader = getMethod.getResponseHeader( "Last-Modified" );

        long lastModified = 0;

        if ( lastModifiedHeader != null )
        {
             try
             {
                lastModified = DateParser.parseDate( lastModifiedHeader.getValue() ).getTime();

                resource.setLastModified(  lastModified );
             }
             catch ( DateParseException e )
             {
                fireTransferDebug( "Unable to parse last modified header" );
             }

            fireTransferDebug( "last-modified = " + lastModifiedHeader.getValue() + " (" + lastModified + ")" );
        }

        //@todo have to check how m1 does it
        boolean isNewer = timestamp < lastModified;

        if(  ( isNewer && newerRequired )  || ( !newerRequired )  )
        {
            retValue = true;

            try
            {
                is = getMethod.getResponseBodyAsStream();

                getTransfer( resource, destination, is );
            }
            catch ( Exception e )
            {
                fireTransferError( resource, e );

                if ( destination.exists() )
                {
                    boolean deleted = destination.delete();

                    if ( ! deleted )
                    {
                        destination.deleteOnExit();
                    }
                }

                String msg = "Error occured while deploying to remote repository:" + getRepository();

                throw new TransferFailedException( msg, e );
            }
            finally
            {
                shutdownStream( is );
            }

        }
        getMethod.releaseConnection();

        return retValue;
    }

    public int getNumberOfAttempts()
    {
        return numberOfAttempts;
    }

    public void setNumberOfAttempts( int numberOfAttempts )
    {
        this.numberOfAttempts = numberOfAttempts;
    }
}