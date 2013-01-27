package org.gege.caldavsyncadapter.caldav.entities;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.text.ParseException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;

import android.util.Log;



public class CalendarEvent {


	private static final String TAG = "CalendarEvent";
	
	private URI uri;
	private String eTag;
	private String ics;

	private Calendar calendar;

	private Component calendarComponent;

	public String getETag() {
		return eTag;
	}

	public URI getUri() {
		return uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	public void fetchBody() throws ClientProtocolException, IOException, CaldavProtocolException {
		
		CaldavFacade.fetchEventBody(this);
		
		parseIcs();
		
	}

	public String getTitle() {
		return calendarComponent.getProperty("SUMMARY").getValue();
	}
	
	public long getStartTime() {
		DateTime startDate = null;
		
		try {
			Property prop = calendarComponent.getProperty("DTSTART");
			if (prop != null)
				startDate = new DateTime(prop.getValue());
		} catch (ParseException e) {
			Log.e(TAG, "Start Date parsing exception",e);
		}
		
		if (startDate != null)
			return startDate.getTime();
		else
			return 0;
	}

	private DateTime getEndDateTime() {
DateTime endDate = null;
		
		try {
			Property prop = calendarComponent.getProperty("DTEND");
			if (prop != null) {
				endDate = new DateTime(prop.getValue());
			}
		} catch (ParseException e) {
			Log.e(TAG, "End Date parsing exception",e);
		}
		return endDate;
	}
	
	public long getEndTime() {
		DateTime endDate = getEndDateTime();
		
		if (endDate != null) 
			return endDate.getTime();
		else
			return 0;
	}
	

	private void parseIcs() throws CaldavProtocolException {
		CalendarBuilder builder = new CalendarBuilder();
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		
		StringReader reader = new StringReader(this.ics);

		try {
			this.calendar = builder.build(reader);
			
			ComponentList components = this.calendar.getComponents(Component.VEVENT);
			if (components.size() != 1) {
				throw new CaldavProtocolException("Several events in ICS");
			}
			
			calendarComponent = (Component)this.calendar.getComponents(Component.VEVENT).get(0);
			
//				PropertyList properties = calendarComponent.getProperties();
//				for (Object oo : properties) {
//					Property p = (Property)oo;
//					Log.d(TAG, "Event Property : "+p.getName()+" = "+p.getValue());
//				}
//				
//				DateTime startDate = null;
//				DateTime endDate = null;
//				try {
//					startDate = new DateTime(calendarComponent.getProperty("DTSTART").getValue());
//					endDate = new DateTime(calendarComponent.getProperty("DTEND").getValue());
//				} catch (ParseException e) {
//					Log.e(TAG, "Date parsing exception",e);
//				}
//				
//				Log.e(TAG, "DTSTART = "+startDate.getTime());
//
//				Log.e(TAG, "DTSTART = "+endDate.getTime());
			
			 
		} catch (IOException e) {
			Log.e(TAG, "Exception Parsing ICS", e);
		} catch (ParserException e) {
			Log.e(TAG, "Exception Parsing ICS", e);
		}
	}

	public void setICS(String ics) {
		this.ics = ics;
	}

	public String getTimeZone() {

		DtStart dtStart = (DtStart) calendarComponent.getProperty("DTSTART");
		
		
		if (dtStart != null) {
			return dtStart.getTimeZone().getID();
		} else {
			return "GMT";
		}
	}
	


}
