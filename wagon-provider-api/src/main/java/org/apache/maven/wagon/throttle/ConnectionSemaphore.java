package org.apache.maven.wagon.throttle;

/**
 * Represents methods for acquiring permits for attempting a connection over some transport (e.g. HTTP, SSH).  Clients
 * of this interface would attempt to acquire a permit before attempting a connection.
 */
public interface ConnectionSemaphore
{

    String ROLE = ConnectionSemaphore.class.getName();

    /**
     * Acquire a permit before obtaining a connection.
     *
     * @param timeoutMs the maximum amount of time to wait for acquiring a permit, in milliseconds.  Zero or negative
     * values indicate that this method should not block.
     * @return true if the permit was acquired, false otherwise
     */
    public boolean acquire( int timeoutMs );

}
