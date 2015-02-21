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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import de.jdevel.acaldav.BuildConfig;

public class CalendarHomeHandler extends DefaultHandler {

    private static final String HREF = "href";

    private static final String CALENDAR_HOME_SET = "calendar-home-set";

    public List<URI> calendarHomeSet = new ArrayList<URI>();

    private boolean isInCalendarHomeSet = false;

    private StringBuilder stringBuilder = new StringBuilder();

    private String currentElement;

    private URI principalURI;

    public CalendarHomeHandler(URI principalURI) {
        this.principalURI = principalURI;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (CALENDAR_HOME_SET.equals(localName)) {
            isInCalendarHomeSet = true;
        }
        currentElement = localName;
        stringBuilder.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (HREF.equals(currentElement) && isInCalendarHomeSet) {
            stringBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (HREF.equals(localName) && isInCalendarHomeSet) {
            String calendarHomeSet = stringBuilder.toString();
            try {
                URI calendarHomeSetURI = new URI(calendarHomeSet);
                calendarHomeSetURI = principalURI.resolve(calendarHomeSetURI);
                this.calendarHomeSet.add(calendarHomeSetURI);
            } catch (URISyntaxException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(CalendarHomeHandler.class.getSimpleName(),
                            "uri malformed: " + calendarHomeSet);
                } else {
                    Log.e(CalendarHomeHandler.class.getSimpleName(),
                            "uri malformed in calendar-home-set/href");
                }
            }
            //stringBuilder.setLength(0);
        }
        if (CALENDAR_HOME_SET.equals(localName)) {
            isInCalendarHomeSet = false;
        }
        currentElement = null;
    }
}
