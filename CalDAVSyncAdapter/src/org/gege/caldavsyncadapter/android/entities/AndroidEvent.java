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

package org.gege.caldavsyncadapter.android.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;

public class AndroidEvent {
	public static String ceTAG = Events.SYNC_DATA1;
	
	private Uri muri;
	private Uri mcalendarUri;
	public ContentValues ContentValues = new ContentValues();	
	
	public AndroidEvent(Uri uri, Uri calendarUri) {
		super();
		muri = uri;
		mcalendarUri = calendarUri;
	}
	
	public Uri getUri() {
		return muri;
	}

	public Uri getCalendarUri() {
		return mcalendarUri;
	}

	public String getETag() {
		String Result = "";
		if (this.ContentValues.containsKey(ceTAG))
			Result = this.ContentValues.getAsString(ceTAG);
		return Result;
	}

	public void setETag(String eTag) {
		this.ContentValues.put(ceTAG, eTag);
	}

	@Override
	public String toString() {
		return muri.toString();
	}

	public boolean readContentValues(Cursor cur) {
		this.setETag(cur.getString(cur.getColumnIndex(AndroidEvent.ceTAG)));

		this.ContentValues.put(Events.EVENT_TIMEZONE, cur.getString(cur.getColumnIndex(Events.EVENT_TIMEZONE)));
		this.ContentValues.put(Events.EVENT_END_TIMEZONE, cur.getString(cur.getColumnIndex(Events.EVENT_END_TIMEZONE)));
		this.ContentValues.put(Events.DTSTART, cur.getLong(cur.getColumnIndex(Events.DTSTART)));
		this.ContentValues.put(Events.DTEND, cur.getLong(cur.getColumnIndex(Events.DTEND)));
		this.ContentValues.put(Events.ALL_DAY, cur.getLong(cur.getColumnIndex(Events.ALL_DAY)));
		this.ContentValues.put(Events.TITLE, cur.getString(cur.getColumnIndex(Events.TITLE)));
		this.ContentValues.put(Events.CALENDAR_ID, cur.getString(cur.getColumnIndex(Events.CALENDAR_ID)));
		this.ContentValues.put(Events._SYNC_ID, cur.getString(cur.getColumnIndex(Events._SYNC_ID)));
		//this.ContentValues.put(Events.SYNC_DATA1, cur.getString(cur.getColumnIndex(Events.SYNC_DATA1))); //not needed here, eTag has already been read
		this.ContentValues.put(Events.DESCRIPTION, cur.getString(cur.getColumnIndex(Events.DESCRIPTION)));
		this.ContentValues.put(Events.EVENT_LOCATION, cur.getString(cur.getColumnIndex(Events.EVENT_LOCATION)));
		this.ContentValues.put(Events.ACCESS_LEVEL, cur.getInt(cur.getColumnIndex(Events.ACCESS_LEVEL)));
		
		this.ContentValues.put(Events.STATUS, cur.getInt(cur.getColumnIndex(Events.STATUS)));
		
		this.ContentValues.put(Events.LAST_DATE, cur.getInt(cur.getColumnIndex(Events.LAST_DATE)));
		this.ContentValues.put(Events.DURATION, cur.getString(cur.getColumnIndex(Events.DURATION)));

		this.ContentValues.put(Events.RDATE, cur.getString(cur.getColumnIndex(Events.RDATE)));
		this.ContentValues.put(Events.RRULE, cur.getString(cur.getColumnIndex(Events.RRULE)));
		this.ContentValues.put(Events.EXRULE, cur.getString(cur.getColumnIndex(Events.EXRULE)));
		this.ContentValues.put(Events.EXDATE, cur.getString(cur.getColumnIndex(Events.EXDATE)));		
		this.ContentValues.put(Events.DIRTY, cur.getInt(cur.getColumnIndex(Events.DIRTY)));
		
		return true;
	}
	
	public java.util.ArrayList<String> getComparableItems() {
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
		Result.add(AndroidEvent.ceTAG);
		Result.add(Events.DESCRIPTION);
		Result.add(Events.EVENT_LOCATION);
		Result.add(Events.ACCESS_LEVEL);
		Result.add(Events.STATUS);
		Result.add(Events.RDATE);
		Result.add(Events.RRULE);
		Result.add(Events.EXRULE);
		Result.add(Events.EXDATE);
		
		return Result;
	}
}

