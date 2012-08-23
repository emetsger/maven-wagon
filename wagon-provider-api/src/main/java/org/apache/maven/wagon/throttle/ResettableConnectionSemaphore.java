package org.apache.maven.wagon.throttle;

/**
 * A {@link ConnectionSemaphore} allowing a client to reset the number of available connection permits.
 */
public interface ResettableConnectionSemaphore extends ConnectionSemaphore
{
    
    /**
     * Resets the number of available permits.
     *
     * @return the number of available permits
     */
    public int reset();
}
