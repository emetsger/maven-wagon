package org.apache.maven.wagon.throttle;

/**
 * Represents methods for a client to explicitly release a connection permit.
 */
public interface ReleasableConnectionSemaphore extends ConnectionSemaphore
{
    /**
     * Explicitly return a connection permit.
     *
     * @return the remaining number of permits in the pool
     */
    public int release();
}
