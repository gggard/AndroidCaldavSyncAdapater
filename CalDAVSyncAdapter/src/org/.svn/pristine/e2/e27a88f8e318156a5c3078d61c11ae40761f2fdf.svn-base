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

package org.osaf.caldav4j.util;

import java.text.ParseException;
import java.util.Calendar;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.CalDAVResource;
import org.osaf.caldav4j.exceptions.CalDAV4JException;

public class ICalendarUtils {
    private static final Log log = LogFactory.getLog(ICalendarUtils.class);
    
    private static java.util.TimeZone J_TZ_GMT = TimeZone.getTimeZone("GMT");
   
    /**
     * Creates an iCal4J DateTime. The values for year, month, day, hour,
     * minutes, seconds and milliseconds should be set way that you specify them
     * in a java.util.Calendar - which means zero indexed months for example
     * (eg. January is '0').
     * 
     * Note that the TimeZone is not a java.util.TimeZone but an iCal4JTimeZone
     * 
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minutes
     * @param seconds
     * @param milliseconds
     * @param tz
     * @param utc
     * @return
     */
    public static DateTime createDateTime(int year, int month, int day,
            int hour, int minutes, int seconds, int milliseconds, TimeZone tz,
            boolean utc) {
        DateTime dateTime = new DateTime();
        setFields(dateTime, year, month, day, hour, minutes, seconds,
                milliseconds, tz, utc);
        return dateTime;
    }
    
    public static DateTime createDateTime(int year, int month, int day,
            int hour, int minutes, TimeZone tz, boolean utc) {
        DateTime dateTime = new DateTime();
        setFields(dateTime, year, month, day, hour, minutes, 0, 0, tz, utc);
        return dateTime;

    }
    
    public static Date createDate(int year, int month, int day) {
        Date date = new Date();
        setFields(date, year, month, day, 0, 0, 0, 0, null, false);
        return date;

    }
    
    public static Date createDateTime(int year, int month, int day,
            TimeZone tz, boolean utc) {
        DateTime date = new DateTime();
        setFields(date, year, month, day, 0, 0, 0, 0, tz, utc);
        return date;

    }
    
    public static VEvent getFirstEvent(net.fortuna.ical4j.model.Calendar calendar){
        return (VEvent) calendar.getComponents().getComponent(Component.VEVENT);
    }
    
    /**
     * get first non-timezone component
     * @param event
     * @return null if not present
     */
    public static Component getFirstComponent(CalDAVResource resource, String component) {
    	return resource.getCalendar().getComponent(component);
    }

    /**
     * return the first non-tz component of a calendar:
     *  exception if calendar contains different types of event
     * @param calendar
     * @return
     * TODO use a parameter to eventually skip VTimeZone
     */
    public static Component getFirstComponent(net.fortuna.ical4j.model
    		.Calendar calendar) throws CalDAV4JException {
    	// XXX this works only if the ics is a caldav resource
    	Component ret = null;
    	String compType = null;
    	
    	for (Object component : calendar.getComponents()) {
    		// skip timezones
    		if (! (component instanceof VTimeZone)) {
    			if (ret == null) {
    				ret = (Component) component;
    				compType = ret.getClass().getName();
    			} else if (! compType.equals(component.getClass().getName()) ) {
    				throw new CalDAV4JException("Can't get first component: "
    						+ "Calendar contains different kinds of component");
    			}
				
			}
    	}
    	return ret;
    } 
    public static Component getFirstComponent(net.fortuna.ical4j.model
    		.Calendar calendar, boolean skipTimezone) throws CalDAV4JException {
    	// XXX this works only if the ics is a caldav resource
    	Component ret = null;
    	String compType = null;
    	
    	for (Object component : calendar.getComponents()) {
    		
    		if (!skipTimezone) {
    			ret =  (Component) component;
    		} else if (! (component instanceof VTimeZone)) {    		// skip timezones
    			if (ret == null) {
    				ret = (Component) component;
    				compType = ret.getClass().getName();
    			} else if (! compType.equals(component.getClass().getName()) ) {
    				throw new CalDAV4JException("Can't get first component: "
    						+ "Calendar contains different kinds of component");
    			}
				
			}
    	}
    	return ret;
    } 

    /**
     * get a Calendar UID value: as in Caldav, a Caldav Calendar Resource should have an unique UID value 
     * @param calendar
     * @return
     */
    public static String getUIDValue(net.fortuna.ical4j.model
    		.Calendar calendar) throws CalDAV4JException {
    	return getUIDValue(getFirstComponent(calendar));
    }
    
    /**
     * set a Calendar UID value: as 
     *  in Caldav, a Caldav Calendar Resource should have an unique UID value
     *  for a flexible method, @see  addOrReplaceProperty
     * @param calendar
     * @param uid
     */
    public static void setUIDValue(net.fortuna.ical4j.model.Calendar calendar,
    		String uid) throws CalDAV4JException{
    	for (Object c : calendar.getComponents()) {
    		if (c!=null && ! (c  instanceof VTimeZone)) {
    			addOrReplaceProperty((Component) c, new Uid(uid));
    		}
    	}    	
    }
    
    public static String getSummaryValue(VEvent event){
        return getPropertyValue(event, Property.SUMMARY);
    }

    public static String getUIDValue(Component component){
        return getPropertyValue(component, Property.UID);
    }
    
    public static String getPropertyValue(Component component, String propertyName){
        Property property = component.getProperties().getProperty(propertyName);
        return property == null ? null : property.getValue();
    }
    
    
    private static void setFields(Date date, int year, int month, int day,
            int hour, int minutes, int seconds, int milliseconds, TimeZone tz,
            boolean utc){
        
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.setTimeZone(tz == null ? J_TZ_GMT : tz);
        if (date instanceof DateTime){
            if (utc) {
               ((DateTime)date).setUtc(utc);
            } else if (tz != null){
                ((DateTime)date).setTimeZone(tz);
            }
            calendar.set(Calendar.HOUR, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);
        }
        date.setTime(calendar.getTimeInMillis());
    }
    
    public static boolean hasProperty(Component c, String propName){
        PropertyList l = c.getProperties().getProperties(propName);
        return l != null && l.size() > 0;
    }
        
    /**
     * Returns the "master" VEvent - one that does not have a RECURRENCE-ID
     * 
     * @param uid
     * @return
     */
    public static VEvent getMasterEvent(net.fortuna.ical4j.model.Calendar calendar, String uid){
        ComponentList clist = calendar.getComponents().getComponents(Component.VEVENT);
        for (Object o : clist){
            VEvent curEvent = (VEvent) o;
            String curUid = getUIDValue(curEvent);
            if (uid.equals(curUid) && !hasProperty(curEvent, Property.RECURRENCE_ID) ){
                return curEvent;
            }
        }
        return null;
    }
    
    /**
     *  Returns the "master" Component - one that does not have a RECURRENCE-ID
     * @param calendar
     * @param uid
     * @return
     */
    // TODO create junit
    public static CalendarComponent getMasterComponent(net.fortuna.ical4j.model.Calendar calendar, String uid){
        ComponentList clist = calendar.getComponents();
        for (Object o : clist){
            CalendarComponent curEvent = (CalendarComponent) o;
            String curUid = getUIDValue(curEvent);
            if (uid.equals(curUid) && !hasProperty(curEvent, Property.RECURRENCE_ID) ){
                return curEvent;
            }
        }
        return null;
    }
    // TODO create junit
    public static CalendarComponent getComponentOccurence(net.fortuna.ical4j.model.Calendar calendar, String uid, String recurrenceId){
        ComponentList clist = calendar.getComponents();
        for (Object o : clist){
            CalendarComponent curEvent = (CalendarComponent) o;
            String curUid = getUIDValue(curEvent);
            String curRid = getPropertyValue(curEvent, Property.RECURRENCE_ID);
            if (uid.equals(curUid) && StringUtils.equalsIgnoreCase(recurrenceId, curRid) ){
                return curEvent;
            }
        }
        return null;
    }  
    /**
     * 
     * @param calendar
     * @param uid
     * @param recurrenceId
     * @return the modified calendar
     * @throws ParseException 
     */
    /// TODO create junit
    public static net.fortuna.ical4j.model.Calendar removeOccurrence(net.fortuna.ical4j.model.Calendar calendar, String uid, String recurrenceId) throws ParseException {
    	ComponentList clist = calendar.getComponents();
    	CalendarComponent master = null;
    	CalendarComponent toBeRemoved = null;
        for (Object o : clist){
            CalendarComponent curEvent = (CalendarComponent) o;
            if ( (master==null) && ! (o instanceof VTimeZone)) {
            	master = curEvent;
            }
            String curUid = getUIDValue(curEvent);
            String curRid = getPropertyValue(curEvent, Property.RECURRENCE_ID);
            if (uid.equals(curUid) && StringUtils.equalsIgnoreCase(recurrenceId, curRid) ){
            	toBeRemoved = curEvent;
            	break;
            }
        }
        if (toBeRemoved!=null) {
        	clist.remove(toBeRemoved);
        }
        if (master != null) {
	    	ExDate x = new ExDate();
	    	x.setValue(recurrenceId);
	    	master.getProperties().add(x);
        }
        return calendar;
    }
    public static void addOrReplaceProperty(Component component, Property property){
        Property oldProp = component.getProperties().getProperty(property.getName());
        if (oldProp != null){
            component.getProperties().remove(oldProp);
        }
        
        component.getProperties().add(property);
    }

}
