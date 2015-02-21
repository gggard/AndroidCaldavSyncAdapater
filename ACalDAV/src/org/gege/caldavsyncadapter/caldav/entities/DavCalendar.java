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

import org.apache.http.client.ClientProtocolException;
import org.gege.caldavsyncadapter.CalendarColors;
import org.gege.caldavsyncadapter.Event;
import org.gege.caldavsyncadapter.android.entities.AndroidEvent;
import org.gege.caldavsyncadapter.caldav.CaldavFacade;
import org.gege.caldavsyncadapter.syncadapter.SyncAdapter;
import org.gege.caldavsyncadapter.syncadapter.notifications.NotificationsHelper;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SyncStats;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

public class DavCalendar {

    private static final String TAG = "Calendar";

    /**
     * stores the CTAG of a calendar
     */
    public static String CTAG = Calendars.CAL_SYNC1;

    /**
     * stores the URI of a calendar
     * example: http://caldav.example.com/calendarserver.php/calendars/username/calendarname
     */
    public static String URI = Calendars._SYNC_ID;

    public static String SERVERURL = Calendars.CAL_SYNC2;

    /**
     * the event transformed into ContentValues
     */
    public ContentValues ContentValues = new ContentValues();

    public boolean foundServerSide = false;

    public boolean foundClientSide = false;

    public CalendarSource Source = CalendarSource.undefined;

    public String ServerUrl = "";

    private String strCalendarColor = "";

    private ArrayList<Uri> mNotifyList = new ArrayList<Uri>();

    private Account mAccount = null;

    private ContentProviderClient mProvider = null;

    private ArrayList<CalendarEvent> mCalendarEvents = new ArrayList<CalendarEvent>();

    private int mTagCounter = 1;

    /**
     * empty constructor
     */
    public DavCalendar(CalendarSource source) {
        this.Source = source;
    }

    /**
     * creates an new instance from a cursor
     *
     * @param cur must be a cursor from "ContentProviderClient" with Uri Calendars.CONTENT_URI
     */
    public DavCalendar(Account account, ContentProviderClient provider, Cursor cur,
            CalendarSource source, String serverUrl) {
        this.mAccount = account;
        this.mProvider = provider;
        this.foundClientSide = true;
        this.Source = source;
        this.ServerUrl = serverUrl;

        String strSyncID = cur.getString(cur.getColumnIndex(Calendars._SYNC_ID));
        String strName = cur.getString(cur.getColumnIndex(Calendars.NAME));
        String strDisplayName = cur.getString(cur.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME));
        String strCTAG = cur.getString(cur.getColumnIndex(DavCalendar.CTAG));
        String strServerUrl = cur.getString(cur.getColumnIndex(DavCalendar.SERVERURL));
        String strCalendarColor = cur.getString(cur.getColumnIndex(Calendars.CALENDAR_COLOR));
        int intAndroidCalendarId = cur.getInt(cur.getColumnIndex(Calendars._ID));

        this.setCalendarName(strName);
        this.setCalendarDisplayName(strDisplayName);
        this.setCTag(strCTAG, false);
        this.setAndroidCalendarId(intAndroidCalendarId);
        this.setCalendarColor(Integer.parseInt(strCalendarColor));

        if (strSyncID == null) {
            this.correctSyncID(strName);
            strSyncID = strName;
        }
        if (strServerUrl == null) {
            this.correctServerUrl(serverUrl);
        }
        URI uri = null;
        try {
            uri = new URI(strSyncID);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.setURI(uri);
    }

    private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,
                        "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }

    /**
     * example: http://caldav.example.com/calendarserver.php/calendars/username/calendarname
     */
    public URI getURI() {
        String strUri = this.getContentValueAsString(DavCalendar.URI);
        URI result = null;
        try {
            result = new URI(strUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * example: http://caldav.example.com/calendarserver.php/calendars/username/calendarname
     */
    public void setURI(URI uri) {
        this.setContentValueAsString(DavCalendar.URI, uri.toString());
    }

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
    public void setCTag(String cTag, boolean Update) {
        this.setContentValueAsString(DavCalendar.CTAG, cTag);
        if (Update) {
            //serverCalendar.updateAndroidCalendar(androidCalendarUri, Calendar.CTAG, serverCalendar.getcTag());
            try {
                this.updateAndroidCalendar(this.getAndroidCalendarUri(), CTAG, cTag);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * example: 1143
     */
    public String getcTag() {
        return this.getContentValueAsString(DavCalendar.CTAG);
    }

    /**
     * example: #FFCCAA
     */
    public String getCalendarColorAsString() {
        return this.strCalendarColor;
    }

    /**
     * example: #FFCCAA
     */
    public void setCalendarColorAsString(String color) {
        int maxlen = 6;

        this.strCalendarColor = color;
        if (!color.equals("")) {
            String strColor = color.replace("#", "");
            if (strColor.length() > maxlen) {
                strColor = strColor.substring(0, maxlen);
            }
            int intColor = Integer.parseInt(strColor, 16);
            this.setContentValueAsInt(Calendars.CALENDAR_COLOR, intColor);
        }
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
     * should be: calendarname
     * but is:    http://caldav.example.com/calendarserver.php/calendars/username/calendarname/
     */
    public String getCalendarName() {
        return this.getContentValueAsString(Calendars.NAME);
    }

    /**
     * example:
     * should be: calendarname
     * but is:    http://caldav.example.com/calendarserver.php/calendars/username/calendarname/
     */
    public void setCalendarName(String calendarName) {
        this.setContentValueAsString(Calendars.NAME, calendarName);
    }

    /**
     * example: 8
     */
    public int getAndroidCalendarId() {
        return this.getContentValueAsInt(Calendars._ID);
    }

    /**
     * example: 8
     */
    public void setAndroidCalendarId(int androidCalendarId) {
        this.setContentValueAsInt(Calendars._ID, androidCalendarId);
    }

    /**
     * example: content://com.android.calendar/calendars/8
     */
    public Uri getAndroidCalendarUri() {
        return ContentUris.withAppendedId(Calendars.CONTENT_URI, this.getAndroidCalendarId());
    }

    /**
     * checks a given list of android calendars for a specific android calendar.
     * this calendar should be a server calendar as it is searched for.
     * if the calendar is not found, it will be created.
     *
     * @param androidCalList the list of android calendars
     * @return the found android calendar or null of fails
     */
    public Uri checkAndroidCalendarList(CalendarList androidCalList,
            android.content.Context context) throws RemoteException {
        Uri androidCalendarUri = null;
        boolean isCalendarExist = false;

        DavCalendar androidCalendar = androidCalList.getCalendarByURI(this.getURI());
        if (androidCalendar != null) {
            isCalendarExist = true;
            androidCalendar.foundServerSide = true;
        }

        if (!isCalendarExist) {
            DavCalendar newCal = this
                    .createNewAndroidCalendar(this, androidCalList.getCalendarList()
                            .size(), context);
            if (newCal != null) {
                androidCalList.addCalendar(newCal);
                androidCalendarUri = newCal.getAndroidCalendarUri();
            }
        } else {
            androidCalendarUri = androidCalendar.getAndroidCalendarUri();
            if (!this.getCalendarColorAsString().equals("")) {
                this.updateAndroidCalendar(androidCalendarUri, Calendars.CALENDAR_COLOR,
                        getColorAsHex(this.getCalendarColorAsString()));
            }
            if ((this.ContentValues.containsKey(Calendars.CALENDAR_DISPLAY_NAME)) &&
                    (androidCalendar.ContentValues.containsKey(Calendars.CALENDAR_DISPLAY_NAME))) {
                String serverDisplayName = this.ContentValues
                        .getAsString(Calendars.CALENDAR_DISPLAY_NAME);
                String clientDisplayName = androidCalendar.ContentValues
                        .getAsString(Calendars.CALENDAR_DISPLAY_NAME);
                if (!serverDisplayName.equals(clientDisplayName)) {
                    this.updateAndroidCalendar(androidCalendarUri, Calendars.CALENDAR_DISPLAY_NAME,
                            serverDisplayName);
                }
            }
        }

        return androidCalendarUri;
    }

    /**
     * COMPAT: the calendar Uri was stored as calendar Name. this function updates the URI
     * (_SYNC_ID)
     *
     * @param calendarUri the real calendarUri
     * @return success of this function
     */
    private boolean correctSyncID(String calendarUri) {
        boolean Result = false;
        Log.v(TAG, "correcting SyncID for calendar:" + this
                .getContentValueAsString(Calendars.CALENDAR_DISPLAY_NAME));

        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(DavCalendar.URI, calendarUri);

        try {
            mProvider.update(this.SyncAdapterCalendar(), mUpdateValues, null, null);
            Result = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return Result;
    }

    /**
     * COMPAT: the serverurl (CAL_SYNC2) was not sored within a calendar. this fixes it. (see #98)
     *
     * @param serverUrl the current serverurl
     * @return success of this function
     */
    private boolean correctServerUrl(String serverUrl) {
        boolean Result = false;
        Log.v(TAG, "correcting ServerUrl for calendar:" + this
                .getContentValueAsString(Calendars.CALENDAR_DISPLAY_NAME));

        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(DavCalendar.SERVERURL, serverUrl);

        try {
            mProvider.update(this.SyncAdapterCalendar(), mUpdateValues, null, null);
            Result = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return Result;
    }

    /**
     * creates a new androidCalendar
     *
     * @return the new androidCalendar or null if fails
     */
    private DavCalendar createNewAndroidCalendar(DavCalendar serverCalendar, int index,
            android.content.Context context) {
        Uri newUri = null;
        DavCalendar Result = null;

        final ContentValues contentValues = new ContentValues();
        contentValues.put(DavCalendar.URI, serverCalendar.getURI().toString());
        contentValues.put(DavCalendar.SERVERURL, this.ServerUrl);

        contentValues.put(Calendars.VISIBLE, 1);
        contentValues.put(Calendars.CALENDAR_DISPLAY_NAME, serverCalendar.getCalendarDisplayName());
        contentValues.put(Calendars.ACCOUNT_NAME, mAccount.name);
        contentValues.put(Calendars.ACCOUNT_TYPE, mAccount.type);
        contentValues.put(Calendars.OWNER_ACCOUNT, mAccount.name);
        contentValues.put(Calendars.SYNC_EVENTS, 1);
        contentValues.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);

        String calendarColorAsString = serverCalendar.getCalendarColorAsString();
        if (!calendarColorAsString.isEmpty()) {
            int color = getColorAsHex(calendarColorAsString);
            contentValues.put(Calendars.CALENDAR_COLOR, color);
        } else {
            // find a color
            //int index = mList.size();
            index = index % CalendarColors.colors.length;
            contentValues.put(Calendars.CALENDAR_COLOR, CalendarColors.colors[2]);
        }

        try {
            newUri = mProvider
                    .insert(asSyncAdapter(Calendars.CONTENT_URI, mAccount.name, mAccount.type),
                            contentValues);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // it is possible that this calendar already exists but the provider failed to find it within isCalendarExist()
        // the adapter would try to create a new calendar but the provider fails again to create a new calendar.
        if (newUri != null) {
            long newCalendarId = ContentUris.parseId(newUri);

            Cursor cur = null;
            Uri uri = Calendars.CONTENT_URI;
            String selection = "(" + Calendars._ID + " = ?)";
            String[] selectionArgs = new String[]{String.valueOf(newCalendarId)};

            // Submit the query and get a Cursor object back.
            try {
                cur = mProvider.query(uri, null, selection, selectionArgs, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (cur != null) {
                while (cur.moveToNext()) {
                    Result = new DavCalendar(mAccount, mProvider, cur, this.Source, this.ServerUrl);
                    Result.foundServerSide = true;
                }
                cur.close();
                //if (Result != null)
                //	this.mList.add(Result);
            }
            Log.i(TAG, "New calendar created : URI=" + Result.getAndroidCalendarUri());
            NotificationsHelper.signalSyncErrors(context, "CalDAV Sync Adapter",
                    "new calendar found: " + Result
                            .getCalendarDisplayName());
            mNotifyList.add(Result.getAndroidCalendarUri());
        }

        return Result;
    }

    private int getColorAsHex(String calendarColorAsString) {
        int color = 0x0000FF00;
        Pattern p = Pattern.compile("#?(\\p{XDigit}{6})(\\p{XDigit}{2})?");
        Matcher m = p.matcher(calendarColorAsString);
        if (m.find()) {
            int color_rgb = Integer.parseInt(m.group(1), 16);
            int color_alpha = m.group(2) != null ? (Integer.parseInt(m.group(2), 16) & 0xFF) : 0xFF;
            color = (color_alpha << 24) | color_rgb;
        }
        return color;
    }

    /**
     * there is no corresponding calendar on server side. time to delete this calendar on android
     * side.
     */
    public boolean deleteAndroidCalendar() {
        boolean Result = false;

        String mSelectionClause = "(" + Calendars._ID + " = ?)";
        int calendarId = this.getAndroidCalendarId();
        String[] mSelectionArgs = {Long.toString(calendarId)};

        int CountDeleted = 0;
        try {
            CountDeleted = mProvider.delete(this.SyncAdapter(), mSelectionClause, mSelectionArgs);
            Log.i(TAG, "Calendar deleted: " + String.valueOf(calendarId));
            this.mNotifyList.add(this.getAndroidCalendarUri());
            Result = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Android Calendars deleted: " + Integer.toString(CountDeleted));

        return Result;
    }

    /**
     * updates the android calendar
     *
     * @param calendarUri the uri of the androidCalendar
     * @param target      must be from android.provider.CalendarContract.Calendars
     * @param value       the new value for the target
     */
    private void updateAndroidCalendar(Uri calendarUri, String target, int value)
            throws RemoteException {
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(target, value);

        mProvider.update(asSyncAdapter(calendarUri, mAccount.name, mAccount.type), mUpdateValues,
                null, null);
    }

    /**
     * updates the android calendar
     *
     * @param calendarUri the uri of the androidCalendar
     * @param target      must be from android.provider.CalendarContract.Calendars
     * @param value       the new value for the target
     */
    private void updateAndroidCalendar(Uri calendarUri, String target, String value)
            throws RemoteException {
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(target, value);

        mProvider.update(asSyncAdapter(calendarUri, mAccount.name, mAccount.type), mUpdateValues,
                null, null);
    }

    /**
     * marks the android event as already handled
     *
     * @see AndroidEvent#cInternalTag
     * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * DavCalendar, SyncStats)
     */
    public boolean tagAndroidEvent(AndroidEvent androidEvent) throws RemoteException {
        boolean Result = false;

        ContentValues values = new ContentValues();
        //values.put(Event.INTERNALTAG, 1);
        values.put(Event.INTERNALTAG, mTagCounter);
        //values.put(Event.INTERNALTAG, String.valueOf(mTagCounter));

        int RowCount = this.mProvider
                .update(asSyncAdapter(androidEvent.getUri(), this.mAccount.name,
                        this.mAccount.type), values, null, null);
        //Log.v(TAG,"event tag nr: " + String.valueOf(mTagCounter));
        //Log.v(TAG,"Rows updated: " + String.valueOf(RowCount));

        if (RowCount == 1) {
            Result = true;
            mTagCounter += 1;
        } else {
            Log.v(TAG, "EVENT NOT TAGGED!");
        }

        return Result;
    }

    /**
     * removes the tag of all android events
     *
     * @see AndroidEvent#cInternalTag
     * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * DavCalendar, SyncStats)
     */
    public int untagAndroidEvents() throws RemoteException {
        int RowCount = 0;
        int Steps = 100;
        ContentValues values = new ContentValues();
        values.put(Event.INTERNALTAG, 0);

        for (int i = 1; i < this.mTagCounter; i = i + Steps) {
            String mSelectionClause = "(CAST(" + Event.INTERNALTAG + " AS INT) >= ?) AND (CAST("
                    + Event.INTERNALTAG + " AS INT) < ?) AND (" + Events.CALENDAR_ID + " = ?)";
            String[] mSelectionArgs = {String.valueOf(i), String.valueOf(i + Steps),
                    Long.toString(ContentUris
                            .parseId(this.getAndroidCalendarUri()))};
            RowCount += this.mProvider.update(asSyncAdapter(Events.CONTENT_URI, this.mAccount.name,
                    this.mAccount.type), values, mSelectionClause, mSelectionArgs);
        }
        /*String mSelectionClause = "(" + Event.INTERNALTAG +  " > ?) AND (" + Events.CALENDAR_ID + " = ?)";
        String[] mSelectionArgs = {"0", Long.toString(ContentUris.parseId(this.getAndroidCalendarUri()))};
		RowCount += this.mProvider.update(asSyncAdapter(Events.CONTENT_URI, this.mAccount.name, this.mAccount.type), values, mSelectionClause, mSelectionArgs);*/

        //Log.d(TAG, "Rows reseted: " + RowCount.toString());
        return RowCount;
    }

    /**
     * Events not being tagged are for deletion
     *
     * @see AndroidEvent#cInternalTag
     * @see SyncAdapter#synchroniseEvents(CaldavFacade, Account, ContentProviderClient, Uri,
     * DavCalendar, SyncStats)
     */
    public int deleteUntaggedEvents() throws RemoteException {
        String mSelectionClause = "(" + Event.INTERNALTAG + " < ?) AND (" + Events.CALENDAR_ID
                + " = ?)";
        String[] mSelectionArgs = {"1",
                Long.toString(ContentUris.parseId(this.getAndroidCalendarUri()))};

        int CountDeleted = this.mProvider
                .delete(asSyncAdapter(Events.CONTENT_URI, this.mAccount.name, this.mAccount.type),
                        mSelectionClause, mSelectionArgs);
        //Log.d(TAG, "Rows deleted: " + CountDeleted.toString());
        return CountDeleted;
    }

    private Uri SyncAdapterCalendar() {
        return asSyncAdapter(this.getAndroidCalendarUri(), mAccount.name, mAccount.type);
    }

    private Uri SyncAdapter() {
        return asSyncAdapter(Calendars.CONTENT_URI, mAccount.name, mAccount.type);
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public void setProvider(ContentProviderClient provider) {
        this.mProvider = provider;
    }

    /**
     * general access function to ContentValues
     *
     * @param Item the item name from Calendars.*
     * @return the value for the item
     */
    private String getContentValueAsString(String Item) {
        String Result = "";
        if (this.ContentValues.containsKey(Item)) {
            Result = this.ContentValues.getAsString(Item);
        }
        return Result;
    }

    /**
     * general access function to ContentValues
     *
     * @param Item the item name from Calendars.*
     * @return the value for the item
     */
    private int getContentValueAsInt(String Item) {
        int Result = 0;
        if (this.ContentValues.containsKey(Item)) {
            Result = this.ContentValues.getAsInteger(Item);
        }
        return Result;
    }

    /**
     * general access function to ContentValues
     *
     * @param Item  the item name from Calendars.*
     * @param Value the value for the item
     * @return success of this function
     */
    private boolean setContentValueAsString(String Item, String Value) {
        boolean Result = false;

        if (this.ContentValues.containsKey(Item)) {
            this.ContentValues.remove(Item);
        }
        this.ContentValues.put(Item, Value);

        return Result;
    }

    /**
     * general access function to ContentValues
     *
     * @param Item  the item name from Calendars.*
     * @param Value the value for the item
     * @return success of this function
     */
    private boolean setContentValueAsInt(String Item, int Value) {
        boolean Result = false;

        if (this.ContentValues.containsKey(Item)) {
            this.ContentValues.remove(Item);
        }
        this.ContentValues.put(Item, Value);

        return Result;
    }

    public ArrayList<Uri> getNotifyList() {
        return this.mNotifyList;
    }

    public ArrayList<CalendarEvent> getCalendarEvents() {
        return this.mCalendarEvents;
    }

    public boolean readCalendarEvents(CaldavFacade facade) {
        boolean Result = false;

        try {
            this.mCalendarEvents = facade.getCalendarEvents(this);
            Result = true;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Result = false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Result = false;
        } catch (IOException e) {
            e.printStackTrace();
            Result = false;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            Result = false;
        } catch (SAXException e) {
            e.printStackTrace();
            Result = false;
        }

        return Result;
    }

    public enum CalendarSource {
        undefined, Android, CalDAV
    }

}
