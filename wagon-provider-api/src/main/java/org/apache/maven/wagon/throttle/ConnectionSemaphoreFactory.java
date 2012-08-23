package org.apache.maven.wagon.throttle;

/**
 *
 */
public interface ConnectionSemaphoreFactory
{
    ConnectionSemaphore getInstance();
}
