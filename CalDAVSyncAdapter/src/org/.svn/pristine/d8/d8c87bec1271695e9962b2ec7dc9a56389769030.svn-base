package org.osaf.caldav4j.scheduling.methods;

import java.io.IOException;

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.osaf.caldav4j.methods.PostMethod;

public class SchedulePostMethod extends PostMethod {

	// we have to set the Attendees and Organize headers taken from Calendar
	protected void addRequestHeaders(HttpState state, HttpConnection conn)
	throws IOException, HttpException {

		boolean addOrganizerToAttendees = false;
		boolean hasAttendees = false;
		
		// get ATTENDEES and ORGANIZER from ical and add 
		// Originator and Recipient to Header
		if ( this.calendar != null) {
			ComponentList cList = calendar.getComponents(); 
			if (Method.REPLY.equals(calendar.getProperty(Property.METHOD))) {
				addOrganizerToAttendees = true;
			}
			for (Object obj : cList) {
				if (! (obj  instanceof VTimeZone)) {
					CalendarComponent event = (CalendarComponent) obj;
					Organizer organizer = (Organizer) event.getProperty(Property.ORGANIZER); 

					if ((organizer != null) && (organizer.getValue() != null) &&
							(organizer.getValue().startsWith("mailto:"))
					) {
						
						super.addRequestHeader("Originator", organizer.getValue());
						if (addOrganizerToAttendees) {
							super.addRequestHeader("Recipient", organizer.getValue());    							
						}

						for (Object oAttendee: event.getProperties(Property.ATTENDEE)) {
							Attendee a = (Attendee) oAttendee;
							if (a.getValue().startsWith("mailto:")) {
								super.addRequestHeader("Recipient", a.getValue());    							
							}
						}   
					}
				} 
			}    					
		}

		super.addRequestHeaders(state, conn);
	}
}
