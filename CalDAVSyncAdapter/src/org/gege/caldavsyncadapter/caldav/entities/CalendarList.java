package org.gege.caldavsyncadapter.caldav.entities;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;

import org.gege.caldavsyncadapter.caldav.entities.DavCalendar.CalendarSource;
import org.gege.caldavsyncadapter.syncadapter.notifications.NotificationsHelper;

import java.net.URI;
import java.util.ArrayList;

//import org.gege.caldavsyncadapter.CalendarColors;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.util.Log;

public class CalendarList {
//	private static final String TAG = "CalendarList";

    private java.util.ArrayList<DavCalendar> mList = new java.util.ArrayList<DavCalendar>();

    private Account mAccount = null;
    private ContentProviderClient mProvider = null;

    public CalendarSource Source = CalendarSource.undefined;

    public String ServerUrl = "";

    public CalendarList(Account account, ContentProviderClient provider, CalendarSource source, String serverUrl) {
        this.mAccount = account;
        this.mProvider = provider;
        this.Source = source;
        this.ServerUrl = serverUrl;
    }

/*	public Calendar getCalendarByAndroidCalendarId(int calendarId) {
        Calendar Result = null;
		
		for (Calendar Item : mList) {
			if (Item.getAndroidCalendarId() == calendarId)
				Result = Item;
		}
		
		return Result;
	}*/

    public DavCalendar getCalendarByURI(URI calendarURI) {
        DavCalendar Result = null;

        for (DavCalendar Item : mList) {
            if (Item.getURI().equals(calendarURI))
                Result = Item;
        }

        return Result;
    }

    public DavCalendar getCalendarByAndroidUri(Uri androidCalendarUri) {
        DavCalendar Result = null;

        for (DavCalendar Item : mList) {
            if (Item.getAndroidCalendarUri().equals(androidCalendarUri))
                Result = Item;
        }

        return Result;
    }

    /**
     * function to get all calendars from client side android
     *
     * @return
     */
    public boolean readCalendarFromClient() {
        boolean Result = false;
        Cursor cur = null;

        Uri uri = Calendars.CONTENT_URI;

		/* COMPAT: in the past, the serverurl was not stored within a calendar. (see #98)
         * so there was no chance to see which calendars belongs to a named account.
		 * username + serverurl have to be unique
		 * ((DavCalendar.SERVERURL = ?) OR (DavCalendar.SERVERURL IS NULL))
		 */
        String selection = "(("
                + Calendars.ACCOUNT_NAME + " = ?) AND " +
                "(" + Calendars.ACCOUNT_TYPE + " = ?))";

        String[] selectionArgs = new String[]{
                mAccount.name,
                mAccount.type
        };

        String[] projection = new String[]{
                Calendars._ID,
                Calendars.NAME,
                Calendars.ACCOUNT_NAME,
                Calendars.ACCOUNT_TYPE
        };

        // Submit the query and get a Cursor object back.
        try {
            cur = mProvider.query(uri, null, selection, selectionArgs, Calendars._ID + " ASC");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (cur != null) {
            while (cur.moveToNext()) {
                mList.add(new DavCalendar(mAccount, mProvider, cur, this.Source, this.ServerUrl));
            }
            cur.close();
            Result = true;
        }

        return Result;
    }

    public boolean deleteCalendarOnClientSideOnly(android.content.Context context) {
        boolean Result = false;

        for (DavCalendar androidCalendar : this.mList) {
            if (!androidCalendar.foundServerSide) {
                NotificationsHelper.signalSyncErrors(context, "CalDAV Sync Adapter", "calendar deleted: " + androidCalendar
                        .getCalendarDisplayName());
                androidCalendar.deleteAndroidCalendar();
            }
        }

        return Result;
    }

    public void addCalendar(DavCalendar item) {
        item.setAccount(this.mAccount);
        item.setProvider(this.mProvider);
        item.ServerUrl = this.ServerUrl;
        this.mList.add(item);
    }

    public java.util.ArrayList<DavCalendar> getCalendarList() {
        return this.mList;
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public void setProvider(ContentProviderClient provider) {
        this.mProvider = provider;
    }

    public ArrayList<Uri> getNotifyList() {
        ArrayList<Uri> Result = new ArrayList<Uri>();

        for (DavCalendar cal : this.mList) {
            Result.addAll(cal.getNotifyList());
        }

        return Result;
    }
}
