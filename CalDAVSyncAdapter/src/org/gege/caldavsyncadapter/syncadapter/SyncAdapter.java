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
import java.net.URI;
//import java.net.MalformedURLException;
import java.net.URISyntaxException;
//import java.security.GeneralSecurityException;

import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.data.ParserException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.gege.caldavsyncadapter.CalendarColors;
import org.gege.caldavsyncadapter.Constants;
import org.gege.caldavsyncadapter.Event;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.syncadapter.notifications.NotificationsHelper;
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
		String trust = mAccountManager.getUserData(account, Constants.USER_DATA_TRUST_ALL_KEY);
		Log.v(TAG, "onPerformSync() on "+account.name+" with URL "+url);

		Iterable<Calendar> calendarList;
		
		try {
			
			CaldavFacade facade = new CaldavFacade(account.name, mAccountManager.getPassword(account), url, trust);
			calendarList = facade.getCalendarList(getContext());
			//String davProperties = facade.getLastDav();
			
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

					long CalendarID = ContentUris.parseId(calendarUri);
					String selection = "(" + Events.CALENDAR_ID + " = ?)";
					String[] selectionArgs = new String[] {String.valueOf(CalendarID)}; 
					Cursor countCursor = provider.query(Events.CONTENT_URI, new String[] {"count(*) AS count"},
				                selection,
				                selectionArgs,
				                null);

				        countCursor.moveToFirst();
				        int count = countCursor.getInt(0);
				        syncResult.stats.numSkippedEntries += count;
				}
				
				this.checkDirtyAndroidEvents(provider, account, calendarUri, facade, calendar.getURI(),  syncResult.stats);
				//int rowDirty = this.checkDirtyAndroidEvents(provider, account, calendarUri, facade, calendar.getURI(),  syncResult.stats);
				//Log.i(TAG,"Rows dirty:    " + String.valueOf(rowDirty));
			}
        /*} catch (final AuthenticatorException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "AuthenticatorException", e);*/
        /*} catch (final OperationCanceledException e) {
            Log.e(TAG, "OperationCanceledExcetpion", e);*/
        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
            NotificationsHelper.signalSyncErrors(getContext(), "Caldav sync error (IO)", e.getMessage());
            //NotificationsHelper.getCurrentSyncLog().addException(e);
            /*} catch (final AuthenticationException e) {
            //mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authtoken);
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthenticationException", e);*/
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
            NotificationsHelper.signalSyncErrors(getContext(), "Caldav sync error (parsing)", e.getMessage());
            //NotificationsHelper.getCurrentSyncLog().addException(e);
        /*} catch (final JSONException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "JSONException", e);*/
		} catch (Exception e) {
			Log.e(TAG, "Updating calendar exception " + e.getClass().getName(), e);
            syncResult.stats.numParseExceptions++;
            NotificationsHelper.signalSyncErrors(getContext(), "Caldav sync error (general)", e.getMessage());
            //NotificationsHelper.getCurrentSyncLog().addException(e);
			//throw new RuntimeException(e);
		}
	}

	
	/**
	 * both calender event and android event have been found.
	 * server wins always at the moment.
	 * @param facade
	 * @param account
	 * @param provider
	 * @param calendarUri
	 * @param calendar
	 * @param stats
	 * @throws ClientProtocolException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws RemoteException
	 * @throws CaldavProtocolException
	 * @throws ParserException
	 * @see SyncAdapter#updateAndroidEvent(ContentProviderClient, Account, AndroidEvent, CalendarEvent)
	 * @see SyncAdapter#tagAndroidEvent(ContentProviderClient, Account, AndroidEvent)
	 * @see SyncAdapter#untagAndroidEvents(ContentProviderClient, Account, Uri)
	 * @see SyncAdapter#deleteUntaggedEvents(ContentProviderClient, Account, Uri)
	 */
	private void synchroniseEvents(CaldavFacade facade, Account account,
			ContentProviderClient provider, Uri calendarUri, Calendar calendar, SyncStats stats) throws ClientProtocolException, URISyntaxException, IOException, ParserConfigurationException, SAXException, RemoteException, CaldavProtocolException, ParserException {
		
		if (DROP_CALENDAR_EVENTS) {
			dropAllEvents(account, provider, calendarUri);
		}
		
		Iterable<CalendarEvent> eventList = facade.getCalendarEvents(calendar);
		
		int rowInsert = 0;
		int rowUpdate = 0;
		int rowTag = 0;
		int rowDelete = 0;
		int rowUntag = 0;
		int rowSkip = 0;
		
		for (CalendarEvent calendarEvent : eventList) {
			
			try {
				AndroidEvent androidEvent = getAndroidEvent(provider, calendarEvent.getUri(), calendarUri);
				
				Log.i(TAG, "Event " + calendarEvent.getUri().toString()+ " androidUri="+androidEvent);
				
				if (androidEvent == null) {
					/* new android event */
					if (createAndroidEvent(provider, account, calendarUri, calendarEvent)) {
						rowInsert += 1;
						androidEvent = getAndroidEvent(provider, calendarEvent.getUri(), calendarUri);
					} else {
						rowSkip += 1;
					}
				} else {
					/* the android exists */
					String androidETag = androidEvent.getETag();
					if (androidETag == null)
						androidETag = "";
					Log.d(TAG, "Event compare: " + androidETag + " <> " + calendarEvent.getETag().toString());
					if ((androidEvent.getETag() == null) || (!androidETag.equals(calendarEvent.getETag()))) {
						/* the android event is getting updated */
						if (updateAndroidEvent(provider, account, androidEvent, calendarEvent))
							rowUpdate += 1;
					}
				}
				if (androidEvent != null)
					if (tagAndroidEvent(provider, account, androidEvent))
						rowTag += 1;
				
				
			} catch (ParserException ex) {
				Log.e(TAG, "Parser exception", ex);
				stats.numParseExceptions++;

	            NotificationsHelper.signalSyncErrors(getContext(), "Caldav sync error (parsing)", ex.getMessage());
	            //NotificationsHelper.getCurrentSyncLog().addException(ex);
			} catch (CaldavProtocolException ex) {
				Log.e(TAG, "Caldav exception", ex);
				stats.numParseExceptions++;

	            NotificationsHelper.signalSyncErrors(getContext(), "Caldav sync error (caldav)", ex.getMessage());
	            //NotificationsHelper.getCurrentSyncLog().addException(ex);
			}
		}
		
		rowDelete = deleteUntaggedEvents(provider, account, calendarUri);
		rowUntag = untagAndroidEvents(provider, account, calendarUri);

		Log.i(TAG,"Statistiks for Calendar: " + calendar.getURI().toString());
		Log.i(TAG,"Statistiks for AndroidCalendar: " + calendarUri.toString());
		Log.i(TAG,"Rows inserted: " + String.valueOf(rowInsert));
		Log.i(TAG,"Rows updated:  " + String.valueOf(rowUpdate));
		Log.i(TAG,"Rows deleted:  " + String.valueOf(rowDelete));
		Log.i(TAG,"Rows skipped:  " + String.valueOf(rowSkip));
		Log.i(TAG,"Rows tagged:   " + String.valueOf(rowTag));
		Log.i(TAG,"Rows untagged: " + String.valueOf(rowUntag));
		
		stats.numInserts += rowInsert;
		stats.numUpdates += rowUpdate;
		stats.numDeletes += rowDelete;
		stats.numSkippedEntries += rowSkip;
		stats.numEntries += rowInsert + rowUpdate + rowDelete;

	}
	
	/**
	 * checks the android events for the dirty flag.
	 * the flag is set by android when the event has been changed. 
	 * the dirty flag is removed when an android event has been updated from calendar event
	 * @param provider
	 * @param account
	 * @param calendarUri
	 * @return count of dirty events
	 */
	private int checkDirtyAndroidEvents(ContentProviderClient provider, Account account, Uri calendarUri, CaldavFacade facade, URI caldavCalendarUri, SyncStats stats) {
		//boolean Result = false;
		Cursor curEvent = null;
		Cursor curAttendee = null;
		Cursor curReminder = null;
		Long EventID;
		Long CalendarID;
		AndroidEvent androidEvent = null;
		int rowDirty = 0;
		int rowInsert = 0;
		int rowUpdate = 0;
		int rowDelete = 0;
		
		try {
			CalendarID = ContentUris.parseId(calendarUri);
			String selection = "(" + Events.DIRTY + " = ?) AND (" + Events.CALENDAR_ID + " = ?)";
			String[] selectionArgs = new String[] {"1", CalendarID.toString()}; 
			curEvent = provider.query(Events.CONTENT_URI, null, selection, selectionArgs, null);
			
			while (curEvent.moveToNext()) {
				EventID = curEvent.getLong(curEvent.getColumnIndex(Events._ID));
				Uri returnedUri = ContentUris.withAppendedId(Events.CONTENT_URI, EventID);
				
				androidEvent = new AndroidEvent(returnedUri, calendarUri);
				androidEvent.readContentValues(curEvent);
				
				selection = "(" + Attendees.EVENT_ID + " = ?)";
				selectionArgs = new String[] {String.valueOf(EventID)};
				curAttendee = provider.query(Attendees.CONTENT_URI, null, selection, selectionArgs, null);
				selection = "(" + Reminders.EVENT_ID + " = ?)";
				selectionArgs = new String[] {String.valueOf(EventID)};
				curReminder = provider.query(Reminders.CONTENT_URI, null, selection, selectionArgs, null);
				androidEvent.readAttendees(curAttendee);
				androidEvent.readReminder(curReminder);
				curAttendee.close();
				curReminder.close();
				
				String SyncID = androidEvent.ContentValues.getAsString(Events._SYNC_ID);
				
				boolean Deleted = false;
				int intDeleted = 0;
				intDeleted = curEvent.getInt(curEvent.getColumnIndex(Events.DELETED));
				Deleted = (intDeleted == 1);

				if (SyncID == null) {
					// new Android event
					String newGUID = java.util.UUID.randomUUID().toString();
					SyncID = caldavCalendarUri.getPath() + newGUID + "-caldavsyncadapter.ics";
					//ev.ContentValues.put(Events._SYNC_ID, SyncID);
					androidEvent.createIcs();
					
					if (facade.createEvent(URI.create(SyncID), androidEvent.getIcsEvent().toString())) {
						ContentValues values = new ContentValues();
						values.put(Events._SYNC_ID, SyncID);
						values.put(Event.ceTAG, facade.getLastETag());
						values.put(Events.DIRTY, 0);
						
						int rowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);
						if (rowCount == 1)
							rowInsert += 1;
					}
					//rowDirty += 1;
				} else if (Deleted) {
					// deleted Android event
					if (facade.deleteEvent(URI.create(SyncID), androidEvent.getETag())) {
						String mSelectionClause = "(" + Events._ID +  "= ?)";
						String[] mSelectionArgs = {String.valueOf(EventID)};
						
						int countDeleted = provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), mSelectionClause, mSelectionArgs);	
						
						if (countDeleted == 1)
							rowDelete += 1;
					}
					//rowDirty += 1;
				} else {
					// TODO: remove this from "else"
					
/*					CalendarEvent calendarEvent = new CalendarEvent();
					calendarEvent.setICSasString(androidEvent.getIcsEvent().toString());
					try {
						calendarEvent.parseIcs();
					} catch (CaldavProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					calendarEvent.setAndroidCalendarId(androidEvent.getAndroidCalendarId());
					try {
						this.updateAndroidEvent(provider, account, androidEvent, calendarEvent);
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (CaldavProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
*/					
					selection = "(" + Events._ID + "= ?)";
					selectionArgs = new String[] {EventID.toString()};
					androidEvent.ContentValues.put(Events.DIRTY, 0);
					int RowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), androidEvent.ContentValues, null, null);

					if (RowCount == 1)
						rowDirty += 1;
				}
			}
			curEvent.close();

			if ((rowInsert > 0) || (rowUpdate > 0) || (rowDelete > 0) || (rowDirty > 0)) {
				Log.i(TAG,"Android Rows inserted: " + String.valueOf(rowInsert));
				Log.i(TAG,"Android Rows updated:  " + String.valueOf(rowUpdate));
				Log.i(TAG,"Android Rows deleted:  " + String.valueOf(rowDelete));
				Log.i(TAG,"Android Rows dirty:    " + String.valueOf(rowDirty));
			}
			
			stats.numInserts += rowInsert;
			stats.numUpdates += rowUpdate;
			stats.numDeletes += rowDelete;
			stats.numSkippedEntries += rowDirty;
			stats.numEntries += rowInsert + rowUpdate + rowDelete;
			
			//Result = true;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		//return Result;
		return rowDirty;
	}
	
	/**
	 * Events not being tagged are for deletion 
	 * @param provider
	 * @param account
	 * @param calendarUri
	 * @return
	 * @see AndroidEvent#cInternalTag
	 * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri, Calendar, SyncStats)
	 * @throws RemoteException
	 */
	private int deleteUntaggedEvents(ContentProviderClient provider, Account account, Uri calendarUri) throws RemoteException {
		String mSelectionClause = "(" + Event.cInternalTag +  "<> ?) AND (" + Events.CALENDAR_ID + " = ?)";
		String[] mSelectionArgs = {"1", Long.toString(ContentUris.parseId(calendarUri))};
		
		int CountDeleted = provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), mSelectionClause, mSelectionArgs);	
		//Log.d(TAG, "Rows deleted: " + CountDeleted.toString());
		return CountDeleted;
	}

	/**
	 * marks the android event as already handled
	 * @param provider
	 * @param account
	 * @param androidEvent
	 * @return
	 * @see AndroidEvent#cInternalTag
	 * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri, Calendar, SyncStats)
	 * @throws RemoteException
	 */
	private boolean tagAndroidEvent(ContentProviderClient provider,Account account, AndroidEvent androidEvent) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Event.cInternalTag, 1);
		
		int RowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);
		//Log.e(TAG,"Rows updated: " + RowCount.toString());
		
		return (RowCount == 1);
	} 
	
	/**
	 * removes the tag of all android events
	 * @param provider
	 * @param account
	 * @param calendarUri
	 * @return
	 * @see AndroidEvent#cInternalTag
	 * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri, Calendar, SyncStats)
	 * @throws RemoteException
	 */
	private int untagAndroidEvents(ContentProviderClient provider, Account account, Uri calendarUri) throws RemoteException {
		
		ContentValues values = new ContentValues();
		values.put(Event.cInternalTag, 0);
		
		String mSelectionClause = "(" + Event.cInternalTag +  " = ?) AND (" + Events.CALENDAR_ID + " = ?)";
		String[] mSelectionArgs = {"1", Long.toString(ContentUris.parseId(calendarUri))};
		
		int RowCount = provider.update(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), values, mSelectionClause, mSelectionArgs);
		//Log.d(TAG, "Rows reseted: " + RowCount.toString());
		return RowCount;
	}
	
	/**
	 * the android event is getting updated
	 * @param provider
	 * @param account
	 * @param androidEvent
	 * @param calendarEvent
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CaldavProtocolException
	 * @throws RemoteException
	 * @throws ParserException
	 * @see {@link SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri, Calendar, SyncStats)}
	 */
	private boolean updateAndroidEvent(ContentProviderClient provider,
			Account account, AndroidEvent androidEvent, CalendarEvent calendarEvent) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		boolean BodyFetched = calendarEvent.fetchBody();
		int Rows = 0;
		
		if (BodyFetched) {
			calendarEvent.readContentValues();
			//calendarEvent.setAndroidCalendarId(ContentUris.parseId(androidEvent.getAndroidCalendarUri()));
			calendarEvent.setAndroidCalendarId(androidEvent.getAndroidCalendarId());
			calendarEvent.setAndroidEventUri(androidEvent.getUri());
		
			Log.d(TAG, "AndroidEvent is dirty: " + androidEvent.ContentValues.getAsString(Events.DIRTY));
			
			if (this.checkEventValuesChanged(androidEvent.ContentValues, calendarEvent.ContentValues)) {
				//update the attendees and reminders
				this.updateAndroidAttendees(provider, calendarEvent);
				this.updateAndroidReminder(provider, calendarEvent);

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
	
	/**
	 * updates the attendees from a calendarEvent to its androidEvent.
	 * the calendarEvent has to know its androidEvent via {@link CalendarEvent#setAndroidEventUri(Uri)}
	 * @param provider
	 * @param calendarEvent
	 * @return
	 * @see SyncAdapter#updateAndroidEvent(ContentProviderClient, Account, AndroidEvent, CalendarEvent)
	 */
	private boolean updateAndroidAttendees(ContentProviderClient provider, CalendarEvent calendarEvent) {
		boolean Result = false;
		
		try {
			String mSelectionClause = "(" + Attendees.EVENT_ID + " = ?)";
			String[] mSelectionArgs = {Long.toString(ContentUris.parseId(calendarEvent.getAndroidEventUri())) };
			int RowDelete;
			RowDelete = provider.delete(Attendees.CONTENT_URI, mSelectionClause, mSelectionArgs);
			Log.d(TAG, "Attendees Deleted:" + String.valueOf(RowDelete));
			
			java.util.ArrayList<ContentValues> AttendeeList = calendarEvent.getAttandees();
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
	
	/**
	 * update the reminders from a calendarEvent to its androidEvent.
	 * the calendarEvent has to know its androidEvent via {@link CalendarEvent#setAndroidEventUri(Uri)}
	 * @param provider
	 * @param calendarEvent
	 * @return
	 * @see SyncAdapter#updateAndroidEvent(ContentProviderClient, Account, AndroidEvent, CalendarEvent)
	 */
	private boolean updateAndroidReminder(ContentProviderClient provider, CalendarEvent calendarEvent) {
		boolean Result = false;
		
		try {
			String mSelectionClause = "(" + Reminders.EVENT_ID + " = ?)";
			String[] mSelectionArgs = {Long.toString(ContentUris.parseId(calendarEvent.getAndroidEventUri())) };
			int RowDelete;
			RowDelete = provider.delete(Reminders.CONTENT_URI, mSelectionClause, mSelectionArgs);
			Log.d(TAG, "Reminders Deleted:" + String.valueOf(RowDelete));
			
			
			Uri ReminderUri;
			java.util.ArrayList<ContentValues> ReminderList = calendarEvent.getReminders();
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
	
	/**
	 * compares two ContentValues for differences
	 * @param androidEventValues the contentValues of the android event
	 * @param calendarEventValues the contentValues of the calendar event
	 * @return if the events are different
	 */
	private boolean checkEventValuesChanged(ContentValues androidEventValues, ContentValues calendarEventValues) {
		boolean Result = false;
		Object ValueAndroid = null;
		Object ValueCalendar = null;
		java.util.ArrayList<String> CompareItems = Event.getComparableItems();
		
		for (String Key: CompareItems) {

			if (androidEventValues.containsKey(Key)) 
				ValueAndroid = androidEventValues.get(Key);
			else
				ValueAndroid = null;

			if (calendarEventValues.containsKey(Key))
				ValueCalendar = calendarEventValues.get(Key);
			else
				ValueCalendar = null;

			/*
			 * TODO: Sync is designed to "Server always wins", should be a general option for this adapter
			 */
			if (ValueAndroid != null) {
				if (ValueCalendar != null) {
					if (!ValueAndroid.toString().equals(ValueCalendar.toString())) {
						Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString() + " <> " + ValueCalendar.toString());
						androidEventValues.put(Key,ValueCalendar.toString());
						Result = true;
					}
				} else {
					Log.d(TAG, "difference in " + Key.toString() + ":" + ValueAndroid.toString() + " <> null");
					androidEventValues.putNull(Key);
					Result = true;
				}
			} else {
				if (ValueCalendar != null) {
					Log.d(TAG, "difference in " + Key.toString() + ":null <> " + ValueCalendar.toString());
					androidEventValues.put(Key, ValueCalendar.toString());
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


	/**
	 * creates a new androidEvent from a given calendarEvent
	 * @param provider
	 * @param account
	 * @param calendarUri
	 * @param calendarEvent this is the source of the new androidEvent
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws CaldavProtocolException
	 * @throws RemoteException
	 * @throws ParserException
	 * @see {@link SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri, Calendar, SyncStats)}
	 */
	private boolean createAndroidEvent(ContentProviderClient provider, Account account, Uri calendarUri, CalendarEvent calendarEvent) throws ClientProtocolException, IOException, CaldavProtocolException, RemoteException, ParserException {
		boolean Result = false;
		boolean BodyFetched = calendarEvent.fetchBody();
		int CountAttendees = 0;
		int CountReminders = 0;
		
		if (BodyFetched) {
			//calendarEvent.readContentValues(calendarUri);
			calendarEvent.readContentValues();
			calendarEvent.setAndroidCalendarId(ContentUris.parseId(calendarUri));
		
			Uri uri = provider.insert(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), calendarEvent.ContentValues);
			calendarEvent.setAndroidEventUri(uri);
		
			Log.d(TAG, "Creating calendar event for " + uri.toString());
			
			//check the attendees
			java.util.ArrayList<ContentValues> AttendeeList = calendarEvent.getAttandees();
			for (ContentValues Attendee : AttendeeList) {
				provider.insert(Attendees.CONTENT_URI, Attendee);
				CountAttendees += 1;
			}
			
			//check the reminders
			java.util.ArrayList<ContentValues> ReminderList = calendarEvent.getReminders();
			for (ContentValues Reminder : ReminderList) {
				provider.insert(Reminders.CONTENT_URI, Reminder);
				CountReminders += 1;
			}
			
			if ((CountAttendees > 0) || (CountReminders > 0)) {
				//the events gets dirty when attendees or reminders were added
				AndroidEvent androidEvent = getAndroidEvent(provider, calendarEvent.getUri(), calendarUri);
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
			
			if (!calendar.getCalendarColor().equals("")) {
				contentValues.put(Calendars.CALENDAR_COLOR, calendar.getCalendarColorAsLong());
			} else {
				// find a color
				int index = calendarCount(account, provider);
				index = index % CalendarColors.colors.length;
				contentValues.put(Calendars.CALENDAR_COLOR, CalendarColors.colors[index]);
			}

			returnedCalendarUri = provider.insert(asSyncAdapter(Calendars.CONTENT_URI, account.name, account.type), contentValues);

			long newCalendarId = ContentUris.parseId(returnedCalendarUri);

			Log.v(TAG, "New calendar created : URI="+newCalendarId+ " id="+newCalendarId);
			
		} else {
			returnedCalendarUri = getCalendarUri(account, provider, calendar);
			if (!calendar.getCalendarColor().equals(""))
				setCalendarColor(account, provider, returnedCalendarUri, calendar);
		}
		
		
		
		return returnedCalendarUri;
	}
	
	/**
	 * searches for an android event
	 * @param provider
	 * @param caldavEventUri
	 * @param calendarUri
	 * @return the android event
	 * @throws RemoteException
	 */
	private static AndroidEvent getAndroidEvent(ContentProviderClient provider, URI caldavEventUri, Uri calendarUri) throws RemoteException {
		boolean Error = false;
		Uri uriEvents = Events.CONTENT_URI;
		Uri uriAttendee = Attendees.CONTENT_URI;
		Uri uriReminder = Reminders.CONTENT_URI;
		AndroidEvent androidEvent = null;
		
		String selection = "(" + Events._SYNC_ID + " = ?)";
		String[] selectionArgs = new String[] {caldavEventUri.toString()}; 
		Cursor curEvent = provider.query(uriEvents, null, selection, selectionArgs, null);
		
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
			
			androidEvent = new AndroidEvent(returnedUri, calendarUri);
			androidEvent.readContentValues(curEvent);
			
			selection = "(" + Attendees.EVENT_ID + " = ?)";
			selectionArgs = new String[] {String.valueOf(EventID)}; 
			curAttendee = provider.query(uriAttendee, null, selection, selectionArgs, null);
			selection = "(" + Reminders.EVENT_ID + " = ?)";
			selectionArgs = new String[] {String.valueOf(EventID)}; 
			curReminder = provider.query(uriReminder, null, selection, selectionArgs, null);
			androidEvent.readAttendees(curAttendee);
			androidEvent.readReminder(curReminder);
			curAttendee.close();
			curReminder.close();
		}
		curEvent.close();
		
		return androidEvent;
	}
	
	private static boolean isCalendarExist(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		boolean Result = false;
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
		
		if (cur != null) {
			Result = (cur.getCount() != 0);
			cur.close();
		}
		
		return Result;
	}

	static int calendarCount(Account account, ContentProviderClient provider) throws RemoteException {
		int Result = 0;
		Cursor cur = null;
		
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "";
		String[] selectionArgs = new String[] {}; 
		// Submit the query and get a Cursor object back. 
		cur = provider.query(uri, CALENDAR_PROJECTION, selection, selectionArgs, null);
		
		Result = cur.getCount();
		cur.close();
		
		return Result;
	}
	
	private static Uri getCalendarUri(Account account, ContentProviderClient provider, Calendar calendar) throws RemoteException {
		Uri Result = null;
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
			Result = null;
		} else {
			cur.moveToNext();
			Uri returnedUri = ContentUris.withAppendedId(uri, cur.getLong(cur.getColumnIndex(Calendars._ID)) );
			Result = returnedUri;
		}
		cur.close();
		
		return Result;
	}
	
	private static String getCalendarCTag(ContentProviderClient provider, Uri calendarUri) throws RemoteException {
		String Result = null;
		Cursor cur = null;
		
		// Submit the query and get a Cursor object back. 
		cur = provider.query(calendarUri, CALENDAR_PROJECTION, null, null, null);
		
		if (cur.getCount() == 0) {
			Result = null;
		} else {
			cur.moveToNext();
			String returnedCtag = cur.getString(cur.getColumnIndex(Calendars.CAL_SYNC1));
			Result = returnedCtag;
		}
		cur.close();
		
		return Result;
	}

	private void setCalendarCTag(Account account, ContentProviderClient provider,
			Uri calendarUri, String cTag) throws RemoteException {
		
		ContentValues mUpdateValues = new ContentValues();
		mUpdateValues.put(Calendars.CAL_SYNC1, cTag);
		
		provider.update(asSyncAdapter(calendarUri, account.name, account.type), mUpdateValues, null, null);
	}
	private void setCalendarColor(Account account, ContentProviderClient provider,
			Uri calendarUri, Calendar calendar) throws RemoteException {
	
		ContentValues mUpdateValues = new ContentValues();
		mUpdateValues.put(Calendars.CALENDAR_COLOR, calendar.getCalendarColorAsLong());
		
		provider.update(asSyncAdapter(calendarUri, account.name, account.type), mUpdateValues, null, null);
	}
	
	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }

}

