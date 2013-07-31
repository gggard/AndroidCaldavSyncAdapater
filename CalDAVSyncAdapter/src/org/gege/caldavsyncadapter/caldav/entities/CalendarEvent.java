/**
 * Copyright (c) 2012-2013, Gerald Garcia, Timo Berger
 * 
 * This file is part of Andoid Caldav Sync Adapter Free.
 *
 * Andoid Caldav Sync Adapter Free is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or at your option any later version.
 *
 * Andoid Caldav Sync Adapter Free is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoid Caldav Sync Adapter Free.  
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.gege.caldavsyncadapter.caldav.entities;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.http.client.ClientProtocolException;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;



public class CalendarEvent extends org.gege.caldavsyncadapter.Event {
	private static final String TAG = "CalendarEvent";
	
	private String stringIcs;
	
	private Calendar calendar;

	private Component calendarComponent;
	
	private String eTag;
	private URI muri;
	private Uri mAndroidEventUri;
	
	private boolean mAllDay = false;
	private VTimeZone mVTimeZone = null; 
	private TimeZone mTimeZone = null; 
	
	private String mstrTimeZoneStart = "";
	private String mstrTimeZoneEnd = "";

	public String getETag() {
		return eTag;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}
	
	public URI getUri() {
		return muri;
	}

	public void setUri(URI uri) {
		this.muri = uri;
	}

	public void setICSasString(String ics) {
		this.stringIcs = ics;
	}
	
	/**
	 * sets the Uri of the android event
	 * @param uri
	 * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#createAndroidEvent(ContentProviderClient provider, Account account, Uri calendarUri, CalendarEvent calendarEvent)
	 * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#updateAndroidEvent(ContentProviderClient provider, Account account, AndroidEvent androidEvent, CalendarEvent calendarEvent)
	 */
	public void setAndroidEventUri(Uri uri) {
		mAndroidEventUri = uri;
	}
	
	/**
	 * gets the Uri of the android event
	 * @return
	 */
	public Uri getAndroidEventUri() {
		return mAndroidEventUri;
	}
	
	
	/**
	 * reads all ContentValues from the caldav source
	 * @param calendarUri
	 * @return
	 * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#createAndroidEvent(ContentProviderClient provider, Account account, Uri calendarUri, CalendarEvent calendarEvent)
	 * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#updateAndroidEvent(ContentProviderClient provider, Account account, AndroidEvent androidEvent, CalendarEvent calendarEvent)
	 */
	//public boolean readContentValues(Uri calendarUri) {
	public boolean readContentValues() {
		this.ContentValues.put(Events.DTSTART, this.getStartTime());
		this.ContentValues.put(Events.EVENT_TIMEZONE, this.getTimeZoneStart());

		if (this.getRRule().isEmpty() && this.getRDate().isEmpty()) {
			//if (AllDay.equals(1)) //{
			//	values.put(Events.ALL_DAY, AllDay);
			//} else {
			this.ContentValues.put(Events.DTEND, this.getEndTime());
			this.ContentValues.put(Events.EVENT_END_TIMEZONE, this.getTimeZoneEnd());
			//}
		} else {
			//if (AllDay.equals(1))
			//	values.put(Events.ALL_DAY, AllDay);
			this.ContentValues.put(Events.DURATION, this.getDuration());
		}
		int AllDay = this.getAllDay();
		this.ContentValues.put(Events.ALL_DAY, AllDay);
		
		this.ContentValues.put(Events.TITLE, this.getTitle());
		//this.ContentValues.put(Events.CALENDAR_ID, ContentUris.parseId(calendarUri));
		this.ContentValues.put(Events._SYNC_ID, this.getUri().toString());
		this.ContentValues.put(ceTAG, this.getETag());
		this.ContentValues.put(Events.DESCRIPTION, this.getDescription());
		this.ContentValues.put(Events.EVENT_LOCATION, this.getLocation());
		this.ContentValues.put(Events.ACCESS_LEVEL, this.getAccessLevel());
		this.ContentValues.put(Events.STATUS, this.getStatus());
		this.ContentValues.put(Events.RDATE, this.getRDate());
		this.ContentValues.put(Events.RRULE, this.getRRule());
		this.ContentValues.put(Events.EXRULE, this.getExRule());
		this.ContentValues.put(Events.EXDATE, this.getExDate());
		
		return true;
	}
	

	/**
	 * receives a single event and parses its content
	 * @return success of this function
	 * @see CalendarEvent#parseIcs()
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CaldavProtocolException
	 * @throws ParserException
	 */
	public boolean fetchBody() throws ClientProtocolException, IOException, CaldavProtocolException, ParserException {
		boolean Error = false;
		
		CaldavFacade.fetchEventBody(this);
		
		boolean Parse = parseIcs();
		if (!Parse)
			Error = true;
		
		return !Error;
	}
	
	public java.util.ArrayList<ContentValues> getReminders() {
		java.util.ArrayList<ContentValues> Result = new java.util.ArrayList<ContentValues>();
		ContentValues Reminder;
		
		/*
		 * http://sourceforge.net/tracker/?func=detail&aid=3021704&group_id=107024&atid=646395
		 */

		net.fortuna.ical4j.model.component.VEvent event = (VEvent) this.calendarComponent;

		//ComponentList ComList = this.calendar.getComponents(Component.VALARM);
		ComponentList ComList = event.getAlarms();

		if (ComList != null) {
			for (Object objCom : ComList) {
				Component Com = (Component) objCom;
				Reminder = new ContentValues();
				
				//Property ACTION = Com.getProperty("ACTION");
				Property TRIGGER = Com.getProperty("TRIGGER");
				if (TRIGGER != null) {
					Dur Duration = new Dur(TRIGGER.getValue());
					//if (ACTION.getValue().equals("DISPLAY"))
					
					int intDuration = Duration.getMinutes() + Duration.getHours() * 60 + Duration.getDays() * 60 * 24;
					
					Reminder.put(Reminders.EVENT_ID, ContentUris.parseId(mAndroidEventUri));
					Reminder.put(Reminders.METHOD, Reminders.METHOD_ALERT);
					Reminder.put(Reminders.MINUTES, intDuration);
					
					Result.add(Reminder);
				}
			}
		}
		return Result;
	}
	
	public java.util.ArrayList<ContentValues> getAttandees() {
		java.util.ArrayList<ContentValues> Result = new java.util.ArrayList<ContentValues>();
		ContentValues Attendee;
		PropertyList Propertys = calendarComponent.getProperties(Property.ATTENDEE);
		if (Propertys != null) {
			for (Object objProperty : Propertys){
				Property property = (Property) objProperty;
				Attendee = ReadAttendeeProperties(property,Property.ATTENDEE);
				if (Attendee != null)
					Result.add(Attendee);
			}
		}
		Propertys = calendarComponent.getProperties(Property.ORGANIZER);
		if (Propertys != null) {
			for (Object objProperty : Propertys){
				Property property = (Property) objProperty;
				Attendee = ReadAttendeeProperties(property,Property.ORGANIZER);
				if (Attendee != null)
					Result.add(Attendee);
			}
		}
		
		
		return Result;
	}
	
	private String mstrcIcalPropertyError = "net.fortunal.ical4j.invalid:";
	private ContentValues ReadAttendeeProperties(Property property, String Type) {
		ContentValues Attendee = null;
		
		ParameterList Parameters = property.getParameters();
		Parameter CN       = Parameters.getParameter(Cn.CN);
		Parameter ROLE     = Parameters.getParameter(Role.ROLE);
		Parameter CUTYPE   = Parameters.getParameter(CuType.CUTYPE);
		//Parameter RSVP     = Parameters.getParameter("RSVP");
		Parameter PARTSTAT = Parameters.getParameter(PartStat.PARTSTAT);

		String strCN = "";
		String strROLE = "";
		String strCUTYPE = "";
		String strValue = property.getValue().replace("mailto:", "");
		String strPARTSTAT = "";
		
		if (strValue.startsWith(mstrcIcalPropertyError)) {
			strValue = strValue.replace(mstrcIcalPropertyError, "");
			try {
				strValue = URLDecoder.decode(strValue, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		if (CN != null)
			strCN = CN.getValue();
		if (ROLE != null)
			strROLE = ROLE.getValue();
		if (CUTYPE != null)
			strCUTYPE = CUTYPE.getValue();
		if (PARTSTAT != null)
			strPARTSTAT = PARTSTAT.getValue();
		
		if (strCN.equals("")) {
			if (!strValue.equals("")) {
				strCN = strValue;
			}
		}

		if (!strCN.equals("")) {
			if (strCUTYPE.equals("") || strCUTYPE.equals("INDIVIDUAL")) {
				Attendee = new ContentValues();
				
				Attendee.put(Attendees.EVENT_ID, ContentUris.parseId(mAndroidEventUri));

				Attendee.put(Attendees.ATTENDEE_NAME, strCN);
				Attendee.put(Attendees.ATTENDEE_EMAIL, strValue);

				if (strROLE.equals("OPT-PARTICIPANT"))
					Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_OPTIONAL);
				else if (strROLE.equals("NON-PARTICIPANT"))
					Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_NONE);
				else if (strROLE.equals("REQ-PARTICIPANT"))
					Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
				else if (strROLE.equals("CHAIR"))
					Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
				else 
					Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_NONE);
				
				if (Type.equals(Property.ATTENDEE))
					Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ATTENDEE);
				else if (Type.equals(Property.ORGANIZER))
					Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ORGANIZER);
				else
					Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_NONE);

				if (strPARTSTAT.equals(PartStat.NEEDS_ACTION.getValue()))
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_INVITED);
				else if (strPARTSTAT.equals(PartStat.ACCEPTED.getValue()))
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_ACCEPTED);
				else if (strPARTSTAT.equals(PartStat.DECLINED.getValue()))
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_DECLINED);
				else if (strPARTSTAT.equals(PartStat.COMPLETED.getValue()))
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_NONE);
				else if (strPARTSTAT.equals(PartStat.TENTATIVE.getValue()))
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_TENTATIVE);
				else
					Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_INVITED);

			}
		}
		
		return Attendee;
	}
	
	private long getAccessLevel() {
		long Result = Events.ACCESS_DEFAULT;
		String Value = "";
		Property property = calendarComponent.getProperty(Property.CLASS);
		if (property != null) {
			Value = property.getValue();
			if (Value.equals(Clazz.PUBLIC))
				Result = Events.ACCESS_PUBLIC;
			else if (Value.equals(Clazz.PRIVATE))
				Result = Events.ACCESS_PRIVATE;
			else if (Value.equals(Clazz.CONFIDENTIAL))
				Result = Events.ACCESS_PRIVATE; // should be ACCESS_CONFIDENTIAL, but is not implemented within Android
		}
		
		return Result;
	}
	
	private int getStatus() {
		int Result = Events.STATUS_TENTATIVE;
		String Value = "";
		Property property = calendarComponent.getProperty(Property.STATUS);
		if (property != null) {
			Value = property.getValue();
			if (Value.equals(Status.VEVENT_CONFIRMED))
				Result = Events.STATUS_CONFIRMED;
			else if (Value.equals(Status.VEVENT_CANCELLED))
				Result = Events.STATUS_CANCELED;
			else if (Value.equals(Status.VEVENT_TENTATIVE))
				Result = Events.STATUS_TENTATIVE;
		}
		
		return Result;
	}
	
	private String getDescription() {
		Property property = calendarComponent.getProperty(Property.DESCRIPTION);
		if (property != null)
			return property.getValue();
		else
			return "";		
	}
	
	private String getLocation() {
		Property property = calendarComponent.getProperty(Property.LOCATION);
		if (property != null) 
			return property.getValue();
		else
			return "";
	}

	private String getTitle() {
		Property property = calendarComponent.getProperty(Property.SUMMARY);
		if (property != null)
			return property.getValue();
		else
			return "**unkonwn**";
	}
	
	private String getRRule() {
		String Result = "";
		Property property = calendarComponent.getProperty(Property.RRULE);
		if (property != null)
			Result = property.getValue();

		return Result;
	}

	private String getExRule() {
		String Result = "";
		Property property = calendarComponent.getProperty(Property.EXRULE);
		if (property != null)
			Result = property.getValue();

		return Result;
	}
	
	private String getRDate() {
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
		PropertyList Propertys = calendarComponent.getProperties(Property.RDATE);
		if (Propertys != null) {
			Property property;
			for (Object objProperty : Propertys){
				property = (Property) objProperty;
				Result.add(property.getValue());
			}
		}

		return Result;
	}
	
	private String getExDate() {
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
		PropertyList Propertys = calendarComponent.getProperties(Property.EXDATE);
		if (Propertys != null) {
			Property property;
			for (Object objProperty : Propertys){
				property = (Property) objProperty;
				Result.add(property.getValue());
			}
		}

		return Result;
	}
	
	private Long getTimestamp(Property prop) {
		Long Result = null;
		String strTimeZone = "";
		//TimeZone timeZone = null;
		
		try {
			String strDate = prop.getValue();
			
			Parameter parAllDay = prop.getParameter("VALUE");
			if (parAllDay != null) {
				if (parAllDay.getValue().equals("DATE")) {
					mAllDay = true;
					strDate += "T000000Z";
				}
			}
			
			Parameter propTZ = prop.getParameter(Property.TZID);
			if (propTZ != null)
				strTimeZone = propTZ.getValue();
			
			//TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			//if (!strTimeZone.equals(""))
			//	timeZone = registry.getTimeZone(strTimeZone);

			//if (timeZone != null) {
			if (!strTimeZone.equals("")) {
				//Result = new DateTime(strDate, timeZone);
				//Result1 = Result.getTime();
				
				//20130331T120000
				int Year = Integer.parseInt(strDate.substring(0, 4));
				int Month = Integer.parseInt(strDate.substring(4, 6)) - 1;
				int Day = Integer.parseInt(strDate.substring(6, 8));
				int Hour = Integer.parseInt(strDate.substring(9, 11));
				int Minute = Integer.parseInt(strDate.substring(11, 13));
				int Second = Integer.parseInt(strDate.substring(13, 15));

				// time in UTC
				java.util.TimeZone jtz = java.util.TimeZone.getTimeZone("UTC");
				java.util.Calendar cal = java.util.GregorianCalendar.getInstance(jtz);
				cal.set(Year, Month, Day, Hour, Minute, Second);
				cal.set(java.util.Calendar.MILLISECOND, 0);
				Result = cal.getTimeInMillis();
				
				// get the timezone
				String[] IDs = java.util.TimeZone.getAvailableIDs();
				Boolean Found = false;
				for (int i = 0; i < IDs.length; i++) {
					Found = Found || IDs[i].equals(strTimeZone);
				}
				if (Found) {
					jtz = java.util.TimeZone.getTimeZone(strTimeZone);
					
					// subtract the offset
					Result -= jtz.getRawOffset();
					
					// remove dst
					if (jtz.inDaylightTime(new java.util.Date(Result)))
						Result -= jtz.getDSTSavings();

				} else {
					if (mTimeZone != null) {
						// subtract the offset
						Result -= mTimeZone.getRawOffset();
					
						// remove dst
						if (mTimeZone.inDaylightTime(new java.util.Date(Result)))
							Result -= mTimeZone.getDSTSavings();
					} else {
						// unknown Time?
						// treat as local time, should not happen too often :)
						Result = new DateTime(strDate).getTime();
					}
				}
			} else {
				if (strDate.endsWith("Z")) {
					// GMT or UTC
					Result = new DateTime(strDate).getTime();
				} else {
					// unknown Time?
					// treat as local time, should not happen too often :)
					Result = new DateTime(strDate).getTime();
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return Result;
	}
	
	private long getStartTime() {
		long Result = 0;
		Property prop;
		/*
		 * DTSTART;TZID=Europe/Berlin:20120425T080000
		 * DTSTART;TZID=(GMT+01.00) Sarajevo/Warsaw/Zagreb:20120305T104500
		 * DTSTART:20120308T093000Z
		 * DTSTART;VALUE=DATE:20120709
		 * DTSTART;TZID=Europe/Berlin:20130330T100000
		 */

		prop = calendarComponent.getProperty(Property.DTSTART);
		if (prop != null) {
			Parameter propTZ = prop.getParameter(Property.TZID);
			if (propTZ != null)
				mstrTimeZoneStart = propTZ.getValue();
			Result = getTimestamp(prop);
		}
		
		return Result;
	}

	private long getEndTime() {
		long Result = 0;
		Property propDtEnd;
		Property propDuration;
		
		propDtEnd = calendarComponent.getProperty(Property.DTEND);
		propDuration = calendarComponent.getProperty(Property.DURATION);
		if (propDtEnd != null) {
			Parameter propTZ = propDtEnd.getParameter(Property.TZID);
			if (propTZ != null)
				mstrTimeZoneEnd = propTZ.getValue();
			Result = getTimestamp(propDtEnd);

		} else if (propDuration != null) {
			Result = 0;
			long Start = this.getStartTime();
			String strDuration = propDuration.getValue();
			Dur dur = new Dur(strDuration);
			Result = Start 
					+ dur.getSeconds() 	* 1000 
					+ dur.getMinutes() 	* 60 * 1000 
					+ dur.getHours() 	* 60 * 60 * 1000 
					+ dur.getDays() 	* 60 * 60 * 24 * 1000;
			
			mstrTimeZoneEnd = mstrTimeZoneStart;
		}

		
		return Result;
	}
	
	private String getTimeZoneStart() {
		String Result = "GMT";
		
		if (!mstrTimeZoneStart.equals("")) {
			Result = mstrTimeZoneStart;
		}
		
		return Result;
	}

	private String getTimeZoneEnd() {
		String Result = "GMT";
		
		if (!mstrTimeZoneEnd.equals("")) {
			Result = mstrTimeZoneEnd;
		}
		
		return Result;
	}

	
	private String getDuration() {
		String Result = "";
		Property prop = calendarComponent.getProperty("DURATION");
		
		if (prop != null) {
			//DURATION:PT1H
			Result = prop.getValue();
		} else {
			// no DURATION given by this event. we have to calculate it by ourself
			Result = "P";
			long Start = this.getStartTime();
			long End   = this.getEndTime();
			long Duration = (End - Start) / 1000; // get rid of the milliseconds, they cann't be described with RFC2445-Duration
			
			int Days = (int) Math.ceil(Duration / 24 / 60 / 60);
			int Hours = (int) Math.ceil((Duration - (Days * 24 * 60 * 60)) / 60 / 60);
			int Minutes = (int) Math.ceil((Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60)) / 60);
			int Seconds = (int) (Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60) - (Minutes * 60));
		
			if (Days > 0)
				Result += String.valueOf(Days) + "D";
			
			if (!mAllDay) {
				//if a ALL_DAY event occurs, there is no need for hours, minutes and seconds (Android doesn't understand them)
				Result += "T";
				Result += String.valueOf(Hours) + "H";
				Result += String.valueOf(Minutes) + "M";
				Result += String.valueOf(Seconds) + "S";
			}
		}
		
		return Result;
	}
	
	private int getAllDay() {
		int Result = 0;

		if (mAllDay)
			Result = 1;
		
		return Result;
	}

	/**
	 * opens the first items of the event
	 * @return success of this function
	 * @see AndroidEvent#createIcs()
	 * @see CalendarEvent#fetchBody()
	 * @throws CaldavProtocolException
	 * @throws IOException
	 * @throws ParserException
	 */
	public boolean parseIcs() throws CaldavProtocolException, IOException, ParserException {
		boolean Error = false;
		
		CalendarBuilder builder = new CalendarBuilder();
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

		StringReader reader = new StringReader(this.stringIcs);
		this.calendar = builder.build(reader);
		
		ComponentList components = null;
		components = this.calendar.getComponents(Component.VEVENT);
		if (components.size() == 0) {
			components = this.calendar.getComponents(Component.VTODO);
			if (components.size() == 0) {
				throw new CaldavProtocolException("unknown events in ICS");
			} else {
				Log.e(TAG, "unsupported event TODO in ICS");
				Error = true;
			}
		} else if (components.size() > 1) {
			Log.e(TAG, "Several events in ICS -> only first will be processed");
		}

		// get the TimeZone information
		Component mCom = this.calendar.getComponent(Component.VTIMEZONE);
		if (mCom != null)
			mVTimeZone = (VTimeZone) this.calendar.getComponent(Component.VTIMEZONE);
		if (mVTimeZone != null)
			mTimeZone = new TimeZone(mVTimeZone);

		if (!Error)
			calendarComponent = (Component) components.get(0);
		
			 
		return !Error;
	}
}

