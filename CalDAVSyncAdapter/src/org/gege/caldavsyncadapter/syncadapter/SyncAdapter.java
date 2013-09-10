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
import org.gege.caldavsyncadapter.Constants;
import org.gege.caldavsyncadapter.Event;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.caldav.CaldavProtocolException;
import org.gege.caldavsyncadapter.caldav.entities.DavCalendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.caldav.entities.CalendarList;
import org.gege.caldavsyncadapter.caldav.entities.DavCalendar.CalendarSource;
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
import android.content.pm.PackageManager.NameNotFoundException;
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
	private String mVersion = "";
//	private Context mContext;
	
	
/*	private static final String[] CALENDAR_PROJECTION = new String[] {
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT,                 // 3
	    Calendar.CTAG                            // 4
	};*/
	  
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
	//private static final boolean FORCE_SYNCHRONIZE = false;
	// drop all calendar before synchro
	//private static final boolean DROP_CALENDAR_EVENTS = false;
	
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		//android.os.Debug.waitForDebugger();
		mAccountManager = AccountManager.get(context);
		try {
			mVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
//		mContext = context;
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		boolean bolError = false;
		String url = mAccountManager.getUserData(account, Constants.USER_DATA_URL_KEY);
		Log.v(TAG, "onPerformSync() on " + account.name + " with URL " + url);

		CalendarList serverCalList;
		
		CalendarList androidCalList = new CalendarList(account, provider, CalendarSource.Android);
		androidCalList.readCalendarFromClient();

		try {
			CaldavFacade facade = new CaldavFacade(account.name, mAccountManager.getPassword(account), url);
			facade.setAccount(account);
			facade.setProvider(provider);
			facade.setVersion(mVersion);
			serverCalList = facade.getCalendarList(this.getContext());
			//String davProperties = facade.getLastDav();
			Log.i(TAG, String.valueOf(androidCalList.getCalendarList().size()) + " calendars found at android");
			
			for (DavCalendar serverCalendar : serverCalList.getCalendarList()) {
				Log.i(TAG, "Detected calendar name=" + serverCalendar.getCalendarDisplayName() + " URI=" + serverCalendar.getURI());

				Uri androidCalendarUri = serverCalendar.checkAndroidCalendarList(androidCalList, this.getContext());

				// check if the adapter was able to get an existing calendar or create a new one
				if (androidCalendarUri != null) {
					DavCalendar androidCalendar = androidCalList.getCalendarByAndroidUri(androidCalendarUri);
					
					//if ((FORCE_SYNCHRONIZE) || (androidCalendar.getcTag() == null) || (!androidCalendar.getcTag().equals(serverCalendar.getcTag()))) {
					if ((androidCalendar.getcTag() == null) || (!androidCalendar.getcTag().equals(serverCalendar.getcTag()))) {
							Log.d(TAG, "CTag has changed, something to synchronise");
							
							this.synchroniseEvents(facade, account, provider, androidCalendar, serverCalendar, syncResult.stats);
							
							Log.d(TAG, "Updating stored CTag");
							//serverCalendar.updateAndroidCalendar(androidCalendarUri, Calendar.CTAG, serverCalendar.getcTag());
							androidCalendar.setCTag(serverCalendar.getcTag(), true);
					} else {
						Log.d(TAG, "CTag has not changed, nothing to do");
	
						long CalendarID = ContentUris.parseId(androidCalendarUri);
						String selection = "(" + Events.CALENDAR_ID + " = ?)";
						String[] selectionArgs = new String[] {String.valueOf(CalendarID)}; 
						Cursor countCursor = provider.query(Events.CONTENT_URI, new String[] {"count(*) AS count"},
					                selection,
					                selectionArgs,
					                null);
	
				        countCursor.moveToFirst();
				        int count = countCursor.getInt(0);
				        syncResult.stats.numSkippedEntries += count;
				        countCursor.close();
					}
					
					this.checkDirtyAndroidEvents(provider, account, androidCalendarUri, facade, serverCalendar.getURI(), syncResult.stats);
				} else {
					// this happens if the data provider failes to get an existing or create a new calendar
					Log.e(TAG, "failed to get an existing or create a new calendar");
					syncResult.stats.numIoExceptions += 1;
					NotificationsHelper.signalSyncErrors(this.getContext(), "Caldav sync error (provider failed)", "the provider failed to get an existing or create a new calendar");
					bolError = true;
				}
			}
			
			if (!bolError) {
				// check whether a calendar is not synced -> delete it at android
				androidCalList.deleteCalendarOnClientSideOnly(this.getContext());
			}

		/*} catch (final AuthenticatorException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "AuthenticatorException", e);*/
        /*} catch (final OperationCanceledException e) {
            Log.e(TAG, "OperationCanceledExcetpion", e);*/
        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
            NotificationsHelper.signalSyncErrors(this.getContext(), "Caldav sync error (IO)", e.getMessage());
            //NotificationsHelper.getCurrentSyncLog().addException(e);
            /*} catch (final AuthenticationException e) {
            //mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, authtoken);
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthenticationException", e);*/
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
            NotificationsHelper.signalSyncErrors(this.getContext(), "Caldav sync error (parsing)", e.getMessage());
            //NotificationsHelper.getCurrentSyncLog().addException(e);
        /*} catch (final JSONException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "JSONException", e);*/
		} catch (Exception e) {
			Log.e(TAG, "Updating calendar exception " + e.getClass().getName(), e);
            syncResult.stats.numParseExceptions++;
            NotificationsHelper.signalSyncErrors(this.getContext(), "Caldav sync error (general)", e.getMessage());
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
	 * @param androidCalendar
	 * @param serverCalendar
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
			ContentProviderClient provider, DavCalendar androidCalendar, DavCalendar serverCalendar, SyncStats stats) throws ClientProtocolException, URISyntaxException, IOException, ParserConfigurationException, SAXException, RemoteException, CaldavProtocolException, ParserException {
		
		/*if (DROP_CALENDAR_EVENTS) {
			dropAllEvents(account, provider, androidCalendar.getAndroidCalendarUri());
		}*/
		
		Iterable<CalendarEvent> eventList = facade.getCalendarEvents(serverCalendar);
		
		int rowInsert = 0;
		int rowUpdate = 0;
		int rowTag = 0;
		int rowDelete = 0;
		int rowUntag = 0;
		int rowSkip = 0;
		
		for (CalendarEvent calendarEvent : eventList) {
			
			try {
				AndroidEvent androidEvent = calendarEvent.getAndroidEvent(androidCalendar);
				
				Log.i(TAG, "Event " + calendarEvent.getUri().toString()+ " androidUri="+androidEvent);
				
				if (androidEvent == null) {
					/* new android event */
					if (calendarEvent.createAndroidEvent(androidCalendar)) {
						rowInsert += 1;
						androidEvent = calendarEvent.getAndroidEvent(androidCalendar);
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
						if (calendarEvent.updateAndroidEvent(androidEvent))
							rowUpdate += 1;
					}
				}
				if (androidEvent != null)
					if (androidEvent.tagAndroidEvent())
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
		
		rowDelete = androidCalendar.deleteUntaggedEvents();
		rowUntag = androidCalendar.untagAndroidEvents();

		Log.i(TAG,"Statistiks for Calendar: " + serverCalendar.getURI().toString());
		Log.i(TAG,"Statistiks for AndroidCalendar: " + androidCalendar.getAndroidCalendarUri().toString());
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
				
				androidEvent = new AndroidEvent(account, provider, returnedUri, calendarUri);
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
					String newGUID = java.util.UUID.randomUUID().toString() + "-caldavsyncadapter";
					String calendarPath = caldavCalendarUri.getPath();
					if (!calendarPath.endsWith("/"))
						calendarPath += "/";

					SyncID = calendarPath + newGUID + ".ics";
					
					androidEvent.createIcs(newGUID);
					
					if (facade.createEvent(URI.create(SyncID), androidEvent.getIcsEvent().toString())) {
						//HINT: bugfix for google calendar
						if (SyncID.contains("@"))
							SyncID = SyncID.replace("@", "%40");
						ContentValues values = new ContentValues();
						values.put(Events._SYNC_ID, SyncID);
						values.put(Event.ETAG, facade.getLastETag());
						values.put(Event.UID, newGUID);
						values.put(Events.DIRTY, 0);
						values.put(Event.RAWDATA, androidEvent.getIcsEvent().toString());
						
						int rowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), values, null, null);
						if (rowCount == 1)
							rowInsert += 1;
					}
				} else if (Deleted) {
					// deleted Android event
					if (facade.deleteEvent(URI.create(SyncID), androidEvent.getETag())) {
						String mSelectionClause = "(" + Events._ID +  "= ?)";
						String[] mSelectionArgs = {String.valueOf(EventID)};
						
						int countDeleted = provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), mSelectionClause, mSelectionArgs);	
						
						if (countDeleted == 1)
							rowDelete += 1;
					}
				} else {
					//update the android event to the server
					String uid = androidEvent.getUID();
					if ((uid == null) || (uid.equals(""))) {
						//this is needed because in the past, the UID was not stored in the android event
						CalendarEvent calendarEvent = new CalendarEvent(account, provider);
						URI syncURI = new URI(SyncID);
						calendarEvent.setUri(syncURI);
						if (calendarEvent.fetchBody()) {
							calendarEvent.readContentValues();
							uid = calendarEvent.getUID();
						}
					}
					if (uid != null) {
						androidEvent.createIcs(uid);
							
						if (facade.updateEvent(URI.create(SyncID), androidEvent.getIcsEvent().toString(), androidEvent.getETag())) {
							selection = "(" + Events._ID + "= ?)";
							selectionArgs = new String[] {EventID.toString()};
							androidEvent.ContentValues.put(Events.DIRTY, 0);
							androidEvent.ContentValues.put(Event.RAWDATA, androidEvent.getIcsEvent().toString());
							int RowCount = provider.update(asSyncAdapter(androidEvent.getUri(), account.name, account.type), androidEvent.ContentValues, null, null);
			
							if (RowCount == 1)
								rowUpdate += 1;
						} else {
							rowDirty += 1;
						}
					} else {
						rowDirty += 1;
					}
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
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (CaldavProtocolException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (ParserException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		
		return rowDirty;
	}
	
/*	private void dropAllEvents(Account account, ContentProviderClient provider,	Uri calendarUri) throws RemoteException {
		
		Log.i(TAG, "Deleting all calendar events for "+calendarUri);
		
		String selection = "(" + Events.CALENDAR_ID + " = ?)";
		String[] selectionArgs = new String[] {Long.toString(ContentUris.parseId(calendarUri))}; 
		
		provider.delete(asSyncAdapter(Events.CONTENT_URI, account.name, account.type), 
				        selection, selectionArgs);
		
	}*/
	
	private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
	    return uri.buildUpon()
	        .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
	        .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
	        .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
	 }

}

