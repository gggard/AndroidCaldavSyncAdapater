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
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.model.Date;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 *   <!ELEMENT comp-filter (is-defined | time-range)?
 *                        comp-filter* prop-filter*>
 *                        
 *   <!ATTLIST comp-filter name CDATA #REQUIRED> 
 *  
 * @author bobbyrullo
 * 
 */
public class CompFilter extends OutputsDOMBase {
    
    public static final String ELEMENT_NAME = "comp-filter";
    public static final String ELEM_IS_DEFINED = "is-defined";
    public static final String ATTR_NAME = "name";
    
    private String caldavNamespaceQualifier = null;

    private boolean isDefined = false;
    private TimeRange timeRange = null;
    private List<CompFilter> compFilters = new ArrayList<CompFilter>();
    private List<PropFilter> propFilters = new ArrayList<PropFilter>();
    private String name = null;
    
    public CompFilter(String caldavNamespaceQualifier) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
    }
    
    /**
     * Create a CompFilter
     * @param caldavNamespaceQualifier
     * @param name
     * @param isDefined
     * @param start
     * @param end
     * @param compFilters
     * @param propFilters
     */
    public CompFilter(String caldavNamespaceQualifier, String name,
            boolean isDefined, Date start, Date end, List<CompFilter> compFilters,
            List<PropFilter> propFilters) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.isDefined = isDefined;
        this.name = name;
        
        if (start != null || end != null) { // XXX test the || instead of && (open interval)
            this.timeRange = new TimeRange(caldavNamespaceQualifier, start, end);
        }
        
        if (propFilters != null){
            this.propFilters.addAll(propFilters);
        }
        
        if (compFilters != null) {
            this.compFilters.addAll(compFilters);
        }
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
        } else if (timeRange != null){
            children.add(timeRange);
        }
        
        if (compFilters != null) {
            children.addAll(compFilters);
        }
        
        if (propFilters != null){
            children.addAll(propFilters);
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

    public List<CompFilter> getCompFilters() {
        return compFilters;
    }

    public void setCompFilters(List<CompFilter> compFilters) {
        this.compFilters = compFilters;
    }
    
    public void addCompFilter(CompFilter compFilter) {
        compFilters.add(compFilter);
    }

    public boolean isDefined() {
        return isDefined;
    }

    public void setDefined(boolean isDefined) {
        this.isDefined = isDefined;
    }

    public List<PropFilter> getPropFilters() {
        return propFilters;
    }

    public void setPropFilters(List<PropFilter> propFilters) {
        this.propFilters = propFilters;
    }
    
    public void addPropFilter(PropFilter propFilter){
        propFilters.add(propFilter);
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     *   <!ELEMENT comp-filter (is-defined | time-range)?
     *                        comp-filter* prop-filter*>
     *                        
     *   <!ATTLIST comp-filter name CDATA #REQUIRED> 
     */
    public void validate() throws DOMValidationException{
        if (name == null){
           throwValidationException("Name is a required property.");
        }
        
       if (isDefined && timeRange != null){
           throwValidationException("TimeRange and isDefined are mutually exclusive");
       }
       
       if (timeRange != null){
           timeRange.validate();
       }
       
       if (compFilters != null){
           validate(compFilters);
       }
       
       if (propFilters != null){
           validate(propFilters);
       }
    }
}
