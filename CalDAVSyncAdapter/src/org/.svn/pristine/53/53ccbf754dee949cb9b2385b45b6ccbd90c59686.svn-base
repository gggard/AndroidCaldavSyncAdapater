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
 *  <!ELEMENT prop-filter (is-defined | time-range | text-match)?
 *                          param-filter*>
 *
 *  <!ATTLIST prop-filter name CDATA #REQUIRED>
 *  
 * @author bobbyrullo
 * 
 */
public class PropFilter extends OutputsDOMBase {
    
    public static final String ELEMENT_NAME = "prop-filter";
    public static final String ELEM_IS_DEFINED = "is-defined";
    public static final String ELEM_IS_NOT_DEFINED = "is-not-defined";
    public static final String ATTR_NAME = "name";
    
    private String caldavNamespaceQualifier = null;

    private String name = null;
    private Boolean isDefined = null;

    private TimeRange timeRange = null;
    private TextMatch textMatch = null;
    private List<ParamFilter> paramFilters = new ArrayList<ParamFilter>();
    
    public PropFilter(String caldavNamespaceQualifier) {
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
    }
    
    public PropFilter(String caldavNamespaceQualifier, String name, 
            Boolean isDefined, Date timeRangeStart, Date timeRangeEnd, 
            Boolean textmatchCaseless, boolean negateCondition,
            String textMatchCollation, String textMatchString, List<ParamFilter> paramFilters){
    	
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.name = name;

        
        if (isDefined != null) {
            this.isDefined = isDefined;
        } else if (timeRangeStart != null || timeRangeEnd != null){
            this.timeRange = new TimeRange(caldavNamespaceQualifier, timeRangeStart, timeRangeEnd);
        } else if (textMatchString != null){
            this.textMatch = new TextMatch(caldavNamespaceQualifier,
            		textmatchCaseless, negateCondition, textMatchCollation, 
            		textMatchString);
        }
        if (paramFilters != null){
            this.paramFilters = paramFilters;
        }
    }
    
    /**
     * @deprecated The Full constructor should be used
     * @param caldavNamespaceQualifier
     * @param name
     * @param isDefined
     * @param timeRangeStart
     * @param timeRangeEnd
     * @param textmatchCaseless
     * @param textMatchString
     * @param paramFilters
     */
    public PropFilter(String caldavNamespaceQualifier, String name, 
            boolean isDefined, Date timeRangeStart, Date timeRangeEnd, 
            Boolean textmatchCaseless, String textMatchString, List<ParamFilter> paramFilters){
    	
        this.caldavNamespaceQualifier = caldavNamespaceQualifier;
        this.name = name;
        this.isDefined = isDefined;
        if (timeRangeStart != null && timeRangeEnd != null){
            this.timeRange = new TimeRange(caldavNamespaceQualifier, timeRangeStart, timeRangeEnd);
        } else if (textMatchString != null){
            this.textMatch = new TextMatch(caldavNamespaceQualifier,
            		textmatchCaseless, false, null, 
            		textMatchString);
        }
        if (paramFilters != null){
            this.paramFilters = paramFilters;
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
        
        if (isDefined != null) {
            children.add(new SimpleDOMOutputtingObject(
                    CalDAVConstants.NS_CALDAV, caldavNamespaceQualifier,
                    isDefined ? ELEM_IS_DEFINED :  ELEM_IS_NOT_DEFINED)); 
        } else if (timeRange != null){
            children.add(timeRange);
        } else if (textMatch != null){
            children.add(textMatch);
        }
        
        if (paramFilters != null && paramFilters.size() > 0){
            children.addAll(paramFilters);
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

    public Boolean isDefined() {
        return isDefined;
    }

    public void setIsDefined(Boolean isDefined) {
        this.isDefined = isDefined;
    }

    public List<ParamFilter> getParamFilters() {
        return paramFilters;
    }

    public void setParamFilters(List<ParamFilter> paramFilters) {
        this.paramFilters = paramFilters;
    }
    
    public void addParamFilter(ParamFilter paramFilter){
        paramFilters.add(paramFilter);
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }
    
    public void setTimeRange(Date start, Date end){
        this.timeRange = new TimeRange(caldavNamespaceQualifier, start, end);
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

    public TextMatch getTextMatch() {
        return textMatch;
    }

    public void setTextMatch(TextMatch textMatch) {
        this.textMatch = textMatch;
    }
    
    /**
    *  <!ELEMENT prop-filter (is-defined | time-range | text-match)?
    *                          param-filter*>
    *
    *  <!ATTLIST prop-filter name CDATA #REQUIRED>
    *  
    * @author bobbyrullo
    * 
    */
    public void validate() throws DOMValidationException{
        if (name == null){
            throwValidationException("Name is a required property");
        }
        
        if ( (isDefined != null) && (timeRange != null || textMatch != null)){
            throwValidationException("isDefined, timeRange and textMatch are mutually exclusive");
        }
        
        if (timeRange != null) {
            if (textMatch != null){
                throwValidationException("isDefined, timeRange and textMatch are mutually exclusive");
            } 
            
            timeRange.validate();
        } else if (textMatch != null){
            textMatch.validate();
        }

        if (paramFilters != null){
            validate(paramFilters);
        }

    }
}