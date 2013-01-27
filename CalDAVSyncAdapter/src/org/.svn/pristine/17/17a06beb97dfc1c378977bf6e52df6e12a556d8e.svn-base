package org.osaf.caldav4j.util;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.exceptions.AuthorizationException;
import org.osaf.caldav4j.exceptions.BadStatusException;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.ResourceNotFoundException;
import org.osaf.caldav4j.exceptions.ResourceNotFoundException.IdentifierType;
import org.osaf.caldav4j.exceptions.ResourceOutOfDateException;

public class MethodUtil {
	private static final Log log = LogFactory.getLog(MethodUtil.class);

	/**
	 * Throws various exceptions depending on the status>=400 of the given method
	 * @param method
	 * @return
	 * @throws CalDAV4JException 
	 * @throws  
	 */
	public static int StatusToExceptions(HttpMethod method) throws CalDAV4JException {
		if (method != null) {
			int status = method.getStatusCode();
			if (log.isDebugEnabled()) {
				try {
					log.debug("Server returned " + method.getResponseBodyAsString());
				} catch (IOException e) {
					throw new CalDAV4JException("Error retrieving server response", e);
				}
			}
			if (status >= 300) {				
				switch (status) {
				case CaldavStatus.SC_CONFLICT:
					throw new ResourceOutOfDateException("Conflict accessing: " + method.getPath() );
				case CaldavStatus.SC_NOT_FOUND:
					throw new ResourceNotFoundException(IdentifierType.PATH, method.getPath());
				case CaldavStatus.SC_UNAUTHORIZED:
					throw new AuthorizationException("Unauthorized accessing " + method.getPath() );
				case CaldavStatus.SC_PRECONDITION_FAILED:
					throw new ResourceOutOfDateException("Resource out of date: " + method.getPath());
				default:
					throw new BadStatusException(status, method.getName(), method.getPath());
				} 
			}
			return status;	
		}
		throw new CalDAV4JException("Null method");
	}
}
