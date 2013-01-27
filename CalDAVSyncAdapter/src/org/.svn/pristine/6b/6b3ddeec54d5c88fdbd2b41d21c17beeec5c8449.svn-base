package org.osaf.caldav4j.model.request;

import org.osaf.caldav4j.CalDAVConstants;


/**
 * 
 * @author robipolli@gmail.com
 * @see http://tools.ietf.org/html/rfc4791#section-5.2.1
 */
public class CalendarDescription extends PropProperty {

	public CalendarDescription() {
		this(null,null);
	}
	public CalendarDescription(String value) {
		this(value,null);

	}
	public CalendarDescription(String value, String lang) {
		super(CalDAVConstants.NS_CALDAV, CalDAVConstants.NS_QUAL_CALDAV, CalDAVConstants.CALDAV_CALENDAR_DESCRIPTION);
		if (value != null) {
			this.setTextContent(value);
			if (lang != null) {
				this.addAttribute("xml:lang", lang);
			}
		}
	}
}
