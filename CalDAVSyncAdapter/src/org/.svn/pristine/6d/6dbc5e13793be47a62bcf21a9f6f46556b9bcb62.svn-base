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
import java.util.List;
import java.util.Map;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 * 
 * <!ELEMENT calendar-query (DAV:allprop | DAV:propname | DAV:prop)?
 *                            filter> 
 *
 * <!ELEMENT filter comp-filter>
 * 
 * <!ELEMENT comp-filter (is-defined | time-range)?
 *                         comp-filter* prop-filter*>
 *
 *  <!ATTLIST comp-filter name CDATA #REQUIRED>
 *  @author bobbyrullo
 * 
 */
public class CalendarQuery extends OutputsDOMBase implements CalDAVReportRequest{
    
    public static final String ELEMENT_NAME = "calendar-query";
    public static final String ELEM_ALLPROP = "allprop";    
    public static final String ELEM_PROPNAME = "propname";
    public static final String ELEM_FILTER = "filter";
    
    private String caldavNamespaceQualifier = null;
    private String webdavNamespaceQualifier = null;
    private boolean allProp = false;
    private boolean propName = false;
    private List<PropProperty> properties = new ArrayList<PropProperty>();
    private CompFilter compFilter = null;
    private CalendarData calendarDataProp = null;
    
    public CalendarQuery(String caldavNamespaceQualifier, String webdavNamespaceQualifer) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.webdavNamespaceQualifier = webdavNamespaceQualifer;
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
        ArrayList<OutputsDOM> children = new ArrayList<OutputsDOM>();
        if (allProp){
            children.add(new SimpleDOMOutputtingObject(CalDAVConstants.NS_DAV,
                    webdavNamespaceQualifier, ELEM_ALLPROP));
        } else if (propName){
            children.add(new SimpleDOMOutputtingObject(CalDAVConstants.NS_DAV,
                    webdavNamespaceQualifier, ELEM_PROPNAME));
        } else if ((properties != null && properties.size() > 0)
                || calendarDataProp != null) {
            Prop prop = new Prop(webdavNamespaceQualifier, properties);
            children.add(prop);
            if (calendarDataProp != null){
              prop.getChildren().add(calendarDataProp);
            }
        }
        
        if (compFilter != null) {
            SimpleDOMOutputtingObject filter = new SimpleDOMOutputtingObject(
                    CalDAVConstants.NS_CALDAV, caldavNamespaceQualifier,
                    ELEM_FILTER);
            filter.addChild(compFilter);
            children.add(filter);
        }
       return children;
    }

    protected String getTextContent() {
        return null;
    }

    public boolean isAllProp() {
        return allProp;
    }

    public void setAllProp(boolean allProp) {
        this.allProp = allProp;
    }

    public boolean isPropName() {
        return propName;
    }

    public void setPropName(boolean propName) {
        this.propName = propName;
    }

    public List<PropProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<PropProperty> properties) {
        this.properties = properties;
    }
    
    public void addProperty(PropProperty propProperty){
        properties.add(propProperty);
    }
    
    public void addProperty(String namespaceURI, String namespaceQualifier,
            String propertyName) {
        PropProperty propProperty = new PropProperty(namespaceURI,
                namespaceQualifier, propertyName);
        properties.add(propProperty);
    }
    protected Map<String, String> getAttributes() {
        return null;
    }

    public CompFilter getCompFilter() {
        return compFilter;
    }

    public void setCompFilter(CompFilter compFilter) {
        this.compFilter = compFilter;
    }

    public CalendarData getCalendarDataProp() {
        return calendarDataProp;
    }

    public void setCalendarDataProp(CalendarData calendarDataProp) {
        this.calendarDataProp = calendarDataProp;
    }
    
    /**
     * Validates that the object validates against the following dtd:
     * 
     * <!ELEMENT calendar-query (DAV:allprop | DAV:propname | DAV:prop)? filter>
     */
    public void validate() throws DOMValidationException{
        if (calendarDataProp != null){
            calendarDataProp.validate();
        }
        if (compFilter == null){
            throwValidationException("CompFilter cannot be null.");
        }
        
        compFilter.validate();
    }

}
