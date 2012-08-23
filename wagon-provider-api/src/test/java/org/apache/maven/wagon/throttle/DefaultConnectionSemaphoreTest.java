package org.apache.maven.wagon.throttle;

import junit.framework.TestCase;

import java.util.Calendar;

/**
 *
 */
public class DefaultConnectionSemaphoreTest extends TestCase
{

    /** A negative timeout; any negative number should suffice */
    static final int NEGATIVE_TIMEOUT = -2000;

    /** Zero seconds */
    static final int ZERO_TIMEOUT = 0;

    /** Two seconds */
    static final int POSITIVE_TIMEOUT = 2000;

    /** One second */
    static final int ONE_SECOND = 1000;

    /** Zero permits */
    static final int ZERO_PERMITS = 0;

    /** A single permit */
    static final int ONE_PERMIT = 1;

    /** Many permits */
    static final int MANY_PERMITS = 3;

    /** A negative number of permits; any negative number should suffice */
    static final int NEGATIVE_PERMITS = -5;

    /** The start time of the test */
    private long startTimeMs;

    /**
     * Records the start time of the test method, in milliseconds
     */
    public void setUp()
    {
        this.startTimeMs = now();
    }

    public void testAcquireWithNegativePermitsNegativeTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( NEGATIVE_PERMITS );
        assertTrue( underTest.acquire( NEGATIVE_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to
        // be within one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND  );
    }

    public void testAcquireWithNegativePermitsZeroTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( NEGATIVE_PERMITS );
        assertTrue( underTest.acquire( ZERO_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to
        // be within one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND  );
    }

    public void testAcquireWithNegativePermitsPositiveTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( NEGATIVE_PERMITS );
        assertTrue( underTest.acquire( POSITIVE_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to
        // be within one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND  );
    }

    public void testAcquireWithZeroPermitsNegativeTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ZERO_PERMITS );
        assertFalse( underTest.acquire( NEGATIVE_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to
        // be within one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND  );
    }

    public void testAcquireWithZeroPermitsZeroTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ZERO_PERMITS );
        assertFalse( underTest.acquire( ZERO_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to
        // be within one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND  );
    }

    public void testAcquireWithZeroPermitsPositiveTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ZERO_PERMITS );
        assertFalse( underTest.acquire( POSITIVE_TIMEOUT ) );

        // the method under test should have blocked for POSITIVE_TIMEOUT, so it is reasonable for the end time to
        // be greater than or equal to POSITIVE_TIMEOUT
        assertTrue( now() - startTimeMs >= POSITIVE_TIMEOUT );
    }

    public void testAcquireWithOnePermitNegativeTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ONE_PERMIT );
        assertTrue( underTest.acquire( NEGATIVE_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    public void testAcquireWithOnePermitZeroTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ONE_PERMIT );
        assertTrue( underTest.acquire( ZERO_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    public void testAcquireWithOnePermitPositiveTimeout() throws Exception
    {
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( ONE_PERMIT );
        assertTrue( underTest.acquire( POSITIVE_TIMEOUT ) );

        // the method under test should not have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    public void testAcquireWithMultiplePermitsNegativeTimeout() throws Exception
    {
        int noPermits = 3;
        int actualAcquired = ZERO_PERMITS;
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( noPermits );
        for ( int i = ZERO_PERMITS; i < noPermits; i++ )
        {
            assertTrue( underTest.acquire( NEGATIVE_TIMEOUT ) );
            actualAcquired++;
        }

        assertEquals( noPermits, actualAcquired );

        // the method under test should never have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    public void testAcquireWithMultiplePermitsZeroTimeout() throws Exception
    {
        int actualAcquired = 0;
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( MANY_PERMITS );
        for ( int i = 0; i < MANY_PERMITS; i++ )
        {
            assertTrue( underTest.acquire( ZERO_TIMEOUT ) );
            actualAcquired++;
        }

        assertEquals( MANY_PERMITS, actualAcquired );

        // the method under test should never have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    public void testAcquireWithMultiplePermitsPositiveTimeout() throws Exception
    {
        int actualAcquired = 0;
        DefaultConnectionSemaphore underTest = new DefaultConnectionSemaphore( MANY_PERMITS );
        for ( int i = 0; i < MANY_PERMITS; i++ )
        {
            assertTrue( underTest.acquire( POSITIVE_TIMEOUT ) );
            actualAcquired++;
        }

        assertEquals( MANY_PERMITS, actualAcquired );
        
        // the method under test should never have blocked, so it is reasonable for the start and end time to be within
        // one second of each other
        assertTrue( now() - startTimeMs < ONE_SECOND );
    }

    /** The current time in milliseconds, since the epoch */
    private static long now()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
}
