package org.gege.caldavsyncadapter.caldav.entities;

import java.net.URI;

import org.gege.caldavsyncadapter.CalendarColors;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

public class CalendarList {
	private static final String TAG = "CalendarList";
	
	private java.util.ArrayList<Calendar> mList;
	
	private Account mAccount = null;
	private ContentProviderClient mProvider = null;
	
	public CalendarList() {
		mList = new java.util.ArrayList<Calendar>();
	}
	public CalendarList(Account account, ContentProviderClient provider) {
		mList = new java.util.ArrayList<Calendar>();
		mAccount = account;
		mProvider = provider;
	}
	
	public Calendar getCalendarById(int calendarId) {
		Calendar Result = null;
		
		for (Calendar Item : mList) {
			if (Item.getAndroidCalendarId() == calendarId)
				Result = Item;
		}
		
		return Result;
	}
	
	public Calendar getCalendarByURI(URI calendarURI) {
		Calendar Result = null;
		
		for (Calendar Item : mList) {
			if (Item.getURI().equals(calendarURI))
				Result = Item;
		}
		
		return Result;
	}
	
	/**
	 * function to get all calendars from client side android
	 * @return
	 */
	public boolean readCalendarFromClient() {
		boolean Result = false;
		Cursor cur = null;
		
		Uri uri = Calendars.CONTENT_URI;   
		String selection = "(" + "(" + Calendars.ACCOUNT_NAME +  " = ?) AND " + 
		                         "(" + Calendars.ACCOUNT_TYPE +  " = ?) AND " + 
		                         "(" + Calendars.OWNER_ACCOUNT + " = ?)" +
		                   ")";
		String[] selectionArgs = new String[] {	mAccount.name, 
												mAccount.type,
												mAccount.name
											}; 

		// Submit the query and get a Cursor object back. 
		try {
			cur = mProvider.query(uri, null, selection, selectionArgs, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		if (cur != null) {
			while (cur.moveToNext()) {
				mList.add(new Calendar(mAccount, mProvider, cur));
			}
			cur.close();
			Result = true;
		}
		
		return Result;
	}
	
	public Uri createNewAndroidCalendar(Calendar serverCalendar) {
		//boolean Result = false;
		Uri Result = null;
		
		final ContentValues contentValues = new ContentValues();
		//contentValues.put(Calendars.NAME, calendar.getURI().toString());
		contentValues.put(Calendar.URI, serverCalendar.getURI().toString());

		contentValues.put(Calendars.VISIBLE, 1);
		contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, serverCalendar.getCalendarDisplayName());
		contentValues.put(Calendars.OWNER_ACCOUNT, mAccount.name);
		contentValues.put(Calendars.ACCOUNT_NAME, mAccount.name);
		contentValues.put(Calendars.ACCOUNT_TYPE, mAccount.type);
		contentValues.put(Calendars.SYNC_EVENTS, 1);
		contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
		
		if (!serverCalendar.getCalendarColorAsString().equals("")) {
			contentValues.put(Calendars.CALENDAR_COLOR, serverCalendar.getCalendarColor());
		} else {
			// find a color
			int index = mList.size();
			index = index % CalendarColors.colors.length;
			contentValues.put(Calendars.CALENDAR_COLOR, CalendarColors.colors[index]);
		}

		try {
			Result = mProvider.insert(asSyncAdapter(Calendars.CONTENT_URI, mAccount.name, mAccount.type), contentValues);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// it is possible that this calendar already exists but the provider failed to find it within isCalendarExist()
		// the adapter would try to create a new calendar but the provider fails again to create a new calendar.
		if (Result != null) {
			long newCalendarId = ContentUris.parseId(Result);
			Log.v(TAG, "New calendar created : URI=" + Result + " id=" + newCalendarId);
		}
		
		return Result;
	}
	
	public boolean deleteCalendarOnlyOnClientSide() {
		boolean Result = false;
		
		for (Calendar androidCalendar : this.mList) {
			if (!androidCalendar.foundServerSide) {
				androidCalendar.deleteAndroidCalendar();
			}
		}
		
		return Result;
	}
	
	public void addCalendar(Calendar item) {
		mList.add(item);
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
}
