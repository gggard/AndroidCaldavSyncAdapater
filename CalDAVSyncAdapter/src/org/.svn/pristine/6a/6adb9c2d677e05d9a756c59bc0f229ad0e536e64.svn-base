package org.osaf.caldav4j.cache;

import org.osaf.caldav4j.CalDAVResource;

/**
 * Does nothing, but is very fast.
 * 
 * @author bobbyrullo
 */
public class NoOpResourceCache implements CalDAVResourceCache {

    /**
     * The only one you'll ever need! 
     */
    public static final NoOpResourceCache SINGLETON = new NoOpResourceCache();
    
    private NoOpResourceCache(){
        
    }
    
    public String getHrefForEventUID(String uid) {
        return null;
    }

    public CalDAVResource getResource(String href) {
        return null;
    }

    public void putResource(CalDAVResource calDAVResource) {
    }

    public void removeResource(String href) {
    }

}
