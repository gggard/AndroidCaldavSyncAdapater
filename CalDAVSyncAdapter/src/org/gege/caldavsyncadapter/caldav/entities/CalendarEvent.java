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
//import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.util.CompatibilityHints;

//import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.entity.StringEntity;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
//import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;

//import android.provider.CalendarContract.Events;
import android.util.Log;



public class CalendarEvent {


	private static final String TAG = "CalendarEvent";
	
	private URI uri;
	private String eTag;
	private String ics;

	private Calendar calendar;

	private Component calendarComponent;

	private boolean mAllDay = false;
	
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

	public boolean fetchBody() throws ClientProtocolException, IOException, CaldavProtocolException, ParserException {
		boolean Error = false;
		
		CaldavFacade.fetchEventBody(this);
		
		Error = !parseIcs();
		
		return !Error;
	}
	
	public String getAccessLevel() {
		String Result = "0";
		String Value = "";
		Property property = calendarComponent.getProperty("CLASS");
		if (property != null) {
			Value = property.getValue();
			if (Value.equals("PUBLIC"))
				Result = "3";
			else if (Value.equals("PRIVATE"))
				Result = "2";
			else if (Value.equals("CONFIDENTIAL"))
				Result = "2"; // should be 1, but is not implemented within Android
		}
		
		return Result;
	}
	
	public Integer getStatus() {
		Integer Result = null;
		String Value = "";
		Property property = calendarComponent.getProperty("STATUS");
		if (property != null) {
			Value = property.getValue();
			if (Value.equals("CONFIRMED"))
				Result = 1;
			else if (Value.equals("CANCELLED"))
				Result = 2;
			else if (Value.equals("TENTATIVE"))
				Result = 0;
		}
		
		return Result;
	}
	
	public String getDescription() {
		Property property = calendarComponent.getProperty("DESCRIPTION");
		if (property != null)
			return property.getValue();
		else
			return "";		
	}
	
	public String getLocation() {
		Property property = calendarComponent.getProperty("LOCATION");
		if (property != null) 
			return property.getValue();
		else
			return "";
	}

	public String getTitle() {
		Property property = calendarComponent.getProperty("SUMMARY");
		if (property != null)
			return property.getValue();
		else
			return "**unkonwn**";
	}
	
	public String getRRule() {
		String Result = "";
		Property property = calendarComponent.getProperty("RRULE");
		if (property != null)
			Result = property.getValue();

		return Result;
	}

	public String getExRule() {
		String Result = "";
		Property property = calendarComponent.getProperty("EXRULE");
		if (property != null)
			Result = property.getValue();

		return Result;
	}
	
	public String getRDate() {
		String Result = "";
		
		java.util.ArrayList<String> ExDates = this.getRDates();
		for (String Value: ExDates) {
			if (!Result.isEmpty())
				Result += ",";
			Result += Value;
		}
		
		return Result;
	}
	
	private java.util.ArrayList<String> getRDates() {
		java.util.ArrayList<String> Result = new java.util.ArrayList<String>();
		PropertyList Propertys = calendarComponent.getProperties("RDATE");
		if (Propertys != null) {
			Property property;
			for (Object objProperty : Propertys){
				property = (Property) objProperty;
				Result.add(property.getValue());
			}
		}

		return Result;
	}
	
	public String getExDate() {
		String Result = "";
		
		java.util.ArrayList<String> ExDates = this.getExDates();
		for (String Value: ExDates) {
			if (!Result.isEmpty())
				Result += ",";
			Result += Value;
		}
		
		return Result;
	}
	
	private java.util.ArrayList<String> getExDates() {
		java.util.ArrayList<String> Result = new java.util.ArrayList<String>();
		PropertyList Propertys = calendarComponent.getProperties("EXDATE");
		if (Propertys != null) {
			Property property;
			for (Object objProperty : Propertys){
				property = (Property) objProperty;
				Result.add(property.getValue());
			}
		}

		return Result;
	}
	
	
	public long getStartTime() {
		long Result = 0;
		DateTime startDate = null;
		DtStart dtstart;
		Property prop;
		
		try {
			prop = calendarComponent.getProperty("DTSTART");
			if (prop != null) {
				startDate = new DateTime(prop.getValue());
				dtstart = (DtStart) prop;
				if (dtstart.getTimeZone() == null) {
					// no TimeZone information in this date
					// -> would be calculated as local date (but UTC is needed)
					startDate = new DateTime(prop.getValue() + "T000000Z");
					mAllDay = true;
				}
			}
		} catch (ParseException e) {
			Log.e(TAG, "Start Date parsing exception",e);
		}
		
		if (startDate != null) {
			Result = startDate.getTime();
		} else {
			Result = 0;
		}
		
		return Result;
	}

	public long getEndTime() {
		long Result;
		DateTime endDate = null;
		DtEnd dtend;
		Property prop;
		
		try {
			prop = calendarComponent.getProperty("DTEND");
			if (prop != null) {
				endDate = new DateTime(prop.getValue());
				dtend = (DtEnd) prop;
				if (dtend.getTimeZone() == null) {
					// no TimeZone information in this date
					// -> would be calculated as local date (but UTC is needed)
					endDate = new DateTime(prop.getValue() + "T000000Z");
					mAllDay = true;
				}
			}
		} catch (ParseException e) {
			Log.e(TAG, "End Date parsing exception",e);
		}
		
		if (endDate != null) 
			Result = endDate.getTime();
		else
			Result = 0;
		
		return Result;
	}
	
	public String getDuration() {
		String Result = "P";
		long Start = this.getStartTime();
		long End   = this.getEndTime();
		long Duration = (End - Start) / 1000; // get rid of the milliseconds, they cann't be described with RFC2445-Duration
		
		Integer Days = (int) Math.ceil(Duration / 24 / 60 / 60);
		Integer Hours = (int) Math.ceil((Duration - (Days * 24 * 60 * 60)) / 60 / 60);
		Integer Minutes = (int) Math.ceil((Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60)) / 60);
		Integer Seconds = (int) (Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60) - (Minutes * 60));
	
		if (Days > 0)
			Result += Days.toString() + "D";
		
		if (!mAllDay) {
			//if a ALL_DAY event occurs, there is no need for hours, minutes and seconds (Android doesn't understand them)
			Result += "T";
			Result += Hours.toString() + "H";
			Result += Minutes.toString() + "M";
			Result += Seconds.toString() + "S";
		}
		
		return Result;
	}
	
	public Integer getAllDay() {
		Integer Result = 0;
/*		long Start = this.getStartTime();
		long End   = this.getEndTime();
		long Duration = (End - Start) / 1000; // get rid of the milliseconds, they cannt be descibt with RFC2445-Duration
*/		
//		if (Duration == (24 * 60 * 60))
		if (mAllDay)
			Result = 1;
		
		return Result;
	}
	

	private boolean parseIcs() throws CaldavProtocolException, IOException, ParserException {
		boolean Error = false;
		CalendarBuilder builder = new CalendarBuilder();
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		
		StringReader reader = new StringReader(this.ics);

		
			this.calendar = builder.build(reader);
			
			ComponentList components = null;
			components = this.calendar.getComponents(Component.VEVENT);
			if (components.size() == 0) {
				components = this.calendar.getComponents(Component.VTODO);
				if (components.size() == 0) {
					throw new CaldavProtocolException("unknown events in ICS");
				} else {
					//throw new CaldavProtocolException("unsupported event TODO in ICS");
					Log.e(TAG, "unsupported event TODO in ICS");
					Error = true;
				}
			} else if (components.size() > 1) {
				//throw new CaldavProtocolException("Several events in ICS");
				Log.e(TAG, "Several events in ICS -> only first will be processed");
			}
			
			if (!Error)
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
			
			 
		return !Error;
	}

	public void setICS(String ics) {
		this.ics = ics;
	}

	public String getTimeZone() {

		Property property = calendarComponent.getProperty("DTSTART");
		DtStart dtStart = (DtStart) property;
		
		
		if ((dtStart != null) && (dtStart.getTimeZone() != null)) {
			return dtStart.getTimeZone().getID();
		} else {
			return "GMT";
		}
	}
	


}
