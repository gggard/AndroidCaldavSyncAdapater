package org.osaf.caldav4j;

/** 
 * TODO tutti gli attributi, tutti i metodi non specializzati
 * 
 * @author rpolli
 * */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.osaf.caldav4j.cache.CalDAVResourceCache;
import org.osaf.caldav4j.cache.EhCacheResourceCache;
import org.osaf.caldav4j.cache.NoOpResourceCache;
import org.osaf.caldav4j.exceptions.BadStatusException;
import org.osaf.caldav4j.exceptions.CacheException;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.ResourceOutOfDateException;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.HttpClient;
import org.osaf.caldav4j.methods.OptionsMethod;
import org.osaf.caldav4j.methods.PutMethod;
import org.osaf.caldav4j.util.CaldavStatus;
import org.osaf.caldav4j.util.UrlUtils;

public abstract class CalDAVCalendarCollectionBase implements CalDAVConstants {
	
	 CalDAV4JMethodFactory methodFactory = null;
	 private String calendarCollectionRoot = null;
	 HostConfiguration hostConfiguration = null;
	 String prodId = null;
	 Random random = new Random();
	 protected CalDAVResourceCache cache = NoOpResourceCache.SINGLETON;

	//Configuration Methods
	
	public HostConfiguration getHostConfiguration() {
	    return hostConfiguration;
	}

	public void setHostConfiguration(HostConfiguration hostConfiguration) {
	    this.hostConfiguration = hostConfiguration;
	}

	public CalDAV4JMethodFactory getMethodFactory() {
	    return methodFactory;
	}

	public void setMethodFactory(CalDAV4JMethodFactory methodFactory) {
		this.methodFactory = methodFactory; 
	}

	/**
	 * remove double slashes
	 * @return
	 */
	public String getCalendarCollectionRoot() {
	    return UrlUtils.removeDoubleSlashes(calendarCollectionRoot);
	}

	/**
	 * set base path, appending trailing "/" and  removing unneeded "/"
	 * @param path
	 */
	public void setCalendarCollectionRoot(String path) {
	    this.calendarCollectionRoot = UrlUtils.removeDoubleSlashes(path.concat("/"));
	}

	public CalDAVResourceCache getCache() {
	    return cache;
	}

	public void setCache(CalDAVResourceCache cache) {
	    this.cache = cache;
	}
	
	/**
	 * Check if a cache is set
	 * @return true if cache is not NoOpResourceCache
	 */
	public boolean isCacheEnabled() {
		boolean p =  (this.cache instanceof NoOpResourceCache);
		return !p;
	}
	

	/**
	 * Returns the path relative to the calendars path given an href
	 * 
	 * @param href
	 * @return
	 */
	protected String getRelativePath(String href){
	    int start = href.indexOf(calendarCollectionRoot);
	    return href.substring(start + calendarCollectionRoot.length() + 1);
	}

	/**
	 * Create a PUT method setting If-None-Match: *
	 * this tag causes PUT fails if a given event exist  
	 * @param resourceName
	 * @param calendar
	 * @return a PutMethod for creating events
	 */
	 PutMethod createPutMethodForNewResource(String resourceName,
	        Calendar calendar) {
	    PutMethod putMethod = methodFactory.createPutMethod();
	    putMethod.setPath(calendarCollectionRoot + "/"
	    		+ resourceName);
	    putMethod.setAllEtags(true);
	    putMethod.setIfNoneMatch(true);
	    putMethod.setRequestBody(calendar);
	    return putMethod;
	}

	 /**
	  * TODO check hostConfiguration.getUri()
	  * @param path
	  * @return the URI of the path resource
	  * 
	  * XXX maybe it will be faster to write down the whole url including port...
	  */
	 String getHref(String path){
		 int port = hostConfiguration.getPort();
		 String scheme = hostConfiguration.getProtocol().getScheme();
		 String portString = "";
		 if ( (port != 80 && "http".equals(scheme)) || 
				 (port != 443 && "https".equals(scheme))
		 ) {
			 portString = ":" + port;
		 } 

		 return UrlUtils.removeDoubleSlashes(
				 String.format("%s://%s%s/%s", scheme,
						 hostConfiguration.getHost(),
						 portString, path )
		 );
	 }

		

		/**
		 * create cache resources: UID_TO_HREF, HREF_TO_RESOURCE
		 * XXX create test method
		 */
		public void enableSimpleCache() throws CalDAV4JException {	
			EhCacheResourceCache cache = null;
			if (!isCacheEnabled()) {
				try {
					cache = EhCacheResourceCache.createSimpleCache();
				} catch (CacheException e) {
					// avoid error if cache doesn't exist
					e.printStackTrace();
				}
				this.setCache(cache);
			}
		}
		
		/**
		 * set cache to NoOpResourceCache
		 * XXX test it
		 */
		public void disableSimpleCache() {
			EhCacheResourceCache.removeSimpleCache();	        
	        this.setCache(NoOpResourceCache.SINGLETON);
		}

		private boolean tolerantParsing = false;

		public boolean isTolerantParsing() {
			return tolerantParsing;
		}

		public void setTolerantParsing(boolean tolerantParsing) {
			this.tolerantParsing = tolerantParsing;
		}


		/**
		 * TODO manage status codes with an helper
		 * TODO I should throw an exception when server doesn't set etag
		 * 
		 * @param httpClient
		 * @param calendar
		 * @param path
		 * @param etag
		 * @throws CalDAV4JException
		 */
	void put(HttpClient httpClient, Calendar calendar, String path,
	        String etag)
	        throws CalDAV4JException {
	    PutMethod putMethod = methodFactory.createPutMethod();
	    putMethod.addEtag(etag);
	    putMethod.setPath(path);
	    putMethod.setIfMatch(true);
	    putMethod.setRequestBody(calendar);
	    try {
	        httpClient.executeMethod(hostConfiguration, putMethod);
	        int statusCode = putMethod.getStatusCode();
	        switch(statusCode) {
	        case CaldavStatus.SC_NO_CONTENT:
	        case CaldavStatus.SC_CREATED:
	        	break;
	        case CaldavStatus.SC_PRECONDITION_FAILED:
	            throw new ResourceOutOfDateException("Etag was not matched: "+ etag);
            default:
            	throw new BadStatusException(statusCode, putMethod.getName(), path);
	        }	        
	    } catch (Exception e){
	        throw new CalDAV4JException("Problem executing put method",e);
	    }
	
	    Header h = putMethod.getResponseHeader("ETag");
	
	    if (h != null) {
	        String newEtag = h.getValue();
	        cache.putResource(new CalDAVResource(calendar, newEtag, getHref(putMethod.getPath())));
	    } 	    	
	}

	/**
	 * get OPTIONS for calendarCollectionRoot
	 * @param httpClient
	 * @return
	 * @throws CalDAV4JException
	 */
	public List<Header> getOptions(HttpClient httpClient) 
		throws CalDAV4JException {
		List<Header> hList = new ArrayList<Header>();
		
		OptionsMethod optMethod = new OptionsMethod();
		optMethod.setPath(this.calendarCollectionRoot);
		optMethod.setRequestHeader(new Header("Host",
										hostConfiguration.getHost()));
		
		try {
			httpClient.executeMethod(this.hostConfiguration, optMethod);
		} catch (Exception e) {
			throw new CalDAV4JException("Trouble executing OPTIONS", e);
		}

		
		int status = optMethod.getStatusCode();
		switch (status) {
		case CaldavStatus.SC_OK:
			break;
		default:
			throw new BadStatusException(status, optMethod.getName(), getCalendarCollectionRoot());
		}

		for (Header h: optMethod.getResponseHeaders()) {
			hList.add(h);
		}
		
		return hList;		
	}
	
	/** 
	 * check whether server Allows the given action
	 * XXX we should implement it as allowed props should be an attribute of this class,
	 *  set each time the base-path is changed
	 * @param action
	 * @return
	 * @throws CalDAV4JException
	 */
	public boolean allows(HttpClient httpClient, String action, List<Header> hList)
			throws CalDAV4JException {		
		for (Header h : hList) {
			if ("Allow".equals(h.getName()) && (h.getValue() != null) && h.getValue().matches("\b"+action+"\b") ) {
				return true;
			}
		}
		return false;
	}
}
