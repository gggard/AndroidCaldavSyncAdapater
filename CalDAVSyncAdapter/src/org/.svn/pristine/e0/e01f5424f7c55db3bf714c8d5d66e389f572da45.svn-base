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
import java.util.HashMap;
import java.util.Map;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 * <!ELEMENT param-filter (is-defined | text-match) >
 *
 * <!ATTLIST param-filter name CDATA #REQUIRED>
 *  
 * @author bobbyrullo
 * 
 */
public class ParamFilter extends OutputsDOMBase {
    
    public static final String ELEMENT_NAME = "param-filter";
    public static final String ELEM_IS_DEFINED = "is-defined";
    public static final String ATTR_NAME = "name";
    
    private String caldavNamespaceQualifier = null;

    private boolean isDefined = false;
    private TextMatch textMatch = null;
    private String name = null;
    public void setName(String name) {
		this.name = name;
	}
    public String getName() {
		return name;
	}
    public ParamFilter(String caldavNamespaceQualifier) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
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
        if (isDefined){
            children.add(new SimpleDOMOutputtingObject(
                    CalDAVConstants.NS_CALDAV, caldavNamespaceQualifier,
                    ELEM_IS_DEFINED));
        } else if (textMatch != null){
            children.add(textMatch);
        }
        
        return children;
    }
    
    protected String getTextContent() {
        return null;
    }
    
    protected Map<String, String> getAttributes() {
        Map<String, String> m = new HashMap<String, String>();
        m.put(ATTR_NAME, name);
        return m;
    }


    public boolean isDefined() {
        return isDefined;
    }

    public void setDefined(boolean isDefined) {
        this.isDefined = isDefined;
    }
    
    public TextMatch getTextMatch() {
        return textMatch;
    }

    public void setTextMatch(TextMatch textMatch) {
        this.textMatch = textMatch;
    }
    
    /**
     * <!ELEMENT param-filter (is-defined | text-match) >
     * 
     * <!ATTLIST param-filter name CDATA #REQUIRED>
     * 
     */
    public void validate() throws DOMValidationException{
       if (name == null){
           throwValidationException("Name is a required property");
       }
        
       if (isDefined && textMatch != null){
           throwValidationException("isDefined and textMatch are mutually exclusive");
       }
       
       if (textMatch != null){
           textMatch.validate();
       }
    }
    
}
