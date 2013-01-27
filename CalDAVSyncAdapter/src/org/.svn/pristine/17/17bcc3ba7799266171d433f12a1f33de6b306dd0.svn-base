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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;

/**
 * <!ELEMENT prop EMPTY>
 * 
 * <!ATTLIST prop name CDATA #REQUIRED
 *                novalue (yes|no) "no">
 *  ex. <C:PROP name="DESCRIPTION" />               
 * @author bobbyrullo
 * 
 */
public class CalDAVProp extends OutputsDOMBase  {
    
    public static final String ELEMENT_NAME = "prop";
    public static final String ATTR_NAME = "start";
    public static final String ATTR_NOVALUE = "novalue";
    public static final String ATTR_VAL_YES = "yes";
    public static final String ATTR_VAL_NO = "no";
    
    private boolean attrNoValueEnabled = true; //XXX used to disable the view on ATTR_NOVALUE
    private String attrName = ATTR_NAME;
    
    private String caldavNamespaceQualifier = null;
    private String name = null;
    private boolean novalue = false;
    
    
    public CalDAVProp(String caldavNamespaceQualifier, String attrName, String name, boolean novalue, boolean attrNoValueEnabled) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.name = name;
        this.novalue = novalue;
        
        this.attrName = attrName; //XXX see if it's ok there or if whe should use another Class
        this.attrNoValueEnabled = attrNoValueEnabled;
    }
    
    public CalDAVProp(String caldavNamespaceQualifier, String name, boolean novalue) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.name = name;
        this.novalue = novalue;
    }
    
    public CalDAVProp(String caldavNamespaceQualifier, String name) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.name = name;
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
        return null;
    }

    protected String getTextContent() {
        return null;
    }
    
    protected Map<String, String> getAttributes() {
        Map<String, String> m =  new HashMap<String, String>();
        m.put(attrName, name); // XXX 
        
        if (attrNoValueEnabled) {
        	m.put(ATTR_NOVALUE, novalue ? ATTR_VAL_YES : ATTR_VAL_NO);
        }
        
        return m;
    }
    
    /**
     * <!ELEMENT prop EMPTY>
     * 
     * <!ATTLIST prop name CDATA #REQUIRED
     *                novalue (yes|no) "no">
     */
    public void validate() throws DOMValidationException {
        if (name == null){
            throwValidationException("name is a required property");
        }
    }

}
