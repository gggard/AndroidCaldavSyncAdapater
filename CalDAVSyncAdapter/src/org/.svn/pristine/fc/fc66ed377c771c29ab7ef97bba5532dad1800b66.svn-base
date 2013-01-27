/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osaf.caldav4j.methods;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.httpclient.Header;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.CalDAV4JProtocolException;
import org.osaf.caldav4j.util.UrlUtils;


public class GetMethod extends org.apache.commons.httpclient.methods.GetMethod{
    private static final String CONTEN_TYPE_TEXT_HTML = "text/html";

	private static final String HEADER_ACCEPT = "Accept";

	private static final Log log = LogFactory.getLog(GetMethod.class);
    
    private CalendarBuilder calendarBuilder = null;
    
    protected GetMethod (){
        super();
        this.addRequestHeader(HEADER_ACCEPT, 
        		"text/calendar; text/html; text/xml;"); // required for bedework
        

    }

    public CalendarBuilder getCalendarBuilder() {
        return calendarBuilder;
    }

    public void setCalendarBuilder(CalendarBuilder calendarBuilder) {
        this.calendarBuilder = calendarBuilder;
    }

    public Calendar getResponseBodyAsCalendar()  throws
            ParserException, CalDAV4JException {
    	Calendar ret = null;
    	BufferedInputStream stream = null;
        try {
		    Header header = getResponseHeader(CalDAVConstants.HEADER_CONTENT_TYPE);
		    String contentType = (header != null) ? header.getValue() : null;
		    if (StringUtils.isBlank(contentType) || 
		    		contentType.startsWith(CalDAVConstants.CONTENT_TYPE_CALENDAR)) {
		         stream = new BufferedInputStream(getResponseBodyAsStream());
		        ret =  calendarBuilder.build(stream);
		        return ret;		        
		    }

	        log.error("Expected content-type text/calendar. Was: " + contentType);
	        throw new CalDAV4JProtocolException("Expected content-type text/calendar. Was: " + contentType );
        } catch (IOException e) {
        	if (stream != null ) { //the server sends the response
        		if (log.isWarnEnabled()) {
        			log.warn("Server response is " + UrlUtils.parseISToString(stream));
        		}
        	}
        	throw new CalDAV4JException("Error retrieving and parsing server response at " + getPath(), e);
        }	       
    }
    
    // remove double slashes
    public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
}
