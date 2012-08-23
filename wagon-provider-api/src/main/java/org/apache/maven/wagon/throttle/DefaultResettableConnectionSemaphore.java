package org.apache.maven.wagon.throttle;

import java.util.Calendar;

/**
 * A {@link ConnectionSemaphore} that can have the number of permits reset.
 * 
 * @plexus.component role="org.apache.maven.wagon.throttle.ConnectionSemaphore"
 *    role-hint="resettable"
 *    instantiation-strategy="singleton"
 */
public class DefaultResettableConnectionSemaphore extends DefaultConnectionSemaphore
        implements ResettableConnectionSemaphore
{

    /**
     *  @plexus.configuration default-value="10000"
     */
    private int interval;

    private Thread resetThread;

    public DefaultResettableConnectionSemaphore( )
    {
        super();
    }

    public DefaultResettableConnectionSemaphore( int permitPoolSize )
    {
        super( permitPoolSize );
    }

    public DefaultResettableConnectionSemaphore( int permitPoolSize, int interval )
    {
        super( permitPoolSize );
        this.interval = interval;
        launchResetThread( this, interval );
    }

    public int reset()
    {
        synchronized ( permitsLock )
        {
            permits.drainPermits();
            permits.release( permitPoolSize );
            System.err.println( Integer.toHexString( System.identityHashCode( this ) ) + ": " +
                    Calendar.getInstance().getTimeInMillis() + " (" + Calendar.getInstance().getTime().toString() +
                    "): Reset connection permits to " + permitPoolSize );
        }

        return permitPoolSize;
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval( int interval )
    {
        this.interval = interval;
        if ( resetThread != null )
        {
            resetThread.interrupt();
            try
            {
                resetThread.join();
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        launchResetThread( this, interval );
    }

    private void launchResetThread( final DefaultResettableConnectionSemaphore semaphore, final int interval )
    {
        this.resetThread = new Thread( new Runnable()
        {
            public void run()
            {
                do
                {
                    try
                    {
                        Thread.sleep( interval );
                    }
                    catch ( InterruptedException e )
                    {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    semaphore.reset();
                }
                while ( true );
            }
        }, this.getClass().getName() + " Connection Reset Thread" );

        resetThread.start();
    }
}
