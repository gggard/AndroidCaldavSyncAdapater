package org.osaf.caldav4j.model.request;

import org.osaf.caldav4j.CalDAVConstants;

public class DisplayName extends PropProperty {
	protected static String DISPLAY_NAME = "displayname" ;

	public DisplayName(String displayName) {
        super(CalDAVConstants.NS_DAV, "D", DISPLAY_NAME);
        if (displayName!=null) 
        	setTextContent(displayName);
	}
	/**
	 * create a <D:displayname/>
	 */
	public DisplayName() {
		this(null);
	}
}

