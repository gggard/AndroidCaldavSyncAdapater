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

import java.net.URI;
import java.net.URISyntaxException;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

public class Calendar {
	private static final String TAG = "Calendar";
	
	/**
	 * stores the CTAG of a calendar
	 */
	public static String CTAG = Calendars.CAL_SYNC1;
	
	/**
	 * stores the URI of a calendar
	 * example: http://<server>/calendarserver.php/calendars/<username>/<calendarname>
	 */
	public static String URI = Calendars._SYNC_ID;
	
	//private URI calendarURI;
	//private String cTag;
	private String strCalendarColor = "";
	//private String calendarName  = "";
	//private int androidCalendarId = 0;

	/**
	 * the event transformed into ContentValues
	 */
	public ContentValues ContentValues = new ContentValues();
	
	private Account mAccount = null;
	private ContentProviderClient mProvider = null;
	
	public boolean foundServerSide = false;
	public boolean foundClientSide = false;
	
	/**
	 * example: http://<server>/calendarserver.php/calendars/<username>/<calendarname>
	 */
	public URI getURI() {
		//return this.calendarURI;
		String strUri = this.getContentValueAsString(Calendar.URI);
		URI result = null;
		try {
			result = new URI(strUri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * example: http://<server>/calendarserver.php/calendars/<username>/<calendarname>
	 */
	public void setURI(URI uri) {
	//	this.calendarURI = uri;
		this.setContentValueAsString(Calendar.URI, uri.toString());
	}

	/**
	 * example:
	 * 		should be: http://caldav.example.com/calendarserver.php/calendars/<username>/<calendarname>/ 
	 *		but is:    null
	 */
	//public void setUri(String syncID) {
	//	this.setContentValueAsString(Calendar.URI, syncID);
	//}
	
	/**
	 * example:
	 * 		should be: http://caldav.example.com/calendarserver.php/calendars/<username>/<calendarname>/ 
	 *		but is:    null
	 */
	//public String getUri() {
	//	return this.getContentValueAsString(Calendar.URI);
	//}
	
	/**
	 * example: Cleartext Display Name 
	 */
	public String getCalendarDisplayName() {
		return this.getContentValueAsString(Calendars.CALENDAR_DISPLAY_NAME);
	}

	/**
	 * example: Cleartext Display Name 
	 */
	public void setCalendarDisplayName(String displayName) {
		this.setContentValueAsString(Calendars.CALENDAR_DISPLAY_NAME, displayName);
	}
	

	/**
	 * example: 1143
	 */
	public void setCTag(String cTag) {
		//this.cTag = cTag;
		this.setContentValueAsString(Calendar.CTAG, cTag);
	}
	
	/**
	 * example: 1143
	 */
	public String getcTag() {
		//return cTag;
		return this.getContentValueAsString(Calendar.CTAG);
	}
	
	/**
	 * example: #FFCCAA 
	 */
	public void setCalendarColorAsString(String color) {
		int maxlen = 6;
		
		this.strCalendarColor = color;
		if (!color.equals("")) {
			String strColor = color.replace("#", "");
			if (strColor.length() > maxlen)
				strColor = strColor.substring(0, maxlen);
			//long lngColor = Long.parseLong(strColor, 16);
			int intColor = Integer.parseInt(strColor, 16);
			//int intColor = (int) lngColor;
			this.setContentValueAsInt(Calendars.CALENDAR_COLOR, intColor);
		}
	}

	/**
	 * example: #FFCCAA 
	 */
	public String getCalendarColorAsString() {
		return this.strCalendarColor;
	}

	/**
	 * example 12345 
	 */
	public int getCalendarColor() {
		return this.getContentValueAsInt(Calendars.CALENDAR_COLOR);
	}
	
	/**
	 * example 12345 
	 */
	public void setCalendarColor(int color) {
		this.setContentValueAsInt(Calendars.CALENDAR_COLOR, color);
	}

	/**
	 * example: 
	 * 		should be: calendarname
	 * 		but is:    http://caldav.example.com/calendarserver.php/calendars/<username>/<calendarname>/
	 */
	public String getCalendarName() {
		//return calendarName;
		return this.getContentValueAsString(Calendars.NAME);
	}

	/**
	 * example: 
	 * 		should be: calendarname
	 * 		but is:    http://caldav.example.com/calendarserver.php/calendars/<username>/<calendarname>/
	 */
	public void setCalendarName(String calendarName) {
		//this.calendarName = calendarName;
		this.setContentValueAsString(Calendars.NAME, calendarName);
	}

	/**
	 * example: 8
	 */
	public int getAndroidCalendarId() {
		//return androidCalendarId;
		return this.getContentValueAsInt(Calendars._ID);
	}

	/**
	 * example: 8
	 */
	public void setAndroidCalendarId(int androidCalendarId) {
		//this.androidCalendarId = androidCalendarId;
		this.setContentValueAsInt(Calendars._ID, androidCalendarId);
	}

	/**
	 * example: content://com.android.calendar/calendars/8
	 */
	public Uri getAndroidCalendarUri() {
		return ContentUris.withAppendedId(Calendars.CONTENT_URI, this.getAndroidCalendarId());
	}
	
	/**
	 * empty constructor
	 */
	public Calendar() {
	}
	
	/**
	 * creates an new instance from a cursor
	 * @param cur must be a cursor from "ContentProviderClient" with Uri Calendars.CONTENT_URI
	 */
	public Calendar(Account account, ContentProviderClient provider, Cursor cur) {
		this.mAccount = account;
		this.mProvider = provider;
		this.foundClientSide = true;

		String strSyncID = cur.getString(cur.getColumnIndex(Calendars._SYNC_ID));
		String strName = cur.getString(cur.getColumnIndex(Calendars.NAME));
		String strDisplayName = cur.getString(cur.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME));
		String strCTAG = cur.getString(cur.getColumnIndex(Calendar.CTAG));
		int intAndroidCalendarId = cur.getInt(cur.getColumnIndex(Calendars._ID));

		this.setCalendarName(strName);
		this.setCalendarDisplayName(strDisplayName);
		this.setCTag(strCTAG);
		this.setAndroidCalendarId(intAndroidCalendarId);

		if (strSyncID == null) {
			this.correctSyncID(strName);
			strSyncID = strName;
		}
		URI uri = null;
		try {
			uri = new URI(strSyncID);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		this.setURI(uri);
		
		this.Debug();
	}
	
	public void Debug() {
		Log.v(TAG, "new Calendar");
		for (String Key : this.ContentValues.keySet()) {
			Log.v(TAG, Key + "=" + ContentValues.getAsString(Key));
		}
	}
	
	/**
	 * the calendar Uri was stored as calendar Name. this function updates the URI (_SYNC_ID)
	 * @param calendarUri the real calendarUri
	 * @return success of this function 
	 */
	private boolean correctSyncID(String calendarUri) {
		boolean Result = false;
		Log.v(TAG, "correcting calendar:" + this.getContentValueAsString(Calendars.CALENDAR_DISPLAY_NAME));
			
		ContentValues mUpdateValues = new ContentValues();
		mUpdateValues.put(Calendar.URI, calendarUri);
		
		try {
			mProvider.update(this.SyncAdapterCalendar(), mUpdateValues, null, null);
			Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return Result;
	}
	
	/**
	 * there is no corresponding calendar on server side. time to delete this calendar on android side.
	 * @return 
	 */
	public boolean deleteAndroidCalendar() {
		boolean Result = false;
		
		String mSelectionClause = "(" + Calendars._ID + " = ?)";
		int calendarId  = this.getAndroidCalendarId();
		String[] mSelectionArgs = {Long.toString(calendarId)};
		
		int CountDeleted = 0;
		try {
			CountDeleted = mProvider.delete(this.SyncAdapter(), mSelectionClause, mSelectionArgs);
			Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
		Log.d(TAG, "Android Calendars deleted: " + Integer.toString(CountDeleted));
		
		return Result;
	}
	

	public void updateCalendarCTag(Uri calendarUri, String cTag) throws RemoteException {
		
		ContentValues mUpdateValues = new ContentValues();
		mUpdateValues.put(Calendar.CTAG, cTag);
		
		mProvider.update(asSyncAdapter(calendarUri, mAccount.name, mAccount.type), mUpdateValues, null, null);
	}

	public void updateCalendarColor(Uri calendarUri, Calendar calendar) throws RemoteException {
	
		ContentValues mUpdateValues = new ContentValues();
		mUpdateValues.put(Calendars.CALENDAR_COLOR, calendar.getCalendarColor());
		
		mProvider.update(asSyncAdapter(calendarUri, mAccount.name, mAccount.type), mUpdateValues, null, null);
	}
	
	private Uri SyncAdapterCalendar() {
		return asSyncAdapter(this.getAndroidCalendarUri(), mAccount.name, mAccount.type);
	}
	private Uri SyncAdapter() {
		return asSyncAdapter(Calendars.CONTENT_URI, mAccount.name, mAccount.type);
	}
	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	}
	
	public void setAccount(Account account) {
		this.mAccount = account;
	}
	public void setProvider(ContentProviderClient provider) {
		this.mProvider = provider;
	}
	
	/**
	 * general access function to ContentValues
	 * @param Item the item name from Calendars.*
	 * @return the value for the item
	 */
	private String getContentValueAsString(String Item) {
		String Result = "";
		if (this.ContentValues.containsKey(Item))
			Result = this.ContentValues.getAsString(Item);
		return Result;
	}
	/**
	 * general access function to ContentValues
	 * @param Item the item name from Calendars.*
	 * @return the value for the item
	 */
	private int getContentValueAsInt(String Item) {
		int Result = 0;
		if (this.ContentValues.containsKey(Item))
			Result = this.ContentValues.getAsInteger(Item);
		return Result;
	}
	
	/**
	 * general access function to ContentValues
	 * @param Item the item name from Calendars.*
	 * @param Value the value for the item
	 * @return success of this function
	 */
	private boolean setContentValueAsString(String Item, String Value) {
		boolean Result = false;
		
		if (this.ContentValues.containsKey(Item))
			this.ContentValues.remove(Item);
		this.ContentValues.put(Item, Value);
		
		return Result;
	}
	
	/**
	 * general access function to ContentValues
	 * @param Item the item name from Calendars.*
	 * @param Value the value for the item
	 * @return success of this function
	 */
	private boolean setContentValueAsInt(String Item, int Value) {
		boolean Result = false;
		
		if (this.ContentValues.containsKey(Item))
			this.ContentValues.remove(Item);
		this.ContentValues.put(Item, Value);
		
		return Result;
	}
}
