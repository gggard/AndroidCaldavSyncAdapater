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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.util.UrlUtils;

public class PostMethod extends org.apache.commons.httpclient.methods.PostMethod{
    private static final Log log = LogFactory.getLog(PostMethod.class);
   
    protected Calendar calendar = null; 
    private String procID = CalDAVConstants.PROC_ID_DEFAULT;
    private CalendarOutputter calendarOutputter = null;
    private Set<String> etags = new HashSet<String>();
    private boolean ifMatch = false;
    private boolean ifNoneMatch = false;
    private boolean allEtags = false;
    
    public PostMethod (){
        super();
    }
    
    public String getName() {
        return CalDAVConstants.METHOD_POST;
    }
    
    /**
     * The set of eTags that will be used in "if-none-match" or "if-match" if the
     * ifMatch or ifNoneMatch properties are set
     * @return
     */
    public Set getEtags() {
        return etags;
    }

    public void setEtags(Set<String> etags) {
        this.etags = etags;
    }
    
    /**
     * Add's the etag provided to the "if-none-match" or "if-match" header.
     * 
     * Note - You MUST provide a quoted string!
     * @param etag
     */
    public void addEtag(String etag){
        etags.add(etag);
    }
    
    
    public void removeEtag(String etag){
        etags.remove(etag);
    }

    /**
     * If true the "if-match" conditional header is used with the etags set in the 
     * etags property.
     * @return
     */
    public boolean isIfMatch() {
        return ifMatch;
    }

    public void setIfMatch(boolean ifMatch) {
        this.ifMatch = ifMatch;
    }

    /**
     * If true the "if-none-match" conditional header is used with the etags set in the 
     * etags property.
     * @return
     */
    public boolean isIfNoneMatch() {
        return ifNoneMatch;
    }

    public void setIfNoneMatch(boolean ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
    }

    public boolean isAllEtags() {
        return allEtags;
    }

    public void setAllEtags(boolean allEtags) {
        this.allEtags = allEtags;
    }

    public void setRequestBody(Calendar calendar){
        this.calendar = calendar;
    }
    
    public void setRequestBody(VEvent vevent, VTimeZone vtimeZone){
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Open Source Applications Foundation//NONSGML Scooby Server//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getComponents().add(vevent);
        if (vtimeZone != null){
            calendar.getComponents().add(vtimeZone);
        }
        this.calendar = calendar;
    }
    
    public void setRequestBody(VEvent vevent){
        setRequestBody(vevent, null);
    }
    
    /**
     * The ProcID to use when creating a new VCALENDAR component
     * @return
     */
    public String getProcID() {
        return procID;
    }
    
    /**
     * Sets the ProcID to use when creating a new VCALENDAR component
     * @param procID
     */
    public void setProcID(String procID) {
        this.procID = procID;
    }

    public CalendarOutputter getCalendarOutputter() {
        return calendarOutputter;
    }

    public void setCalendarOutputter(CalendarOutputter calendarOutputter) {
        this.calendarOutputter = calendarOutputter;
    }

    protected byte[] generateRequestBody()  {
        if (calendar != null){
            StringWriter writer = new StringWriter();
            try{
                calendarOutputter.output(calendar, writer);
                
                RequestEntity requestEntity = new StringRequestEntity(writer.toString(),
						CalDAVConstants.CONTENT_TYPE_CALENDAR, 
						Charset.defaultCharset().toString());
                setRequestEntity(requestEntity);                                
            } catch (UnsupportedEncodingException e) {
            	 log.error("Unsupported encoding in event" + writer.toString());
            	 throw new RuntimeException("Problem generating calendar. ", e);
            } catch (Exception e){
                log.error("Problem generating calendar: ", e);
                throw new RuntimeException("Problem generating calendar. ", e);
            }
        }
        return super.generateRequestBody();
    }
    
    protected void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        if (ifMatch || ifNoneMatch){
            String name = ifMatch ? CalDAVConstants.HEADER_IF_MATCH : CalDAVConstants.HEADER_IF_NONE_MATCH;
            String value = null;
            if (allEtags){
                value = "*";
            } else {
                StringBuffer buf = new StringBuffer();
                int x = 0;
                for (Iterator<String> i = etags.iterator();i.hasNext();){
                    if (x > 0){
                        buf.append(", ");
                    }
                    String etag = (String)i.next();
                    buf.append(etag);
                    x++;
                }
                value = buf.toString();
            }
            addRequestHeader(name, value);
        }
        addRequestHeader(CalDAVConstants.HEADER_CONTENT_TYPE, CalDAVConstants.CONTENT_TYPE_CALENDAR);
        super.addRequestHeaders(state, conn);
    }

    // remove double slashes
    public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
    
    protected boolean hasRequestContent() {
        if (calendar != null) {
            return true;
        } else {
            return super.hasRequestContent();
        }
    }    
}
