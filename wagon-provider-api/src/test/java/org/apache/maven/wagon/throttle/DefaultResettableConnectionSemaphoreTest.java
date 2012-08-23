package org.apache.maven.wagon.throttle;

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 */
public class DefaultResettableConnectionSemaphoreTest extends TestCase
{
    public void testBasicReset() throws Exception
    {
        DefaultResettableConnectionSemaphore underTest = new DefaultResettableConnectionSemaphore(
                DefaultConnectionSemaphoreTest.MANY_PERMITS );

        for ( int i = 0; i < DefaultConnectionSemaphoreTest.MANY_PERMITS; i++ )
        {
            assertTrue( underTest.acquire( DefaultConnectionSemaphoreTest.NEGATIVE_TIMEOUT ) );
        }

        assertFalse( underTest.acquire( DefaultConnectionSemaphoreTest.NEGATIVE_TIMEOUT ) );

        underTest.reset();

        for ( int i = 0; i < DefaultConnectionSemaphoreTest.MANY_PERMITS; i++ )
        {
            assertTrue( underTest.acquire( DefaultConnectionSemaphoreTest.NEGATIVE_TIMEOUT ) );
        }

        assertFalse( underTest.acquire( DefaultConnectionSemaphoreTest.NEGATIVE_TIMEOUT ) );
    }

    public void testThreadedReset() throws InterruptedException
    {
        final int timeout = 5 * 1000;
        final DefaultResettableConnectionSemaphore underTest = new DefaultResettableConnectionSemaphore(
                DefaultConnectionSemaphoreTest.MANY_PERMITS );

        int expectedPermits = DefaultConnectionSemaphoreTest.MANY_PERMITS + 1;
        Thread[] t = new Thread[expectedPermits];
        final AtomicInteger acquiredPermits = new AtomicInteger( 0 );

        for ( int i = 0; i < expectedPermits; i++ )
        {
            t[ i ] = new Thread( new Runnable()
            {
                public void run()
                {
                    boolean acquired = underTest.acquire( timeout );
                    if ( acquired )
                    {
                        acquiredPermits.getAndIncrement();
                    }
                }
            }, "Acquire Thread " + i );

            t[ i ].start();
        }

        Thread.sleep( timeout / 2 );

        assertEquals( DefaultConnectionSemaphoreTest.MANY_PERMITS, underTest.reset() );

        for ( int i = 0; i < expectedPermits; i++ )
        {
            t[ i ].join();
        }

        assertEquals( expectedPermits, acquiredPermits.intValue() );
    }
}
