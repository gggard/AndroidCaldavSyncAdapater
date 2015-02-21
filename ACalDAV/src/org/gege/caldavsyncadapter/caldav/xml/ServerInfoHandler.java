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

import java.util.Arrays;
import java.util.List;

public class ServerInfoHandler extends DefaultHandler {

    private static final String HREF = "href";

    private static final String PRINCIPAL_URL = "principal-URL";

    private static final String CURRENT_USER_PRINCIPAL = "current-user-principal";

    private final static List<String> TAGS = Arrays.asList(
            CURRENT_USER_PRINCIPAL, PRINCIPAL_URL);

    public String currentUserPrincipal = null;

    public String principalUrl = null;

    private StringBuilder stringBuilder = new StringBuilder();

    private String inParentElement;

    private String currentElement;

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (TAGS.contains(localName)) {
            inParentElement = localName;
        }
        currentElement = localName;
        stringBuilder.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (HREF.equals(currentElement) && TAGS.contains(inParentElement)) {
            stringBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        //if (TAGS.contains(inParentElement)) {
        if (HREF.equals(currentElement) && TAGS.contains(inParentElement)) {
            if (CURRENT_USER_PRINCIPAL.equals(inParentElement)) {
                currentUserPrincipal = stringBuilder.toString();
            } else {
                principalUrl = stringBuilder.toString();
            }
        }
        if (TAGS.contains(localName)) {
            inParentElement = null;
            //stringBuilder.setLength(0);
        }
        currentElement = null;

    }
}
