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

package org.gege.caldavsyncadapter.caldav.xml;

import org.gege.caldavsyncadapter.caldav.entities.DavCalendar;
import org.gege.caldavsyncadapter.caldav.entities.DavCalendar.CalendarSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jdevel.acaldav.BuildConfig;

public class CalendarsHandler extends DefaultHandler {

    private static final String CALENDAR = "calendar";

    private static final String RESOURCETYPE = "resourcetype";

    private static final String CALENDAR_COLOR = "calendar-color";

    private static final String GETCTAG = "getctag";

    private static final String DISPLAYNAME = "displayname";

    private static final String RESPONSE = "response";

    private static final String HREF = "href";

    public final static List<String> TAGS = Arrays.asList(HREF, RESOURCETYPE,
            DISPLAYNAME, GETCTAG, CALENDAR_COLOR);

    public List<DavCalendar> calendars = new ArrayList<DavCalendar>();

    private URI homeURI;

    private StringBuilder stringBuilder = new StringBuilder();

    private String currentElement;

    private DavCalendar calendar;

    private boolean isInResourceType = false;

    private boolean isCalendarResource;

    public CalendarsHandler(URI homeURI) {
        this.homeURI = homeURI;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (RESPONSE.equals(localName)) {
            calendar = new DavCalendar(CalendarSource.CalDAV);
            isCalendarResource = false;
        } else if (RESOURCETYPE.equals(localName)) {
            isInResourceType = true;
        } else if (isInResourceType && CALENDAR.equals(localName)) {
            isCalendarResource = true;
        }
        currentElement = localName;
        stringBuilder.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (TAGS.contains(currentElement)) {
            stringBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (TAGS.contains(localName)) {
            if (calendar != null) {
                if (HREF.equals(localName)) {
                    String calendarUrl = stringBuilder.toString();
                    calendarUrl = calendarUrl.trim();
                    try {
                        URI calendarURI = new URI(calendarUrl);
                        calendar.setURI(homeURI.resolve(calendarURI));
                    } catch (URISyntaxException e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(CalendarsHandler.class.getSimpleName(),
                                    "calendar-uri malformed: " + calendarUrl);
                        } else {
                            Log.e(CalendarsHandler.class.getSimpleName(),
                                    "uri malformed in href");
                        }
                    }
                } else if (DISPLAYNAME.equals(localName)) {
                    calendar.setCalendarDisplayName(stringBuilder.toString());
                } else if (GETCTAG.equals(localName)) {
                    calendar.setCTag(stringBuilder.toString(), false);
                } else if (CALENDAR_COLOR.equals(localName)) {
                    calendar.setCalendarColorAsString(stringBuilder.toString());
                }
            }
            //stringBuilder.setLength(0);
        } else if (RESPONSE.equals(localName)) {
            if (isCalendarResource && isValidCalendar(calendar)) {
                calendars.add(calendar);
            }
        }
        currentElement = null;
    }

    private boolean isValidCalendar(DavCalendar calendar) {
        return calendar != null && calendar.getURI() != null
                && calendar.getcTag() != null
                && calendar.getCalendarDisplayName() != null;
    }
}
