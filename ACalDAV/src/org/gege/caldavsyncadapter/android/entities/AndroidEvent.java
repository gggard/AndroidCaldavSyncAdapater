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
 */


package org.gege.caldavsyncadapter.android.entities;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;

import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;

import java.net.URISyntaxException;
import java.text.ParseException;

import de.jdevel.acaldav.App;

//import android.accounts.Account;
//import android.content.ContentProviderClient;
//import android.content.ContentValues;
//import android.content.SyncStats;
//import android.os.RemoteException;
//import android.provider.CalendarContract.Calendars;
//import org.gege.caldavsyncadapter.Event;
//import org.gege.caldavsyncadapter.caldav.CaldavFacade;
//import org.gege.caldavsyncadapter.caldav.entities.DavCalendar;
//import org.gege.caldavsyncadapter.syncadapter.SyncAdapter;

public class AndroidEvent extends org.gege.caldavsyncadapter.Event {

    private Uri muri;

    private Uri mAndroidCalendarUri;

    /**
     * the list of attendees
     */
    private PropertyList mAttendees = new PropertyList();

    /**
     * the list of reminders
     */
    private ComponentList mReminders = new ComponentList();

    private Calendar mCalendar = null;

/*	private Account mAccount = null;
    private ContentProviderClient mProvider = null;*/

    //public AndroidEvent(Account account, ContentProviderClient provider, Uri uri, Uri calendarUri) {
    public AndroidEvent(Uri uri, Uri calendarUri) {
        super();
        this.setUri(uri);
/*		this.mAccount = account;
        this.mProvider = provider;*/
        //this.setCounterpartUri(calendarUri);
        mAndroidCalendarUri = calendarUri;
    }

    public Calendar getIcsEvent() {
        return mCalendar;
    }

    public String getETag() {
        String result = "";
        if (this.ContentValues.containsKey(ETAG)) {
            result = this.ContentValues.getAsString(ETAG);
        }
        return result;
    }

    public void setETag(String eTag) {
        this.ContentValues.put(ETAG, eTag);
    }

    public Uri getUri() {
        return muri;
    }

    public void setUri(Uri uri) {
        this.muri = uri;
    }

    public Uri getAndroidCalendarUri() {
        return mAndroidCalendarUri;
    }

    @Override
    public String toString() {
        return this.getUri().toString();
    }

    /**
     * reads an android event from a given cursor into {@link AndroidEvent#ContentValues}
     *
     * @param cur the cursor with the event
     * @return success of this funtion
     * @see AndroidEvent#ContentValues
     */
    public boolean readContentValues(Cursor cur) {
        this.setETag(cur.getString(cur.getColumnIndex(ETAG)));

        this.ContentValues.put(Events.EVENT_TIMEZONE,
                cur.getString(cur.getColumnIndex(Events.EVENT_TIMEZONE)));
        this.ContentValues.put(Events.EVENT_END_TIMEZONE,
                cur.getString(cur.getColumnIndex(Events.EVENT_END_TIMEZONE)));
        this.ContentValues.put(Events.DTSTART, cur.getLong(cur.getColumnIndex(Events.DTSTART)));
        this.ContentValues.put(Events.DTEND, cur.getLong(cur.getColumnIndex(Events.DTEND)));
        this.ContentValues.put(Events.ALL_DAY, cur.getLong(cur.getColumnIndex(Events.ALL_DAY)));
        this.ContentValues.put(Events.TITLE, cur.getString(cur.getColumnIndex(Events.TITLE)));
        this.ContentValues
                .put(Events.CALENDAR_ID, cur.getString(cur.getColumnIndex(Events.CALENDAR_ID)));
        this.ContentValues.put(Events._SYNC_ID, cur.getString(cur.getColumnIndex(Events._SYNC_ID)));
        //this.ContentValues.put(Events.SYNC_DATA1, cur.getString(cur.getColumnIndex(Events.SYNC_DATA1))); //not needed here, eTag has already been read
        this.ContentValues
                .put(Events.DESCRIPTION, cur.getString(cur.getColumnIndex(Events.DESCRIPTION)));
        this.ContentValues.put(Events.EVENT_LOCATION,
                cur.getString(cur.getColumnIndex(Events.EVENT_LOCATION)));
        this.ContentValues
                .put(Events.ACCESS_LEVEL, cur.getInt(cur.getColumnIndex(Events.ACCESS_LEVEL)));

        this.ContentValues.put(Events.STATUS, cur.getInt(cur.getColumnIndex(Events.STATUS)));

        this.ContentValues.put(Events.LAST_DATE, cur.getInt(cur.getColumnIndex(Events.LAST_DATE)));
        this.ContentValues.put(Events.DURATION, cur.getString(cur.getColumnIndex(Events.DURATION)));

        this.ContentValues.put(Events.RDATE, cur.getString(cur.getColumnIndex(Events.RDATE)));
        this.ContentValues.put(Events.RRULE, cur.getString(cur.getColumnIndex(Events.RRULE)));
        this.ContentValues.put(Events.EXRULE, cur.getString(cur.getColumnIndex(Events.EXRULE)));
        this.ContentValues.put(Events.EXDATE, cur.getString(cur.getColumnIndex(Events.EXDATE)));
        this.ContentValues.put(Events.DIRTY, cur.getInt(cur.getColumnIndex(Events.DIRTY)));
        this.ContentValues.put(UID, cur.getString(cur.getColumnIndex(UID)));
        this.ContentValues.put(RAWDATA, cur.getString(cur.getColumnIndex(RAWDATA)));

        return true;
    }

    /**
     * reads the attendees from a given cursor
     *
     * @param cur the cursor with the attendees
     * @return success of this function
     * @see AndroidEvent#mAttendees
     */
    public boolean readAttendees(Cursor cur) {
        Attendee attendee = null;
        Organizer organizer = null;
        ParameterList paraList = null;

        String name = "";
        Cn cn = null;

        String email = "";

        int relationship = 0;

        int status = 0;
        PartStat partstat = null;

        int type = 0;
        Role role = null;

        try {
            while (cur.moveToNext()) {
                name = cur.getString(cur.getColumnIndex(Attendees.ATTENDEE_NAME));
                email = cur.getString(cur.getColumnIndex(Attendees.ATTENDEE_EMAIL));
                relationship = cur.getInt(cur.getColumnIndex(Attendees.ATTENDEE_RELATIONSHIP));
                type = cur.getInt(cur.getColumnIndex(Attendees.ATTENDEE_TYPE));
                status = cur.getInt(cur.getColumnIndex(Attendees.ATTENDEE_STATUS));

                if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
                    organizer = new Organizer();
                    organizer.setValue("mailto:" + email);
                    paraList = organizer.getParameters();
                    mAttendees.add(organizer);
                } else {
                    attendee = new Attendee();
                    attendee.setValue("mailto:" + email);
                    paraList = attendee.getParameters();
                    mAttendees.add(attendee);
                }

                Rsvp rsvp = new Rsvp(true);
                paraList.add(rsvp);

                cn = new Cn(name);
                paraList.add(cn);

                if (status == Attendees.ATTENDEE_STATUS_INVITED) {
                    partstat = new PartStat(PartStat.NEEDS_ACTION.getValue());
                } else if (status == Attendees.ATTENDEE_STATUS_ACCEPTED) {
                    partstat = new PartStat(PartStat.ACCEPTED.getValue());
                } else if (status == Attendees.ATTENDEE_STATUS_DECLINED) {
                    partstat = new PartStat(PartStat.DECLINED.getValue());
                } else if (status == Attendees.ATTENDEE_STATUS_NONE) {
                    partstat = new PartStat(PartStat.COMPLETED.getValue());
                } else if (status == Attendees.ATTENDEE_STATUS_TENTATIVE) {
                    partstat = new PartStat(PartStat.TENTATIVE.getValue());
                } else {
                    partstat = new PartStat(PartStat.NEEDS_ACTION.getValue());
                }
                paraList.add(partstat);

                if (type == Attendees.TYPE_OPTIONAL) {
                    role = new Role(Role.OPT_PARTICIPANT.getValue());
                } else if (type == Attendees.TYPE_NONE) {
                    role = new Role(Role.NON_PARTICIPANT
                            .getValue()); //regular participants in android are non required?
                } else if (type == Attendees.TYPE_REQUIRED) {
                    role = new Role(Role.REQ_PARTICIPANT.getValue());
                } else {
                    role = new Role(Role.NON_PARTICIPANT.getValue());
                }
                paraList.add(role);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * reads the reminders from a given cursor
     *
     * @param cur the cursor with the reminders
     * @return success of this function
     */
    public boolean readReminder(Cursor cur) {
        int method;
        int minutes;
        VAlarm reminder;
        while (cur.moveToNext()) {
            reminder = new VAlarm();
            method = cur.getInt(cur.getColumnIndex(Reminders.METHOD));
            minutes = cur.getInt(cur.getColumnIndex(Reminders.MINUTES)) * -1;

            Dur dur = new Dur(0, 0, minutes, 0);
            Trigger tri = new Trigger(dur);
            Value val = new Value(Duration.DURATION);
            tri.getParameters().add(val);
            reminder.getProperties().add(tri);

            Description desc = new Description();
            desc.setValue("caldavsyncadapter standard description");
            reminder.getProperties().add(desc);

            if (method == Reminders.METHOD_EMAIL) {
                reminder.getProperties().add(Action.EMAIL);
            } else {
                reminder.getProperties().add(Action.DISPLAY);
            }

            this.mReminders.add(reminder);
        }
        return true;
    }

    /**
     * generates a new ics-file.
     * uses {@link AndroidEvent#ContentValues} as source.
     * this should only be used when a new event has been generated within android.
     *
     * @param strUid the UID for this event. example: UID:e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter
     * @return success of the function
     * @see CalendarEvent#fetchBody()
     */
    public boolean createIcs(String strUid) {
        boolean result = false;
        TimeZone timeZone = null;
        Thread.currentThread().setContextClassLoader(App.getContext().getClassLoader());
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
//TODO: do not simply create the ics-file new. take into account the RAWDATA if available
                /*
                 * dtstart=1365598800000
		 * dtend=1365602400000
		 * eventTimezone=Europe/Berlin
		 * eventEndTimezone=null
		 * duration=null
		 * allDay=0
		 * rrule=null
		 * rdate=null
		 * exrule=null
		 * exdate=null
		 * title=Einurlner Termin
		 * description=null
		 * eventLocation=null
		 * accessLevel=0
		 * eventStatus=0
		 * 
		 * calendar_id=4
		 * lastDate=-197200128
		 * sync_data1=null
		 * _sync_id=null
		 * dirty=1
		 */

        try {
            mCalendar = new Calendar();
            PropertyList propCalendar = mCalendar.getProperties();
            propCalendar.add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
            propCalendar.add(Version.VERSION_2_0);
            propCalendar.add(CalScale.GREGORIAN);

            VEvent event = new VEvent();
            mCalendar.getComponents().add(event);
            PropertyList propEvent = event.getProperties();

            // DTSTAMP -> is created by new VEvent() automatical
            //na

            // CREATED
            //na

            // LAST-MODIFIED
            //na

            // SEQUENCE
            //na

            // DTSTART
            long lngStart = this.ContentValues.getAsLong(Events.DTSTART);
            String strTZStart = this.ContentValues.getAsString(Events.EVENT_TIMEZONE);
            boolean allDay = this.ContentValues.getAsBoolean(Events.ALL_DAY);
            if (lngStart > 0) {
                DtStart dtStart = new DtStart();
                if (allDay) {
                    Date dateStart = new Date();
                    dateStart.setTime(lngStart);
                    dtStart.setDate(dateStart);
                } else {
                    DateTime datetimeStart = new DateTime();
                    datetimeStart.setTime(lngStart);
                    dtStart.setDate(datetimeStart);

                    timeZone = registry.getTimeZone(strTZStart);
                    if (timeZone == null) {
                        java.util.TimeZone systemTimeZone = TimeZone.getTimeZone(strTZStart);
                        if (systemTimeZone == null) {
                            systemTimeZone = TimeZone.getDefault();
                        }
                        timeZone = registry.getTimeZone(systemTimeZone.getID());
                    }
                    dtStart.setTimeZone(timeZone);

                    // no timezone information for allDay events
                    mCalendar.getComponents().add(timeZone.getVTimeZone());
                }
                propEvent.add(dtStart);
            }

            // DTEND
            long lngEnd = this.ContentValues.getAsLong(Events.DTEND);
            String strTZEnd = this.ContentValues.getAsString(Events.EVENT_END_TIMEZONE);
            if (strTZEnd == null) {
                strTZEnd = strTZStart;
            }
            if (lngEnd > 0) {
                DtEnd dtEnd = new DtEnd();
                if (allDay) {
                    Date dateEnd = new Date();
                    dateEnd.setTime(lngEnd);
                    dtEnd.setDate(dateEnd);
                } else {
                    DateTime datetimeEnd = new DateTime();
                    datetimeEnd.setTime(lngEnd);
                    dtEnd.setDate(datetimeEnd);
                    if (strTZEnd != null) {
                        timeZone = registry.getTimeZone(strTZEnd);
                    }
                    dtEnd.setTimeZone(timeZone);
                }
                propEvent.add(dtEnd);
            }

            // DURATION
            if (this.ContentValues.containsKey(Events.DURATION)) {
                String strDuration = this.ContentValues.getAsString(Events.DURATION);
                if (strDuration != null) {
                    Duration duration = new Duration();
                    duration.setValue(strDuration);

                    propEvent.add(duration);
                }
            }

            //RRULE
            if (this.ContentValues.containsKey(Events.RRULE)) {
                String strRrule = this.ContentValues.getAsString(Events.RRULE);
                if (strRrule != null) {
                    if (!strRrule.equals("")) {
                        RRule rrule = new RRule();
                        rrule.setValue(strRrule);
                        propEvent.add(rrule);
                    }
                }
            }

            //RDATE
            if (this.ContentValues.containsKey(Events.RDATE)) {
                String strRdate = this.ContentValues.getAsString(Events.RDATE);
                if (strRdate != null) {
                    if (!strRdate.equals("")) {
                        RDate rdate = new RDate();
                        rdate.setValue(strRdate);
                        propEvent.add(rdate);
                    }
                }
            }

            //EXRULE
            if (this.ContentValues.containsKey(Events.EXRULE)) {
                String strExrule = this.ContentValues.getAsString(Events.EXRULE);
                if (strExrule != null) {
                    if (!strExrule.equals("")) {
                        ExRule exrule = new ExRule();
                        exrule.setValue(strExrule);
                        propEvent.add(exrule);
                    }
                }
            }

            //EXDATE
            if (this.ContentValues.containsKey(Events.EXDATE)) {
                String strExdate = this.ContentValues.getAsString(Events.EXDATE);
                if (strExdate != null) {
                    if (!strExdate.equals("")) {
                        ExDate exdate = new ExDate();
                        exdate.setValue(strExdate);
                        propEvent.add(exdate);
                    }
                }
            }

            //SUMMARY
            if (this.ContentValues.containsKey(Events.TITLE)) {
                String strTitle = this.ContentValues.getAsString(Events.TITLE);
                if (strTitle != null) {
                    Summary summary = new Summary(strTitle);
                    propEvent.add(summary);
                }
            }

            //DESCIPTION
            if (this.ContentValues.containsKey(Events.DESCRIPTION)) {
                String strDescription = this.ContentValues.getAsString(Events.DESCRIPTION);
                if (strDescription != null) {
                    if (!strDescription.equals("")) {
                        Description description = new Description(strDescription);
                        propEvent.add(description);
                    }
                }
            }

            //LOCATION
            if (this.ContentValues.containsKey(Events.EVENT_LOCATION)) {
                String strLocation = this.ContentValues.getAsString(Events.EVENT_LOCATION);
                if (strLocation != null) {
                    if (!strLocation.equals("")) {
                        Location location = new Location(strLocation);
                        propEvent.add(location);
                    }
                }
            }

            //CLASS / ACCESS_LEVEL
            if (this.ContentValues.containsKey(Events.ACCESS_LEVEL)) {
                int accessLevel = this.ContentValues.getAsInteger(Events.ACCESS_LEVEL);
                Clazz clazz = new Clazz();
                if (accessLevel == Events.ACCESS_PUBLIC) {
                    clazz.setValue(Clazz.PUBLIC.getValue());
                } else if (accessLevel == Events.ACCESS_PRIVATE) {
                    clazz.setValue(Clazz.PRIVATE.getValue());
                } else if (accessLevel == Events.ACCESS_CONFIDENTIAL) {
                    clazz.setValue(Clazz.CONFIDENTIAL.getValue());
                } else {
                    clazz.setValue(Clazz.PUBLIC.getValue());
                }

                propEvent.add(clazz);
            }

            //STATUS
            if (this.ContentValues.containsKey(Events.STATUS)) {
                int intStatus = this.ContentValues.getAsInteger(Events.STATUS);
                if (intStatus > -1) {
                    Status status = new Status();
                    if (intStatus == Events.STATUS_CANCELED) {
                        status.setValue(Status.VEVENT_CANCELLED.getValue());
                    } else if (intStatus == Events.STATUS_CONFIRMED) {
                        status.setValue(Status.VEVENT_CONFIRMED.getValue());
                    } else if (intStatus == Events.STATUS_TENTATIVE) {
                        status.setValue(Status.VEVENT_TENTATIVE.getValue());
                    }

                    propEvent.add(status);
                }
            }

            //UID
            Uid uid = new Uid(strUid);
            propEvent.add(uid);

            // Attendees
            if (mAttendees.size() > 0) {
                for (Object objProp : mAttendees) {
                    Property prop = (Property) objProp;
                    propEvent.add(prop);
                }
            }

            // Reminders
            if (mReminders.size() > 0) {
                for (Object objComp : mReminders) {
                    Component com = (Component) objComp;
                    event.getAlarms().add(com);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * marks the android event as already handled
     *
     * @see AndroidEvent#cInternalTag
     * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * DavCalendar, SyncStats)
     */
//    public boolean tagAndroidEvent() throws RemoteException {
//
//        ContentValues values = new ContentValues();
//        values.put(Event.INTERNALTAG, 1);
//
//        int RowCount = this.mProvider
//                .update(asSyncAdapter(this.getUri(), this.mAccount.name, this.mAccount.type),
//                        values, null, null);
//        //Log.e(TAG,"Rows updated: " + RowCount.toString());
//
//        return (RowCount == 1);
//    }

/*	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
            return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }*/
}

