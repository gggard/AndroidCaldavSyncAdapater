/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.caldav4j.model.response;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.fortuna.ical4j.model.Calendar;

import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.properties.PropertyFactory;
import org.apache.webdav.lib.util.DOMUtils;
import org.apache.webdav.lib.util.DOMWriter;
import org.apache.webdav.lib.util.QName;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CalDAVResponse implements ResponseEntity {

    protected Node node = null;
    private Hashtable<QName, Property> properties = null;
    
    //must register CalendarDataProperty
    static {
        try {
        PropertyFactory.register(CalDAVConstants.NS_CALDAV,
                CalendarDataProperty.ELEMENT_CALENDAR_DATA,
                CalendarDataProperty.class);
        } catch (Exception e){
            throw new RuntimeException("Could not register CalendarDataProperty!", e);
        }
    }
    
    public CalDAVResponse(Node node) {
        this.node = node;
    }

    public static final String TAG_NAME = "response";
    
    public int getStatusCode() {
        Element propstat = getFirstElement("DAV:", "propstat");
        if (propstat != null ) {
            Element status = DOMUtils.getFirstElement(propstat,"DAV:", "status");
            if (status != null) {
                return DOMUtils.parseStatus(DOMUtils.getTextValue(status));
            }
        }

        return -1;
    }

    public String getHref() {
        Element href = getFirstElement("DAV:", "href");
        if (href != null) {
            return DOMUtils.getTextValue(href);
        } else {
            return "";
        }

    }
    
    public String getETag(){
        Property eTagProperty = getProperty(CalDAVConstants.QNAME_GETETAG);

        if (eTagProperty != null) {
            return eTagProperty.getElement().getTextContent();
        }
        
        return null;
    }

    public Enumeration getHistories(){
        Vector result = new Vector();
        return result.elements();
    }
    
    public Enumeration getWorkspaces(){
        Vector result = new Vector();
        return result.elements();
    }
    
    public Enumeration getProperties() {
        if (properties == null){
            initProperties();
        }

        return properties.elements();
    }

    public String toString () {
        StringWriter tmp = new StringWriter();
        DOMWriter domWriter = new DOMWriter(tmp, true);
        domWriter.print(node);
        return tmp.getBuffer().toString();
    }
    
    public Property getProperty(QName qname){
        if (properties == null){
            initProperties();
        }
        return (Property)properties.get(qname);
    }
    
    public CalendarDataProperty getCalendarDataProperty(){
        return (CalendarDataProperty) getProperty(new QName(
                CalDAVConstants.NS_CALDAV,
                CalendarDataProperty.ELEMENT_CALENDAR_DATA));
    }
    
    /**
     * Return a Calendar object or null if calendarDataPropery wasn't defined
     * @author rpolli
     * @return a Calendar object is calendarDataPropery exists
     * @throws CalDAV4JException
     */
    public Calendar getCalendar() throws CalDAV4JException{
    	CalendarDataProperty cdp = getCalendarDataProperty();
    	if (cdp!=null) {
    		return cdp.getCalendar();
    	} 
    	return null;
    }
    
    protected Element getFirstElement(String namespace, String name) {
        return DOMUtils.getFirstElement(this.node, namespace, name);
    }
    
    private void initProperties() {
        NodeList list =
            DOMUtils.getElementsByTagNameNS(node, "prop", "DAV:");
        properties = new Hashtable<QName, Property>();
        for (int i = 0; list != null && i < list.getLength(); i++ ) {
            Element element = (Element) list.item(i);
            NodeList children = element.getChildNodes();
            for (int j = 0; children != null && j < children.getLength();
                 j++) {
                try {
                    Node n = children.item(j);
                    if (n.getNodeType() == Node.ELEMENT_NODE){
                    Element child = (Element) n;
                    properties.put(new QName(child.getNamespaceURI(), child
                            .getLocalName()), PropertyFactory.create(this,
                            child));
                    }
                } catch (ClassCastException e) {
                    throw new RuntimeException(e);
                }
            }
        }   
    }

}
