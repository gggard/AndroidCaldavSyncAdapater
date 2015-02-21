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
import org.gege.caldavsyncadapter.Event;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
import org.gege.caldavsyncadapter.caldav.xml.MultiStatusHandler;
import org.gege.caldavsyncadapter.caldav.xml.sax.MultiStatus;
import org.gege.caldavsyncadapter.caldav.xml.sax.Prop;
import org.gege.caldavsyncadapter.caldav.xml.sax.PropStat;
import org.gege.caldavsyncadapter.caldav.xml.sax.Response;
import org.gege.caldavsyncadapter.syncadapter.SyncAdapter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SyncStats;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.jdevel.acaldav.App;


public class CalendarEvent extends org.gege.caldavsyncadapter.Event {

    private static final String TAG = "CalendarEvent";

    public URL calendarURL;

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

    private Account mAccount = null;

    private ContentProviderClient mProvider = null;

    private String mstrcIcalPropertyError = "net.fortunal.ical4j.invalid:";

    public CalendarEvent(Account account, ContentProviderClient provider) {
        this.mAccount = account;
        this.mProvider = provider;
    }

    private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }

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

    public boolean setICSasMultiStatus(String stringMultiStatus) {
        boolean Result = false;
        String ics = "";
        MultiStatus multistatus;
        ArrayList<Response> responselist;
        Response response;
        PropStat propstat;
        Prop prop;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            MultiStatusHandler contentHandler = new MultiStatusHandler();
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(new StringReader(stringMultiStatus)));

            multistatus = contentHandler.mMultiStatus;
            if (multistatus != null) {
                responselist = multistatus.ResponseList;
                if (responselist.size() == 1) {
                    response = responselist.get(0);
                    //HINT: bugfix for google calendar, zimbra replace("@", "%40")
                    if (response.href.replace("@", "%40")
                            .equals(this.getUri().getRawPath().replace("@", "%40"))) {
                        propstat = response.propstat;
                        if (propstat != null) {
                            if (propstat.status.contains("200 OK")) {
                                prop = propstat.prop;
                                ics = prop.calendardata;
                                this.setETag(prop.getetag);
                                Result = true;
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.stringIcs = ics;
        return Result;
    }

    /**
     * gets the Uri of the android event
     */
    public Uri getAndroidEventUri() {
        return mAndroidEventUri;
    }

    /**
     * sets the Uri of the android event
     *
     * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#createAndroidEvent(ContentProviderClient
     * provider, Account account, Uri calendarUri, CalendarEvent calendarEvent)
     * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#updateAndroidEvent(ContentProviderClient
     * provider, Account account, AndroidEvent androidEvent, CalendarEvent calendarEvent)
     */
    public void setAndroidEventUri(Uri uri) {
        mAndroidEventUri = uri;
    }

    /**
     * reads all ContentValues from the caldav source
     *
     * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#createAndroidEvent(ContentProviderClient
     * provider, Account account, Uri calendarUri, CalendarEvent calendarEvent)
     * @see org.gege.caldavsyncadapter.syncadapter.SyncAdapter#updateAndroidEvent(ContentProviderClient
     * provider, Account account, AndroidEvent androidEvent, CalendarEvent calendarEvent)
     */
    public boolean readContentValues() {
        this.ContentValues.put(Events.DTSTART, this.getStartTime());
        this.ContentValues.put(Events.EVENT_TIMEZONE, this.getTimeZoneStart());

        //if (this.getRRule().isEmpty() && this.getRDate().isEmpty()) {
        if (this.getRRule() == null && this.getRDate() == null) {
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
        this.ContentValues.put(ETAG, this.getETag());
        this.ContentValues.put(Events.DESCRIPTION, this.getDescription());
        this.ContentValues.put(Events.EVENT_LOCATION, this.getLocation());
        this.ContentValues.put(Events.ACCESS_LEVEL, this.getAccessLevel());
        this.ContentValues.put(Events.STATUS, this.getStatus());
        this.ContentValues.put(Events.RDATE, this.getRDate());
        this.ContentValues.put(Events.RRULE, this.getRRule());
        this.ContentValues.put(Events.EXRULE, this.getExRule());
        this.ContentValues.put(Events.EXDATE, this.getExDate());
        this.ContentValues.put(UID, this.getUid());
        this.ContentValues.put(RAWDATA, this.stringIcs);

        return true;
    }

    /**
     * receives a single event and parses its content
     *
     * @return success of this function
     * @see CalendarEvent#parseIcs()
     */
    public boolean fetchBody()
            throws ClientProtocolException, IOException, CaldavProtocolException, ParserException {
        boolean error = false;

        //replaced fetchEvent() with getEvent()
        //CaldavFacade.fetchEvent(this);
        CaldavFacade.getEvent(this);

        boolean parsed = this.parseIcs();
        if (!parsed) {
            error = true;
        }

        return !error;
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

                    int intDuration = Duration.getMinutes() + Duration.getHours() * 60
                            + Duration.getDays() * 60 * 24;

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
            for (Object objProperty : Propertys) {
                Property property = (Property) objProperty;
                Attendee = ReadAttendeeProperties(property, Property.ATTENDEE);
                if (Attendee != null) {
                    Result.add(Attendee);
                }
            }
        }
        Propertys = calendarComponent.getProperties(Property.ORGANIZER);
        if (Propertys != null) {
            for (Object objProperty : Propertys) {
                Property property = (Property) objProperty;
                Attendee = ReadAttendeeProperties(property, Property.ORGANIZER);
                if (Attendee != null) {
                    Result.add(Attendee);
                }
            }
        }

        return Result;
    }

    private ContentValues ReadAttendeeProperties(Property property, String Type) {
        ContentValues Attendee = null;

        ParameterList Parameters = property.getParameters();
        Parameter CN = Parameters.getParameter(Cn.CN);
        Parameter ROLE = Parameters.getParameter(Role.ROLE);
        Parameter CUTYPE = Parameters.getParameter(CuType.CUTYPE);
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

        if (CN != null) {
            strCN = CN.getValue();
        }
        if (ROLE != null) {
            strROLE = ROLE.getValue();
        }
        if (CUTYPE != null) {
            strCUTYPE = CUTYPE.getValue();
        }
        if (PARTSTAT != null) {
            strPARTSTAT = PARTSTAT.getValue();
        }

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

                if (strROLE.equals("OPT-PARTICIPANT")) {
                    Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_OPTIONAL);
                } else if (strROLE.equals("NON-PARTICIPANT")) {
                    Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_NONE);
                } else if (strROLE.equals("REQ-PARTICIPANT")) {
                    Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
                } else if (strROLE.equals("CHAIR")) {
                    Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
                } else {
                    Attendee.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_NONE);
                }

                if (Type.equals(Property.ATTENDEE)) {
                    Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ATTENDEE);
                } else if (Type.equals(Property.ORGANIZER)) {
                    Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_ORGANIZER);
                } else {
                    Attendee.put(Attendees.ATTENDEE_RELATIONSHIP, Attendees.RELATIONSHIP_NONE);
                }

                if (strPARTSTAT.equals(PartStat.NEEDS_ACTION.getValue())) {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_INVITED);
                } else if (strPARTSTAT.equals(PartStat.ACCEPTED.getValue())) {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_ACCEPTED);
                } else if (strPARTSTAT.equals(PartStat.DECLINED.getValue())) {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_DECLINED);
                } else if (strPARTSTAT.equals(PartStat.COMPLETED.getValue())) {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_NONE);
                } else if (strPARTSTAT.equals(PartStat.TENTATIVE.getValue())) {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_TENTATIVE);
                } else {
                    Attendee.put(Attendees.ATTENDEE_STATUS, Attendees.ATTENDEE_STATUS_INVITED);
                }

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
            if (Value.equals(Clazz.PUBLIC)) {
                Result = Events.ACCESS_PUBLIC;
            } else if (Value.equals(Clazz.PRIVATE)) {
                Result = Events.ACCESS_PRIVATE;
            } else if (Value.equals(Clazz.CONFIDENTIAL)) {
                Result
                        = Events.ACCESS_PRIVATE; // should be ACCESS_CONFIDENTIAL, but is not implemented within Android
            }
        }

        return Result;
    }

    private int getStatus() {
        int Result = -1;
        String Value = "";
        Property property = calendarComponent.getProperty(Property.STATUS);
        if (property != null) {
            Value = property.getValue();
            if (Value.equals(Status.VEVENT_CONFIRMED.getValue())) {
                Result = Events.STATUS_CONFIRMED;
            } else if (Value.equals(Status.VEVENT_CANCELLED.getValue())) {
                Result = Events.STATUS_CANCELED;
            } else if (Value.equals(Status.VEVENT_TENTATIVE.getValue())) {
                Result = Events.STATUS_TENTATIVE;
            }
        }

        return Result;
    }

    private String getDescription() {
        String Result = null;
        Property property = calendarComponent.getProperty(Property.DESCRIPTION);
        if (property != null) {
            Result = property.getValue();
        }

        return Result;
    }

    private String getLocation() {
        String Result = null;
        Property property = calendarComponent.getProperty(Property.LOCATION);
        if (property != null) {
            Result = property.getValue();
        }

        return Result;
    }

    private String getTitle() {
        Property property = calendarComponent.getProperty(Property.SUMMARY);
        if (property != null) {
            return property.getValue();
        } else {
            return "**unkonwn**";
        }
    }

    private String getRRule() {
        String Result = null;
        Property property = calendarComponent.getProperty(Property.RRULE);
        if (property != null) {
            Result = property.getValue();
        }

        return Result;
    }

    private String getExRule() {
        String Result = null;
        Property property = calendarComponent.getProperty(Property.EXRULE);
        if (property != null) {
            Result = property.getValue();
        }

        return Result;
    }

    private String getRDate() {
        String Result = null;

        java.util.ArrayList<String> ExDates = this.getRDates();
        for (String Value : ExDates) {
            if (Result == null) {
                Result = "";
            }
            if (!Result.isEmpty()) {
                Result += ",";
            }
            Result += Value;
        }

        return Result;
    }

    private java.util.ArrayList<String> getRDates() {
        java.util.ArrayList<String> Result = new java.util.ArrayList<String>();
        PropertyList Propertys = calendarComponent.getProperties(Property.RDATE);
        if (Propertys != null) {
            Property property;
            for (Object objProperty : Propertys) {
                property = (Property) objProperty;
                Result.add(property.getValue());
            }
        }

        return Result;
    }

    private String getExDate() {
        String Result = null;

        java.util.ArrayList<String> ExDates = this.getExDates();
        for (String Value : ExDates) {
            if (Result == null) {
                Result = "";
            }
            if (!Result.isEmpty()) {
                Result += ",";
            }
            Result += Value;
        }

        return Result;
    }

    private java.util.ArrayList<String> getExDates() {
        java.util.ArrayList<String> Result = new java.util.ArrayList<String>();
        PropertyList Propertys = calendarComponent.getProperties(Property.EXDATE);
        if (Propertys != null) {
            Property property;
            for (Object objProperty : Propertys) {
                property = (Property) objProperty;
                Result.add(property.getValue());
            }
        }

        return Result;
    }

    private String getUid() {
        String Result = "";
        Property prop = calendarComponent.getProperty(Property.UID);
        if (prop != null) {
            Result = prop.getValue();
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
            if (propTZ != null) {
                strTimeZone = propTZ.getValue();
            }

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
                    if (jtz.inDaylightTime(new java.util.Date(Result))) {
                        Result -= jtz.getDSTSavings();
                    }

                } else {
                    if (mTimeZone != null) {
                        // subtract the offset
                        Result -= mTimeZone.getRawOffset();

                        // remove dst
                        if (mTimeZone.inDaylightTime(new java.util.Date(Result))) {
                            Result -= mTimeZone.getDSTSavings();
                        }
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
            if (propTZ != null) {
                mstrTimeZoneStart = propTZ.getValue();
            }
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
            if (propTZ != null) {
                mstrTimeZoneEnd = propTZ.getValue();
            }
            Result = getTimestamp(propDtEnd);

        } else if (propDuration != null) {
            Result = 0;
            long Start = this.getStartTime();
            String strDuration = propDuration.getValue();
            Dur dur = new Dur(strDuration);
            Result = Start
                    + dur.getSeconds() * 1000
                    + dur.getMinutes() * 60 * 1000
                    + dur.getHours() * 60 * 60 * 1000
                    + dur.getDays() * 60 * 60 * 24 * 1000;

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
            long End = this.getEndTime();
            long Duration = 0;
            if (End != 0) {
                Duration = (End - Start)
                        / 1000; // get rid of the milliseconds, they cann't be described with RFC2445-Duration
            }

            if (Duration < 0) {
                Duration = 0;
            }
            int Days = (int) Math.ceil(Duration / 24 / 60 / 60);
            int Hours = (int) Math.ceil((Duration - (Days * 24 * 60 * 60)) / 60 / 60);
            int Minutes = (int) Math
                    .ceil((Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60)) / 60);
            int Seconds = (int) (Duration - (Days * 24 * 60 * 60) - (Hours * 60 * 60) - (Minutes
                    * 60));

            if (Days > 0) {
                Result += String.valueOf(Days) + "D";
            }

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

        if (mAllDay) {
            Result = 1;
        }

        return Result;
    }

    /**
     * opens the first items of the event
     *
     * @return success of this function
     * @see AndroidEvent#createIcs(String)
     * @see CalendarEvent#fetchBody()
     */
    private boolean parseIcs() throws CaldavProtocolException, IOException, ParserException {
        boolean Error = false;
        Thread.currentThread().setContextClassLoader(App.getContext().getClassLoader());
        CalendarBuilder builder = new CalendarBuilder();
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_VALIDATION, true);
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

        StringReader reader = new StringReader(this.stringIcs);
        try {
            this.calendar = builder.build(reader);
        } catch (ParserException ex) {
            // ical4j fails with this: "Cannot set timezone for UTC properties"
            // CREATED;TZID=America/New_York:20130129T140250
            Error = true;
        }

        if (!Error) {
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
            if (mCom != null) {
                mVTimeZone = (VTimeZone) this.calendar.getComponent(Component.VTIMEZONE);
            }
            if (mVTimeZone != null) {
                mTimeZone = new TimeZone(mVTimeZone);
            }

            if (!Error) {
                calendarComponent = (Component) components.get(0);
            }
        }

        return !Error;
    }

    /**
     * searches for an android event
     *
     * @return the android event
     */
    public AndroidEvent getAndroidEvent(DavCalendar androidCalendar) throws RemoteException {
        boolean Error = false;
        Uri uriEvents = Events.CONTENT_URI;
        Uri uriAttendee = Attendees.CONTENT_URI;
        Uri uriReminder = Reminders.CONTENT_URI;
        AndroidEvent androidEvent = null;

        String selection = "(" + Events._SYNC_ID + " = ?)";
        String[] selectionArgs = new String[]{this.getUri().toString()};
        Cursor curEvent = this.mProvider.query(uriEvents, null, selection, selectionArgs, null);

        Cursor curAttendee = null;
        Cursor curReminder = null;

        if (curEvent == null) {
            Error = true;
        }
        if (!Error) {
            if (curEvent.getCount() == 0) {
                Error = true;
            }
        }
        if (!Error) {
            curEvent.moveToNext();

            long EventID = curEvent.getLong(curEvent.getColumnIndex(Events._ID));
            Uri returnedUri = ContentUris.withAppendedId(uriEvents, EventID);

            //androidEvent = new AndroidEvent(this.mAccount, this.mProvider, returnedUri, androidCalendar.getAndroidCalendarUri());
            androidEvent = new AndroidEvent(returnedUri, androidCalendar.getAndroidCalendarUri());
            androidEvent.readContentValues(curEvent);

            selection = "(" + Attendees.EVENT_ID + " = ?)";
            selectionArgs = new String[]{String.valueOf(EventID)};
            curAttendee = this.mProvider.query(uriAttendee, null, selection, selectionArgs, null);
            selection = "(" + Reminders.EVENT_ID + " = ?)";
            selectionArgs = new String[]{String.valueOf(EventID)};
            curReminder = this.mProvider.query(uriReminder, null, selection, selectionArgs, null);
            androidEvent.readAttendees(curAttendee);
            androidEvent.readReminder(curReminder);
            curAttendee.close();
            curReminder.close();
        }
        curEvent.close();

        return androidEvent;
    }

    /**
     * creates a new androidEvent from a given calendarEvent
     *
     * @see {@link SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * DavCalendar, SyncStats)}
     */
    public boolean createAndroidEvent(DavCalendar androidCalendar)
            throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException,
            ParserException {
        boolean Result = false;
        boolean BodyFetched = this.fetchBody();
        int CountAttendees = 0;
        int CountReminders = 0;

        if (BodyFetched) {
            //calendarEvent.readContentValues(calendarUri);
            this.readContentValues();
            this.setAndroidCalendarId(ContentUris.parseId(androidCalendar.getAndroidCalendarUri()));

            Uri uri = this.mProvider.insert(asSyncAdapter(Events.CONTENT_URI, this.mAccount.name,
                    this.mAccount.type), this.ContentValues);
            this.setAndroidEventUri(uri);

            Log.d(TAG, "Creating calendar event for " + uri.toString());

            //check the attendees
            java.util.ArrayList<ContentValues> AttendeeList = this.getAttandees();
            for (ContentValues Attendee : AttendeeList) {
                this.mProvider.insert(Attendees.CONTENT_URI, Attendee);
                CountAttendees += 1;
            }

            //check the reminders
            java.util.ArrayList<ContentValues> ReminderList = this.getReminders();
            for (ContentValues Reminder : ReminderList) {
                this.mProvider.insert(Reminders.CONTENT_URI, Reminder);
                CountReminders += 1;
            }

            if ((CountAttendees > 0) || (CountReminders > 0)) {
                //the events gets dirty when attendees or reminders were added
                AndroidEvent androidEvent = this.getAndroidEvent(androidCalendar);

                androidEvent.ContentValues.put(Events.DIRTY, 0);
                int RowCount = this.mProvider
                        .update(asSyncAdapter(androidEvent.getUri(), this.mAccount.name,
                                this.mAccount.type), androidEvent.ContentValues, null, null);
                Result = (RowCount == 1);
            } else {
                Result = true;
            }


        }
        return Result;
    }

    /**
     * the android event is getting updated
     *
     * @see {@link SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * Calendar, SyncStats)}
     */
    public boolean updateAndroidEvent(AndroidEvent androidEvent)
            throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException,
            ParserException {
        boolean BodyFetched = this.fetchBody();
        int Rows = 0;

        if (BodyFetched) {
            this.readContentValues();
            this.setAndroidCalendarId(androidEvent.getAndroidCalendarId());
            this.setAndroidEventUri(androidEvent.getUri());

            Log.d(TAG, "AndroidEvent is dirty: " + androidEvent.ContentValues
                    .getAsString(Events.DIRTY));

            if (androidEvent.checkEventValuesChanged(this.ContentValues)) {
                // just set the raw data from server event into android event
                if (androidEvent.ContentValues.containsKey(Event.RAWDATA)) {
                    androidEvent.ContentValues.remove(Event.RAWDATA);
                }
                androidEvent.ContentValues
                        .put(Event.RAWDATA, this.ContentValues.getAsString(Event.RAWDATA));

                //update the attendees and reminders
                this.updateAndroidAttendees();
                this.updateAndroidReminder();

                androidEvent.ContentValues.put(Events.DIRTY, 0); // the event is now in sync
                Log.d(TAG, "Update calendar event: for " + androidEvent.getUri());

                Rows = mProvider
                        .update(asSyncAdapter(androidEvent.getUri(), mAccount.name, mAccount.type),
                                androidEvent.ContentValues, null, null);
                //Log.i(TAG, "Updated calendar event: rows effected " + Rows.toString());
            } else {
                Log.d(TAG, "Update calendar event not needed: for " + androidEvent.getUri());
            }
        }
        return (Rows == 1);
    }

    /**
     * updates the attendees from a calendarEvent to its androidEvent.
     * the calendarEvent has to know its androidEvent via {@link CalendarEvent#setAndroidEventUri(Uri)}
     *
     * @see SyncAdapter#updateAndroidEvent(ContentProviderClient, Account, AndroidEvent,
     * CalendarEvent)
     */
    private boolean updateAndroidAttendees() {
        boolean Result = false;

        try {
            String mSelectionClause = "(" + Attendees.EVENT_ID + " = ?)";
            String[] mSelectionArgs = {
                    Long.toString(ContentUris.parseId(this.getAndroidEventUri()))};
            int RowDelete;
            RowDelete = this.mProvider
                    .delete(Attendees.CONTENT_URI, mSelectionClause, mSelectionArgs);
            Log.d(TAG, "Attendees Deleted:" + String.valueOf(RowDelete));

            java.util.ArrayList<ContentValues> AttendeeList = this.getAttandees();
            for (ContentValues Attendee : AttendeeList) {
                this.mProvider.insert(Attendees.CONTENT_URI, Attendee);
            }
            Log.d(TAG, "Attendees Inserted:" + String.valueOf(AttendeeList.size()));
            Result = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return Result;
    }

    /**
     * update the reminders from a calendarEvent to its androidEvent.
     * the calendarEvent has to know its androidEvent via {@link CalendarEvent#setAndroidEventUri(Uri)}
     *
     * @see SyncAdapter#updateAndroidEvent(ContentProviderClient, Account, AndroidEvent,
     * CalendarEvent)
     */
    private boolean updateAndroidReminder() {
        boolean Result = false;

        try {
            String mSelectionClause = "(" + Reminders.EVENT_ID + " = ?)";
            String[] mSelectionArgs = {
                    Long.toString(ContentUris.parseId(this.getAndroidEventUri()))};
            int RowDelete;
            RowDelete = this.mProvider
                    .delete(Reminders.CONTENT_URI, mSelectionClause, mSelectionArgs);
            Log.d(TAG, "Reminders Deleted:" + String.valueOf(RowDelete));

            Uri ReminderUri;
            java.util.ArrayList<ContentValues> ReminderList = this.getReminders();
            for (ContentValues Reminder : ReminderList) {
                ReminderUri = this.mProvider.insert(Reminders.CONTENT_URI, Reminder);
                System.out.println(ReminderUri);
            }
            Log.d(TAG, "Reminders Inserted:" + String.valueOf(ReminderList.size()));

            Result = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return Result;
    }
}

