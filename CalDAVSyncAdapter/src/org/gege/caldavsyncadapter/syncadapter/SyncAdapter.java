/**
 * Copyright (c) 2012-2013, David Wiesner
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

package org.gege.caldavsyncadapter.syncadapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.data.ParserException;

import org.apache.http.client.ClientProtocolException;
import org.gege.caldavsyncadapter.Constants;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String TAG = "SyncAdapter";
	private AccountManager mAccountManager;
	private Context mContext;
	
	
	public static final String[] CALENDAR_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT,                  // 3
	    Calendars.CAL_SYNC1                       // 4
	};
	  
	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
	
	

	private static final String[] EVENT_PROJECTION = new String[] {
		Events._ID,
		Events._SYNC_ID,
		Events.SYNC_DATA1,
		Events.CALENDAR_ID
	};
	
	
	
	// ignore same CTag
	private static final boolean FORCE_SYNCHRONIZE = false;
	// drop all calendar before synchro
	private static final boolean DROP_CALENDAR_EVENTS = false;
	
	
	
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		mAccountManager = AccountManager.get(context);
		mContext = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		
		String url = mAccountManager.getUserData(account, Constants.USER_DATA_URL_KEY);

		Log.v(TAG, "onPerformSync() on "+account.name+" with URL "+url);

		
		
		
		
		Iterable<Calendar> calendarList;
		
		try {
			CaldavFacade facade = new CaldavFacade(account.name, mAccountManager.getPassword(account), url);
			calendarList = facade.getCalendarList();
			
			for (Calendar calendar : calendarList) {
				Log.d(TAG, "Detected calendar name="+calendar.getDisplayName()+" URI="+calendar.getURI());
			
				Uri calendarUri = getOrCreateCalendarUri(account, provider, calendar);
			
				if ((FORCE_SYNCHRONIZE) ||
					(getCalendarCTag(provider, calendarUri) == null) ||
					(!getCalendarCTag(provider, calendarUri).equals(calendar.getcTag()))) {
						Log.d(TAG, "CTag has changed, something to synchronise");
						
						synchroniseEvents(facade,account, provider, calendarUri, calendar);
						
						Log.d(TAG, "Updating stored CTag");
						setCalendarCTag(account, provider, calendarUri, calendar.getcTag());
					
				} else {
					Log.d(TAG, "CTag has not changed, nothing to do");
				}
				
			}
		}
		catch (Exception e) {
			Log.e(TAG, "Updating calendar exception "+e.getClass().getName(), e);
			throw new RuntimeException(e);
		}
	}

	

	private void synchroniseEvents(CaldavFacade facade, Account account,
			ContentProviderClient provider, Uri calendarUri, Calendar calendar) throws ClientProtocolException, URISyntaxException, IOException, ParserConfigurationException, SAXException, RemoteException, CaldavProtocolException, ParserException {
		
		if (DROP_CALENDAR_EVENTS) {
			dropAllEvents(account, provider, calendarUri);
		}
		
		Iterable<CalendarEvent> eventList = facade.getCalendarEvents(calendar);
		
		for (CalendarEvent event : eventList) {
			
			try {
			AndroidEvent androidEvent = getAndroidEvent(provider, event.getUri(), calendarUri);
			

			Log.d(TAG, "Event "+event.getUri().toString()+ " androidUri="+androidEvent);
			
			if (androidEvent == null) {
				createAndroidEvent(provider, account, calendarUri, event);
			} else {
				if ((androidEvent.getETag() == null) || 
						(!androidEvent.getETag().equals(event.getETag()))) {
					updateAndroidEvent(provider, account, androidEvent, event);
				}
				
				tagAndroidEvent(provider, account, androidEvent);
			}
			} catch (ParserException ex) {
				Log.e(TAG, "Parser exception", ex);
			} catch (CaldavProtocolException ex) {
				Log.e(TAG, "Caldav exception", ex);
			}
			
		}
		
		deleteUntaggedEvents(provider, account);
		untagAndroidEvents(provider, account);
		
	}

	private void deleteUntaggedEvents(ContentProviderClient provider,
			Account account) throws RemoteException {
		String mSelectionClause = Events.SYNC_DATA2 +  "<> ?";
		String[] mSelectionArgs = {"1"};
		
		provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), mSelectionClause, mSelectionArgs);	

	}

	private void tagAndroidEvent(ContentProviderClient provider,
			Account account, AndroidEvent androidEvent) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Events.SYNC_DATA2, 1);
		
		provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);	
	}

	
	private void untagAndroidEvents(ContentProviderClient provider,
			Account account) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Events.SYNC_DATA2, 0);
		
		String mSelectionClause = Events.SYNC_DATA2 +  " = ?";
		String[] mSelectionArgs = {"1"};
		
		provider.update(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), values, mSelectionClause, mSelectionArgs);	
	}

	
	private void updateAndroidEvent(ContentProviderClient provider,
			Account account, AndroidEvent androidEvent, CalendarEvent event) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		Log.i(TAG, "Updating calendar event for "+androidEvent.getUri());
		
		event.fetchBody();
		
		ContentValues values = getContentValues(event, androidEvent.getCalendarUri());
		
		provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);

	}

	private void dropAllEvents(Account account, ContentProviderClient provider,
			Uri calendarUri) throws RemoteException {
		
		Log.i(TAG, "Deleting all calendar events for "+calendarUri);
		
		String selection = "(" + Events.CALENDAR_ID + " = ?)";

		String[] selectionArgs = new String[] {Long.toString(ContentUris.parseId(calendarUri))}; 
		
		provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), 
				        selection, selectionArgs);
		
	}

	private ContentValues getContentValues(CalendarEvent event, Uri calendarUri) {
		ContentValues values = new ContentValues();
		values.put(Events.DTSTART, event.getStartTime());
		values.put(Events.DTEND, event.getEndTime());
		values.put(Events.TITLE, event.getTitle());
		values.put(Events.CALENDAR_ID, ContentUris.parseId(calendarUri));
		values.put(Events.EVENT_TIMEZONE, event.getTimeZone());
		values.put(Events._SYNC_ID, event.getUri().toString());
		values.put(Events.SYNC_DATA1, event.getETag());
		return values;
	}
	
	private void createAndroidEvent(ContentProviderClient provider, Account account, Uri calendarUri, CalendarEvent event) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		event.fetchBody();
		
		ContentValues values = getContentValues(event, calendarUri);
		
		Uri uri = provider.insert(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), values);
		
	}

	private Uri getOrCreateCalendarUri(Account account,
			ContentProviderClient provider, Calendar calendar) throws RemoteException {
		
		Uri returnedCalendarUri = null;
		
		if (!isCalendarExist(account,provider, calendar)) {

			final ContentValues contentValues = new ContentValues();
			contentValues.put(Calendars.NAME, calendar.getURI().toString()); 
			contentValues.put(Calendars.VISIBLE, 1);
			contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, calendar.getDisplayName());
			contentValues.put(Calendars.OWNER_ACCOUNT, account.name);
			contentValues.put(Calendars.ACCOUNT_NAME, account.name);
			contentValues.put(Calendars.ACCOUNT_TYPE, account.type);
			contentValues.put(Calendars.SYNC_EVENTS, 1);
			contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
			
			

			returnedCalendarUri = provider.insert(asSyncAdapter(Calendars.CONTENT_URI, account.name, account.type), contentValues);

			long newCalendarId = ContentUris.parseId(returnedCalendarUri);

			Log.v(TAG, "New calendar created : URI="+newCalendarId+ " id="+newCalendarId);
			
		} else {
			returnedCalendarUri = getCalendarUri(account, provider, calendar);
		}
		
		
		
		return returnedCalendarUri;
	}
	
	static AndroidEvent getAndroidEvent(ContentProviderClient provider, URI caldavEventUri, Uri calendarUri) throws RemoteException {
		Uri uri = Events.CONTENT_URI;
		
		String selection = "(" + Events._SYNC_ID + " = ?)";

		String[] selectionArgs = new String[] {caldavEventUri.toString()}; 
		
		Cursor cur = provider.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		
		if (cur.getCount() == 0) return null;
		cur.moveToNext();
		
		Uri returnedUri = ContentUris.withAppendedId(uri, cur.getLong(cur.getColumnIndex(Events._ID)) );
		
		AndroidEvent ev = new AndroidEvent();
		ev.setUri(returnedUri);
				
		ev.setCalendarUri(calendarUri);
		ev.setETag(cur.getString(cur.getColumnIndex(Events.SYNC_DATA1)));
		
		return ev;
	}
	
	static boolean isCalendarExist(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		
		Cursor cur = null;
		
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
		                        + Calendars.ACCOUNT_TYPE + " = ?) AND (" 
				                + Calendars.NAME + " = ?) AND ("
		                        + Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] {account.name, account.type,
				 							   calendar.getURI().toString(),
		        							   account.name}; 
		// Submit the query and get a Cursor object back. 
		cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
		
		return (cur.getCount() != 0);
	}
	
static Uri getCalendarUri(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		
		Cursor cur = null;
		
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
		                        + Calendars.ACCOUNT_TYPE + " = ?) AND (" 
				                + Calendars.NAME + " = ?) AND ("
		                        + Calendars.OWNER_ACCOUNT + " = ?))";
		String[] selectionArgs = new String[] {account.name, account.type,
				 							   calendar.getURI().toString(),
		        							   account.name}; 
		// Submit the query and get a Cursor object back. 
		cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
		
		if (cur.getCount() == 0) {
			return null;
		} else {
			cur.moveToNext();
			Uri returnedUri = ContentUris.withAppendedId(uri, cur.getLong(cur.getColumnIndex(Calendars._ID)) );
			return returnedUri;
		}
	}
	
static String getCalendarCTag(ContentProviderClient provider, Uri calendarUri) throws RemoteException {
	
	Cursor cur = null;
	
	// Submit the query and get a Cursor object back. 
	cur = provider.query(calendarUri, CALENDAR_PROJECTION, null, null, null);
	
	if (cur.getCount() == 0) {
		return null;
	} else {
		cur.moveToNext();
		String returnedCtag = cur.getString(cur.getColumnIndex(Calendars.CAL_SYNC1));
		return returnedCtag;
	}
}

private void setCalendarCTag(Account account, ContentProviderClient provider,
		Uri calendarUri, String cTag) throws RemoteException {
	
	ContentValues mUpdateValues = new ContentValues();
	mUpdateValues.put(Calendars.CAL_SYNC1, cTag);
	
	provider.update(asSyncAdapter(calendarUri, account.name, account.type), mUpdateValues, null, null);
}
	
	static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }

}
