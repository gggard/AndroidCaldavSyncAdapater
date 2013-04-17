/**
 * Copyright (c) 2012-2013, Gerald Garcia, David Wiesner, Timo Berger
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
//import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
//import java.security.GeneralSecurityException;

import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.data.ParserException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.gege.caldavsyncadapter.CalendarColors;
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
import android.content.SyncStats;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String TAG = "SyncAdapter";
	private AccountManager mAccountManager;
//	private Context mContext;
	
	
	private static final String[] CALENDAR_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT,                  // 3
	    Calendars.CAL_SYNC1                       // 4
	};
	  
/*	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
*/
	
/*
	private static final String[] EVENT_PROJECTION = new String[] {
		Events._ID,
		Events._SYNC_ID,
		Events.SYNC_DATA1,
		Events.CALENDAR_ID
	};
*/
	
	
	// ignore same CTag
	private static final boolean FORCE_SYNCHRONIZE = false;
	// drop all calendar before synchro
	private static final boolean DROP_CALENDAR_EVENTS = false;
	
	
	
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);

		mAccountManager = AccountManager.get(context);
//		mContext = context;
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
				Log.i(TAG, "Detected calendar name="+calendar.getDisplayName()+" URI="+calendar.getURI());
			
				Uri calendarUri = getOrCreateCalendarUri(account, provider, calendar);
			
				if ((FORCE_SYNCHRONIZE) ||
					(getCalendarCTag(provider, calendarUri) == null) ||
					(!getCalendarCTag(provider, calendarUri).equals(calendar.getcTag()))) {
						Log.d(TAG, "CTag has changed, something to synchronise");
						
						synchroniseEvents(facade,account, provider, calendarUri, calendar, syncResult.stats);
						
						Log.d(TAG, "Updating stored CTag");
						setCalendarCTag(account, provider, calendarUri, calendar.getcTag());
					
				} else {
					Log.d(TAG, "CTag has not changed, nothing to do");
				}
				
				this.checkDirtyAndroidEvents(provider, account, calendarUri);
			}
        /*} catch (final AuthenticatorException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "AuthenticatorException", e);*/
        /*} catch (final OperationCanceledException e) {
            Log.e(TAG, "OperationCanceledExcetpion", e);*/
        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        /*} catch (final AuthenticationException e) {
            //mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authtoken);
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthenticationException", e);*/
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        /*} catch (final JSONException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "JSONException", e);*/
		} catch (Exception e) {
			Log.e(TAG, "Updating calendar exception " + e.getClass().getName(), e);
            syncResult.stats.numParseExceptions++;
			throw new RuntimeException(e);
		}
	}

	

	private void synchroniseEvents(CaldavFacade facade, Account account,
			ContentProviderClient provider, Uri calendarUri, Calendar calendar, SyncStats stats) throws ClientProtocolException, URISyntaxException, IOException, ParserConfigurationException, SAXException, RemoteException, CaldavProtocolException, ParserException {
		
		if (DROP_CALENDAR_EVENTS) {
			dropAllEvents(account, provider, calendarUri);
		}
		
		Iterable<CalendarEvent> eventList = facade.getCalendarEvents(calendar);
		
		int rowInsert = 0;
		int rowUpdate = 0;
		int rawTag = 0;
		int rowDelete = 0;
		int rowUntag = 0;
		int rowSkip = 0;
		
		for (CalendarEvent event : eventList) {
			
			try {
				AndroidEvent androidEvent = getAndroidEvent(provider, event.getUri(), calendarUri);
				
	
				Log.i(TAG, "Event " + event.getUri().toString()+ " androidUri="+androidEvent);
				
				if (androidEvent == null) {
					if (createAndroidEvent(provider, account, calendarUri, event)) {
						rowInsert += 1;
						androidEvent = getAndroidEvent(provider, event.getUri(), calendarUri);
					} else {
						rowSkip += 1;
					}
				} else {
					Log.d(TAG, "Event compare: " + androidEvent.getETag().toString() + " <> " + event.getETag().toString());
					if ((androidEvent.getETag() == null) || (!androidEvent.getETag().equals(event.getETag()))) {
						if (updateAndroidEvent(provider, account, androidEvent, event))
							rowUpdate += 1;
					}
				}
				if (androidEvent != null)
					if (tagAndroidEvent(provider, account, androidEvent))
						rawTag += 1;
				
				
			} catch (ParserException ex) {
				Log.e(TAG, "Parser exception", ex);
				stats.numParseExceptions++;
			} catch (CaldavProtocolException ex) {
				Log.e(TAG, "Caldav exception", ex);
				stats.numParseExceptions++;
			}
		}
		
		rowDelete = deleteUntaggedEvents(provider, account, calendarUri);
		rowUntag = untagAndroidEvents(provider, account, calendarUri);

		Log.i(TAG,"Statistiks for Calendar: " + calendar.getURI().toString());
		Log.i(TAG,"Statistiks for AndroidCalendar: " + calendarUri.toString());
		Log.i(TAG,"Rows inserted: " + String.valueOf(rowInsert));
		Log.i(TAG,"Rows updated:  " + String.valueOf(rowUpdate));
		Log.i(TAG,"Rows tagged:   " + String.valueOf(rawTag));
		Log.i(TAG,"Rows deleted:  " + String.valueOf(rowDelete));
		Log.i(TAG,"Rows reseted:  " + String.valueOf(rowUntag));
		Log.i(TAG,"Rows skipped:  " + String.valueOf(rowSkip));
		
		stats.numInserts += rowInsert;
		stats.numUpdates += rowUpdate;
		stats.numDeletes += rowDelete;
		stats.numSkippedEntries += rowSkip;
		stats.numEntries += rowInsert + rowUpdate + rowDelete;

	}
	
	private boolean checkDirtyAndroidEvents(ContentProviderClient provider, Account account, Uri calendarUri) {
		boolean Result = false;
		Cursor cur = null;
		Long EventID;
		Long CalendarID;
		AndroidEvent ev = null;
		int CountDirtyRows = 0;
		
		try {
			CalendarID = ContentUris.parseId(calendarUri);
			String selection = "(" + Events.DIRTY + " = ?) AND (" + Events.CALENDAR_ID + " = ?)";
			String[] selectionArgs = new String[] {"1", CalendarID.toString()}; 
			cur = provider.query(Events.CONTENT_URI, null, selection, selectionArgs, null);
			
			while (cur.moveToNext()) {
				EventID = cur.getLong(cur.getColumnIndex(Events._ID));
				Uri returnedUri = ContentUris.withAppendedId(Events.CONTENT_URI, EventID);
				
				ev = new AndroidEvent(returnedUri, calendarUri);
				ev.readContentValues(cur);
				
				selection = "(" + Events._ID + "= ?)";
				selectionArgs = new String[] {EventID.toString()};
				ev.ContentValues.put(Events.DIRTY, 0);
				int RowCount = provider.update(asSyncAdapter(ev.getUri(), account.name, account.type), ev.ContentValues, null, null);
				
				if (RowCount == 1)
					CountDirtyRows += 1;
			}

			//if (CountDirtyRows > 0)
			Log.d(TAG,"Dirty Rows in calendar:" + String.valueOf(CountDirtyRows) + " " + calendarUri.toString());
			
			Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return Result;
	}

	private int deleteUntaggedEvents(ContentProviderClient provider, Account account, Uri calendarUri) throws RemoteException {
		String mSelectionClause = "(" + Events.SYNC_DATA2 +  "<> ?) AND (" + Events.CALENDAR_ID + " = ?)";
		String[] mSelectionArgs = {"1", Long.toString(ContentUris.parseId(calendarUri))};
		
		int CountDeleted = provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), mSelectionClause, mSelectionArgs);	
		//Log.d(TAG, "Rows deleted: " + CountDeleted.toString());
		return CountDeleted;
	}
	
	private boolean tagAndroidEvent(ContentProviderClient provider,Account account, AndroidEvent androidEvent) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Events.SYNC_DATA2, 1);
		
		int RowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);
		//Log.e(TAG,"Rows updated: " + RowCount.toString());
		
		return (RowCount == 1);
	} 
	
	private int untagAndroidEvents(ContentProviderClient provider, Account account, Uri calendarUri) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Events.SYNC_DATA2, 0);
		
		String mSelectionClause = "(" + Events.SYNC_DATA2 +  " = ?) AND (" + Events.CALENDAR_ID + " = ?)";
		String[] mSelectionArgs = {"1", Long.toString(ContentUris.parseId(calendarUri))};
		
		int RowCount = provider.update(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), values, mSelectionClause, mSelectionArgs);
		//Log.d(TAG, "Rows reseted: " + RowCount.toString());
		return RowCount;
	}
	
	private boolean updateAndroidEvent(ContentProviderClient provider,
			Account account, AndroidEvent androidEvent, CalendarEvent event) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		boolean BodyFetched = event.fetchBody();
		int Rows = 0;
		
		if (BodyFetched) {
			ContentValues values = event.getContentValues(androidEvent.getCalendarUri());
			event.setAndroidUri(androidEvent.getUri());
		
			Log.d(TAG, "AndroidEvent is dirty: " + androidEvent.ContentValues.getAsString(Events.DIRTY));
			
			if (CheckEventValuesChanged(androidEvent.ContentValues, values, androidEvent.getComparableItems())) {
				//check the attendees and reminders
				this.InsertAttendees(provider, event);
				this.InsertReminder(provider, event);

				androidEvent.ContentValues.put(Events.DIRTY, 0); // the event is now in sync
				Log.d(TAG, "Update calendar event: for "+androidEvent.getUri());
				
				Rows = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), androidEvent.ContentValues, null, null);
				//Log.i(TAG, "Updated calendar event: rows effected " + Rows.toString());
			} else {
				Log.d(TAG, "Update calendar event not needed: for "+androidEvent.getUri());
			}
		}
		return (Rows == 1);
	}
	
	private boolean InsertReminder(ContentProviderClient provider, CalendarEvent event) {
		boolean Result = false;
		
		try {
			String mSelectionClause = "(" + Reminders.EVENT_ID + " = ?)";
			String[] mSelectionArgs = {Long.toString(ContentUris.parseId(event.getAndroidUri())) };
			int RowDelete;
			RowDelete = provider.delete(Reminders.CONTENT_URI, mSelectionClause, mSelectionArgs);
			Log.d(TAG, "Reminders Deleted:" + String.valueOf(RowDelete));
			
			
			Uri ReminderUri;
			java.util.ArrayList<ContentValues> ReminderList = event.getReminders();
			for (ContentValues Reminder : ReminderList) {
				ReminderUri = provider.insert(Reminders.CONTENT_URI, Reminder);
				System.out.println(ReminderUri);
			}
			Log.d(TAG, "Reminders Inserted:" + String.valueOf(ReminderList.size()));
			
			Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return Result;
	}
	
	private boolean InsertAttendees(ContentProviderClient provider, CalendarEvent event) {
		boolean Result = false;
		
		try {
			String mSelectionClause = "(" + Attendees.EVENT_ID + " = ?)";
			String[] mSelectionArgs = {Long.toString(ContentUris.parseId(event.getAndroidUri())) };
			int RowDelete;
			RowDelete = provider.delete(Attendees.CONTENT_URI, mSelectionClause, mSelectionArgs);
			Log.d(TAG, "Attendees Deleted:" + String.valueOf(RowDelete));
			
			java.util.ArrayList<ContentValues> AttendeeList = event.getAttandees();
			for (ContentValues Attendee : AttendeeList) {
				provider.insert(Attendees.CONTENT_URI, Attendee);
			}
			Log.d(TAG, "Attendees Inserted:" + String.valueOf(AttendeeList.size()));
			Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		return Result;
	}
	
	private boolean CheckEventValuesChanged(ContentValues AndroidEvt, ContentValues CalendarEvt, java.util.ArrayList<String> CompareItems) {
		boolean Result = false;
		Object ValueAndroid = null;
		Object ValueCalendar = null;

		for (String Key: CompareItems) {

			if (AndroidEvt.containsKey(Key)) 
				ValueAndroid = AndroidEvt.get(Key);
			else
				ValueAndroid = null;

			if (CalendarEvt.containsKey(Key))
				ValueCalendar = CalendarEvt.get(Key);
			else
				ValueCalendar = null;

			/*
			 * TODO: Sync is designed to "Server always wins", should be a general option for this adapter
			 */
			if (ValueAndroid != null) {
				if (ValueCalendar != null) {
					if (!ValueAndroid.toString().equals(ValueCalendar.toString())) {
						Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString() + " <> " + ValueCalendar.toString());
						AndroidEvt.put(Key,ValueCalendar.toString());
						Result = true;
					}
				} else {
					Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString() + " <> null");
					AndroidEvt.putNull(Key);
					Result = true;
				}
			} else {
				if (ValueCalendar != null) {
					Log.d(TAG, "difference in " + Key.toString() + ":null <> " + ValueCalendar.toString());
					AndroidEvt.put(Key, ValueCalendar.toString());
					Result = true;
				} else {
					// both null -> this is ok
				}
			}
		}
		
		return Result;
	}

	private void dropAllEvents(Account account, ContentProviderClient provider,	Uri calendarUri) throws RemoteException {
		
		Log.i(TAG, "Deleting all calendar events for "+calendarUri);
		
		String selection = "(" + Events.CALENDAR_ID + " = ?)";
		String[] selectionArgs = new String[] {Long.toString(ContentUris.parseId(calendarUri))}; 
		
		provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), 
				        selection, selectionArgs);
		
	}


	
	private boolean createAndroidEvent(ContentProviderClient provider, Account account, Uri calendarUri, CalendarEvent event) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		boolean Result = false;
		boolean BodyFetched = event.fetchBody();
		int CountAttendees = 0;
		int CountReminders = 0;
		
		if (BodyFetched) {
			ContentValues values = event.getContentValues(calendarUri);
		
			Uri uri = provider.insert(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), values);
			event.setAndroidUri(uri);
		
			Log.d(TAG, "Creating calendar event for " + uri.toString());
			
			//check the attendees
			java.util.ArrayList<ContentValues> AttendeeList = event.getAttandees();
			for (ContentValues Attendee : AttendeeList) {
				provider.insert(Attendees.CONTENT_URI, Attendee);
				CountAttendees += 1;
			}
			
			//check the reminders
			java.util.ArrayList<ContentValues> ReminderList = event.getReminders();
			for (ContentValues Reminder : ReminderList) {
				provider.insert(Reminders.CONTENT_URI, Reminder);
				CountReminders += 1;
			}
			
			if ((CountAttendees > 0) || (CountReminders > 0)) {
				//the events gets dirty when attendees or reminders were added
				AndroidEvent androidEvent = getAndroidEvent(provider, event.getUri(), calendarUri);
				androidEvent.ContentValues.put(Events.DIRTY, 0);
				//CheckEventValuesChanged(androidEvent.ContentValues, values, androidEvent.getComparableItems());
				int RowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), androidEvent.ContentValues, null, null);
				Result = (RowCount == 1);
			} else {
				Result = true;
			}
			
			
		}
		return Result;
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
			
			// find a color
			int index = calendarCount(account, provider);
			index = index % CalendarColors.colors.length;
			contentValues.put(Calendars.CALENDAR_COLOR, CalendarColors.colors[index]);


			returnedCalendarUri = provider.insert(asSyncAdapter(Calendars.CONTENT_URI, account.name, account.type), contentValues);

			long newCalendarId = ContentUris.parseId(returnedCalendarUri);

			Log.v(TAG, "New calendar created : URI="+newCalendarId+ " id="+newCalendarId);
			
		} else {
			returnedCalendarUri = getCalendarUri(account, provider, calendar);
		}
		
		
		
		return returnedCalendarUri;
	}
	
	private static AndroidEvent getAndroidEvent(ContentProviderClient provider, URI caldavEventUri, Uri calendarUri) throws RemoteException {
		Uri uri = Events.CONTENT_URI;
		
		String selection = "(" + Events._SYNC_ID + " = ?)";

		String[] selectionArgs = new String[] {caldavEventUri.toString()}; 
		
		//Cursor cur = provider.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		Cursor cur = provider.query(uri, null, selection, selectionArgs, null);
		
		
		if (cur.getCount() == 0) return null;
		cur.moveToNext();

	
		Uri returnedUri = ContentUris.withAppendedId(uri, cur.getLong(cur.getColumnIndex(Events._ID)) );
		
		AndroidEvent ev = new AndroidEvent(returnedUri, calendarUri);
		ev.readContentValues(cur);

		return ev;
	}
	
	private static boolean isCalendarExist(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		
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
	
	static int calendarCount(Account account, ContentProviderClient provider) throws RemoteException {
		
		Cursor cur = null;
		
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "";
		String[] selectionArgs = new String[] {}; 
		// Submit the query and get a Cursor object back. 
		cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
		
		return cur.getCount();
	}
	
	private static Uri getCalendarUri(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		
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
	
	private static String getCalendarCTag(ContentProviderClient provider, Uri calendarUri) throws RemoteException {
		
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
	
	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }

}

