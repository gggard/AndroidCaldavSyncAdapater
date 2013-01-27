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
 * 
 * 
 */
package org.osaf.caldav4j.model.response;

import java.io.StringReader;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.w3c.dom.Element;
/**
 * 
 * @author pventura_at_babel.it changed getCalendar
 *
 */
public class CalendarDataProperty extends BaseProperty {

	public static final String ELEMENT_CALENDAR_DATA = "calendar-data";

	private Calendar calendar = null;
    private ThreadLocal<CalendarBuilder> calendarBuilderThreadLocal = new ThreadLocal<CalendarBuilder>();


	public CalendarDataProperty(ResponseEntity response, Element element) {
		super(response, element);
	}
    private CalendarBuilder getCalendarBuilderInstance(){
        CalendarBuilder builder = calendarBuilderThreadLocal.get();
        if (builder == null){
            builder = new CalendarBuilder();
            calendarBuilderThreadLocal.set(builder);
        }
        return builder;
    }

	/**
	 * 
	 * @return the parsed calendar if present, or generate a new calendar item if not present
	 * @throws CalDAV4JException
	 */
	public Calendar getCalendar() throws CalDAV4JException {
		if (calendar != null) {
			return calendar;
		}

		String text = getElement().getTextContent();
		text.trim();
		
		
		//text might contain lines breaked only with \n. RFC states that long lines must be delimited by CRLF.
		//@see{http://www.apps.ietf.org/rfc/rfc2445.html#sec-4.1 }
		//this fix the problem occurred when lines are breaked only with \n 
		text=text.replaceAll("\n","\r\n").replaceAll("\r\r\n", "\r\n");
		
		
//		Pattern noDayLight = Pattern.compile("BEGIN:VTIMEZONE.*END:VTIMEZONE", Pattern.DOTALL);
//		Matcher m = noDayLight.matcher(text);
//		text = m.replaceAll("");
		StringReader stringReader = new StringReader(text);
		try {
			calendar = getCalendarBuilderInstance().build(stringReader);
			stringReader = null;
			return calendar;
		} catch (Exception e) {
			throw new CalDAV4JException("Problem building calendar", e);
		}
	}
}
