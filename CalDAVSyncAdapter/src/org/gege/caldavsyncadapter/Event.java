package org.gege.caldavsyncadapter;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.CalendarContract.Events;

abstract public class Event {
	/**
	 * stores the ETAG of an event
	 */
	public static String ceTAG = Events.SYNC_DATA1;
	/**
	 * internal Tag used to identify deleted events 
	 */
	public static String cInternalTag = Events.SYNC_DATA2;
	/**
	 * for a proper sync the raw ics-file should be stored here 
	 */
	public static String cRawData = Events.SYNC_DATA3;

	/**
	 * the event transformed into ContentValues
	 */
	public ContentValues ContentValues = new ContentValues();
	
	abstract public String getETag();
	abstract public void setETag(String ETag);

	private Uri mcounterpartUri;
	/**
	 * represents the Uri of its counterpart.
	 * the android event stores its calendar event Uri.
	 * the calendar event stores its android event Uri.
	 * @param uri
	 */
	public void setCounterpartUri(Uri uri) {
		mcounterpartUri = uri;
	}
	/**
	 * represents the Uri of its counterpart
	 * the android event stores its calendar event Uri.
	 * the calendar event stores its android event Uri.
	 * @param uri
	 */
	public Uri getCounterpartUri() {
		return mcounterpartUri;
	}
	
	/**
	 * returns a list of all items that are comparable with this sync adapter
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
		Result.add(ceTAG);
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
