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
package org.osaf.caldav4j;

import java.io.Serializable;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.model.response.CalDAVResponse;

public class CalDAVResource implements Serializable{
	private static final long serialVersionUID = -2607152240683030192L;
	private ResourceMetadata resourceMetadata = null;
    private Calendar calendar = null;
    
    public CalDAVResource(CalDAVResponse response) throws CalDAV4JException{
        this.calendar = response.getCalendar();
        this.resourceMetadata = new ResourceMetadata();
        this.resourceMetadata.setETag(response.getETag());
        this.resourceMetadata.setHref(response.getHref());
    }
    
    public CalDAVResource(Calendar calendar, String etag, String href){
        this.calendar = calendar;
        ResourceMetadata rm = new ResourceMetadata();
        rm.setETag(etag);
        rm.setHref(href);
        this.resourceMetadata = rm;
    }

    public CalDAVResource(){
        resourceMetadata = new ResourceMetadata();
    }
    
    public void setCalendar(Calendar calendar){
        this.calendar = calendar;
    }
    
    public Calendar getCalendar() {
        return calendar;
    }

    public ResourceMetadata getResourceMetadata() {
        return resourceMetadata;
    }
}