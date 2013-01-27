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

package org.osaf.caldav4j.methods;

import java.util.ArrayList;
import java.util.List;

import org.apache.webdav.lib.methods.XMLResponseMethodBase;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.model.request.CalendarDescription;
import org.osaf.caldav4j.model.request.DisplayName;
import org.osaf.caldav4j.model.request.MkCalendar;
import org.osaf.caldav4j.model.request.Prop;
import org.osaf.caldav4j.model.request.PropProperty;
import org.osaf.caldav4j.util.UrlUtils;
import org.osaf.caldav4j.util.XMLUtils;
import org.w3c.dom.Document;

public class MkCalendarMethod extends XMLResponseMethodBase{
    
	
	/**
	 * Standard calendar properties
	 */
	
	protected String CALENDAR_DESCRIPTION = "calendar-description";
    /**
     * Map of the properties to set.
     */
    protected List<PropProperty> propertiesToSet = new ArrayList<PropProperty>();

    // --------------------------------------------------------- Public Methods

    public MkCalendarMethod() {
		// Add Headers Content-Type: text/xml
    	
    	addRequestHeader(CalDAVConstants.HEADER_CONTENT_TYPE, CalDAVConstants.CONTENT_TYPE_TEXT_XML);
	}

    public void addDisplayName(String s) {
    	propertiesToSet.add(new DisplayName(s));
    }
    public void addDescription(String description, String lang) {
    	propertiesToSet.add(new CalendarDescription(description, lang));
    }
    public void addDescription(String description) {
    	propertiesToSet.add(new CalendarDescription(description));
    }
    /**
     * 
     */
    public void addPropertyToSet(String namespaceURI, String qualifiedName,
            String value) {
        checkNotUsed();
        PropProperty propertyToSet = new PropProperty();
        propertyToSet.setQualifiedName(qualifiedName);
        propertyToSet.setTextContent(value);
        propertyToSet.setNamespaceURI(namespaceURI);
        propertiesToSet.add(propertyToSet);
    }

    // remove double slashes
	public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
    
    // --------------------------------------------------- WebdavMethod Methods

    public String getName() {
        return CalDAVConstants.METHOD_MKCALENDAR;
    }
    
    /**
     *
     */
    protected String generateRequestBody() {
        if (propertiesToSet.size() == 0 ){
            return null;
        }
        
        Prop prop = new Prop(CalDAVConstants.NS_QUAL_DAV, propertiesToSet);
        MkCalendar mkCalendar = new MkCalendar("C",CalDAVConstants.NS_QUAL_DAV,prop);
        Document d = null;
        try {
            d = mkCalendar.createNewDocument(XMLUtils
                    .getDOMImplementation());
        } catch (DOMValidationException domve) {
            throw new RuntimeException(domve);
        }
        return XMLUtils.toPrettyXML(d);

    }
}
