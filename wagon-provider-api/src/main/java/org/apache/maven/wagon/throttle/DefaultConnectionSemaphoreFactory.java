package org.apache.maven.wagon.throttle;

import com.sun.xml.internal.rngom.digested.DDataPattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DefaultConnectionSemaphoreFactory implements ConnectionSemaphoreFactory
{
    private static Map /* <Params, ConnectionSemaphore> */ instances = Collections.synchronizedMap( new HashMap() );

    private static Params params = new Params( -1, -1 );

    public DefaultConnectionSemaphoreFactory( int permitPoolSize, int resetInterval )
    {
        Params params = new Params( permitPoolSize, resetInterval );

        synchronized ( this )
        {
            if ( !instances.containsKey( params ) )
            {
                instances.put( params, new DefaultResettableConnectionSemaphore( permitPoolSize, resetInterval ) );
            }
        }
    }

    public synchronized ConnectionSemaphore getInstance()
    {
        return (ConnectionSemaphore) instances.values().iterator().next();
    }

//    public synchronized ConnectionSemaphore getInstance( int permitPoolSize, int resetInterval )
//    {
//        Params params = new Params( permitPoolSize, resetInterval );
//        DefaultResettableConnectionSemaphore instance = null;
//        if (! instances.containsKey( params ) )
//        {
//            instance = new DefaultResettableConnectionSemaphore( permitPoolSize, resetInterval );
//            instances.put( params, instance );
//
//        }
//        else
//        {
//            instance = (DefaultResettableConnectionSemaphore) instances.get( params );
//        }
//
//        return instance;
//    }

    private static class Params {
        int permitPoolSize;
        int resetInterval;

        private Params( int permitPoolSize, int resetInterval )
        {
            this.permitPoolSize = permitPoolSize;
            this.resetInterval = resetInterval;
        }

        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            Params params = ( Params ) o;

            if ( permitPoolSize != params.permitPoolSize )
            {
                return false;
            }
            if ( resetInterval != params.resetInterval )
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            int result = permitPoolSize;
            result = 31 * result + resetInterval;
            return result;
        }
    }
}
