package org.osaf.caldav4j.cache;

import org.osaf.caldav4j.CalDAVResource;
import org.osaf.caldav4j.exceptions.CacheException;

/**
 * Implementations allow for caching of CalDAVResources based on path, and also for
 * paths based on UID.
 * 
 * @author bobbyrullo
 *
 */
public interface CalDAVResourceCache {

    /**
     * Returns the cached CalDAVResource for the given href, or null
     * if none has been cached yet.
     * @param href the href to use to lookup the CalDAVResource
     * @return
     */
    public CalDAVResource getResource(String href) throws CacheException;

    /**
     * Adds the given resource to the cache, retrievable its href.
     * 
     * If there is an event in the enclosed calendar, adds the href to the cache 
     * retrievable by the UID of the event.
     * 
     * @param calDAVResource the resource to cache
     */
    public void putResource(CalDAVResource calDAVResource) throws CacheException;

    /**
     * Returns the href for which the resource with the event with the given 
     * UID is stored.
     * @param uid the uid for the resource whose href you want
     * @return
     */
    public String getHrefForEventUID(String uid) throws CacheException;

    /**
     * Removes a resource from the cache
     * @param href
     */
    public void removeResource(String href) throws CacheException;

}
