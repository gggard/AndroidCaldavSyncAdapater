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
package org.gege.caldavsyncadapter;

import android.content.ContentValues;
import android.provider.CalendarContract.Events;
import android.util.Log;

/**
 * abstract class for Calendar and Android events
 */
abstract public class Event {

    /**
     * stores the ETAG of an event
     */
    public final static String ETAG = Events.SYNC_DATA1;

    private static final String TAG = "Event";

    /**
     * internal Tag used to identify deleted events
     */
    public static String INTERNALTAG = Events.SYNC_DATA2;

    /**
     * store the whole VEVENT in here
     * missing TAGs they might be missing for google update
     *
     * CREATED:20130906T102857Z
     * DTSTAMP:20130906T102857Z
     * LAST-MODIFIED:20130906T102857Z
     * SEQUENCE:0
     */
    public static String RAWDATA = Events.SYNC_DATA3;

    /**
     * stores the UID of an Event
     * example: UID:e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter
     */
    public static String UID = Events.SYNC_DATA4;

    /**
     * the event transformed into ContentValues
     */
    public ContentValues ContentValues = new ContentValues();

    /**
     * returns a list of all items that are comparable with this sync adapter
     *
     * @return a list of all items that are comparable with this sync adapter
     */
    public static java.util.ArrayList<String> getComparableItems() {
        java.util.ArrayList<String> Result = new java.util.ArrayList<String>();
        Result.add(Events.DTSTART);
        Result.add(Events.DTEND);
        Result.add(Events.EVENT_TIMEZONE);
        Result.add(Events.EVENT_END_TIMEZONE);
        Result.add(Events.ALL_DAY);
        Result.add(Events.DURATION);
        Result.add(Events.TITLE);
        Result.add(Events.CALENDAR_ID);
        Result.add(Events._SYNC_ID);
        //Result.add(Events.SYNC_DATA1);
        Result.add(ETAG);
        Result.add(Events.DESCRIPTION);
        Result.add(Events.EVENT_LOCATION);
        Result.add(Events.ACCESS_LEVEL);
        Result.add(Events.STATUS);
        Result.add(Events.RDATE);
        Result.add(Events.RRULE);
        Result.add(Events.EXRULE);
        Result.add(Events.EXDATE);
        Result.add(UID);

        return Result;
    }

    abstract public String getETag();

    abstract public void setETag(String ETag);

    /**
     * returns the AndroidCalendarId for this event.
     *
     * @return the AndroidCalendarId for this event
     */
    public long getAndroidCalendarId() {
        long Result = -1;
        if (this.ContentValues.containsKey(Events.CALENDAR_ID)) {
            Result = this.ContentValues.getAsLong(Events.CALENDAR_ID);
        }
        return Result;
    }

    /**
     * sets the AndroidCalendarId for this event
     *
     * @param ID the AndroidCalendarId for this event
     */
    public void setAndroidCalendarId(long ID) {
        if (this.ContentValues.containsKey(Events.CALENDAR_ID)) {
            this.ContentValues.remove(Events.CALENDAR_ID);
        }

        this.ContentValues.put(Events.CALENDAR_ID, ID);
    }

    /**
     * returns the UID for this event. you can also check, whether the UID was stored from server.
     * the V1.7 release and before didn't save them.
     * example: UID:e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter
     *
     * @return the UID for this event
     */
    public String getUID() {
        String Result = "";
        if (this.ContentValues.containsKey(UID)) {
            Result = this.ContentValues.getAsString(UID);
        }

        return Result;
    }

    /**
     * compares the given ContentValues with the current ones for differences
     *
     * @param calendarEventValues the contentValues of the calendar event
     * @return if the events are different
     */
    public boolean checkEventValuesChanged(ContentValues calendarEventValues) {
        boolean Result = false;
        Object ValueAndroid = null;
        Object ValueCalendar = null;
        java.util.ArrayList<String> CompareItems = Event.getComparableItems();

        for (String Key : CompareItems) {

            if (this.ContentValues.containsKey(Key)) {
                ValueAndroid = this.ContentValues.get(Key);
            } else {
                ValueAndroid = null;
            }

            if (calendarEventValues.containsKey(Key)) {
                ValueCalendar = calendarEventValues.get(Key);
            } else {
                ValueCalendar = null;
            }

			/*
                         * TODO: Sync is designed to "Server always wins", should be a general option for this adapter
			 */
            if (ValueAndroid != null) {
                if (ValueCalendar != null) {
                    if (!ValueAndroid.toString().equals(ValueCalendar.toString())) {
                        Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString()
                                + " <> " + ValueCalendar.toString());
                        this.ContentValues.put(Key, ValueCalendar.toString());
                        Result = true;
                    }
                } else {
                    Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString()
                            + " <> null");
                    this.ContentValues.putNull(Key);
                    Result = true;
                }
            } else {
                if (ValueCalendar != null) {
                    Log.d(TAG, "difference in " + Key.toString() + ":null <> " + ValueCalendar
                            .toString());
                    this.ContentValues.put(Key, ValueCalendar.toString());
                    Result = true;
                } else {
                    // both null -> this is ok
                }
            }
        }

        return Result;
    }
}
