/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.caldav4j.model.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 *
 *   <!ELEMENT mkcalendar (DAV:set)>
 *   <!ELEMENT set (prop) >
 * @author bobbyrullo
 *
 */
public class MkCalendar extends OutputsDOMBase{
    public static final String ELEMENT_NAME = "mkcalendar";
    public static final String SET_ELEMENT_NAME = "set";
    
    private String caldavNamespaceQualifier = null;
    private String webdavNamespaceQualifier = null;
    private Prop prop = null;
    
    public MkCalendar(String caldavNamespaceQualifier, String webdavNamespaceQualifier, Prop prop){
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.webdavNamespaceQualifier = webdavNamespaceQualifier;
        this.prop = prop;
    }
    
    protected String getElementName() {
        return ELEMENT_NAME;
    }

    protected String getNamespaceQualifier() {
        return caldavNamespaceQualifier;
    }

    protected String getNamespaceURI() {
        return CalDAVConstants.NS_CALDAV;
    }

    protected Collection<OutputsDOM> getChildren() {
        Collection<OutputsDOM> c  = new ArrayList<OutputsDOM>();
        SimpleDOMOutputtingObject set = new SimpleDOMOutputtingObject();
        set.addChild(prop);
        set.setElementName(SET_ELEMENT_NAME);
        set.setNamespaceQualifier(webdavNamespaceQualifier);
        set.setNamespaceURI(CalDAVConstants.NS_DAV);
        c.add(set);
        return c;
    }

    protected String getTextContent() {
        return null;
    }
    protected Map<String, String> getAttributes() {
        return null;
    }
}
