package org.apache.maven.wagon.throttle;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Allows clients to acquire connection permits, supplying an optional timeout.  If a client cannot obtain a connection
 * permit within the timeout period, then the client should not continue obtaining a connection, and instead throw
 * the appropriate exception.
 *
 * @plexus.component role="org.apache.maven.wagon.throttle.ConnectionSemaphore"
 *    role-hint="default"
 *    instantiation-strategy="singleton"
 */
public class DefaultConnectionSemaphore implements ConnectionSemaphore
{

    private static final String ACQUIRED_PERMIT = "%s: Acquired permit, %s permits (of %s) remaining.";
    private static final String DENIED_PERMIT = "%s Denied permit, no permits (of %s) remaining.";

    /**
     *  @plexus.configuration default-value="1"
     */
    int permitPoolSize;

    Semaphore permits;

    /** Mutex to obtain when modifying the {@link #permits} reference */
    final Object permitsLock = new Object();

    public DefaultConnectionSemaphore()
    {
        initializePermits( -1 );
    }

    public DefaultConnectionSemaphore( int permitPoolSize )
    {
        initializePermits( permitPoolSize );
    }

    public boolean acquire( int timeoutMs )
    {

        if ( permits == null )
        {
            System.err.println( String.format( ACQUIRED_PERMIT, new Object[] {
                    Integer.toHexString( System.identityHashCode( this ) ), "unlimited", "unlimited" } ) );
            return true;
        }

        if ( timeoutMs < 1 )
        {
            boolean result = permits.tryAcquire();
            logPermitAcquisition( result );
            return result;
        }
        else
        {
            try
            {
                boolean result = permits.tryAcquire( timeoutMs, TimeUnit.MILLISECONDS );
                logPermitAcquisition( result );
                return result;
            }
            catch ( InterruptedException e )
            {
                Thread.currentThread().isInterrupted();
                return false;
            }
        }
    }

    public int getPermitPoolSize()
    {
        return permitPoolSize;
    }

    public void setPermitPoolSize( int permitPoolSize )
    {
        if ( permitPoolSize != this.permitPoolSize )
        {
            initializePermits( permitPoolSize );
        }
    }

    private void initializePermits( int permitPoolSize )
    {
        synchronized ( permitsLock )
        {
            System.err.println( Integer.toHexString( System.identityHashCode( this ) ) + ": Initializing connection " +
                    "permit pool to " + permitPoolSize );
            this.permitPoolSize = permitPoolSize;
            if ( permitPoolSize >= 0 )
            {
                this.permits = new Semaphore( this.permitPoolSize );
            }
            else
            {
                permits = null;
            }
        }
    }

    private void logPermitAcquisition( boolean success )
    {
        if ( success )
        {
            System.err.println( String.format( ACQUIRED_PERMIT,
                    new Object[]{ Integer.toHexString( System.identityHashCode( this ) ),
                            Integer.valueOf( permits.availablePermits() ),
                            Integer.valueOf( permitPoolSize ) } ) );
        }
        else
        {
            System.err.println( String.format( DENIED_PERMIT, new Object[]{
                    Integer.toHexString( System.identityHashCode( this ) ),
                    Integer.valueOf( permitPoolSize ) } ) );
        }
    }

    public String toString()
    {
        return "DefaultConnectionSemaphore{" +
                "permitPoolSize=" + permitPoolSize +
                ", permits=" + permits +
                ", permitsLock=" + permitsLock +
                '}';
    }
}
