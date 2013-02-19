package org.gege.caldavsyncadapter.caldav.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class CalendarsHandler extends DefaultHandler {

	private static final String CALENDAR = "calendar";
	private static final String RESOURCETYPE = "resourcetype";
	private static final String CALENDAR_COLOR = "calendar-color";
	private static final String GETCTAG = "getctag";
	private static final String DISPLAYNAME = "displayname";
	private URI homeURI;

	public CalendarsHandler(URI homeURI) {
		this.homeURI = homeURI;
	}

	private static final String RESPONSE = "response";
	private static final String HREF = "href";

	private StringBuilder stringBuilder = new StringBuilder();
	private String currentElement;
	private Calendar calendar;
	public List<Calendar> calendars = new ArrayList<Calendar>();
	private boolean isResourceType = false;
	private boolean isCalendarType;

	public final static List<String> TAGS = Arrays.asList(HREF, RESOURCETYPE,
			DISPLAYNAME, GETCTAG, CALENDAR_COLOR);

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (RESPONSE.equals(localName)) {
			calendar = new Calendar();
		} else if (RESOURCETYPE.equals(localName)) {
			isResourceType=true;
		} else if(isResourceType && CALENDAR.equals(localName)){
			isCalendarType = true;
		}
		currentElement = localName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (TAGS.contains(currentElement)) {
			stringBuilder.append(ch, start, length);
		}

		Log.d(CalendarsHandler.class.getSimpleName(), currentElement + " = "
				+ new String(ch, start, length));
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(RESOURCETYPE.equals(localName) && !isCalendarType){
			calendar = null;
		} else if (TAGS.contains(localName)) {
			if (calendar != null) {
				if (HREF.equals(localName)) {
					String calendarUrl = stringBuilder.toString();
					try {
						URI calendarURI = new URI(calendarUrl);
						calendar.setURI(homeURI.resolve(calendarURI));
					} catch (URISyntaxException e) {
						Log.e(CalendarsHandler.class.getSimpleName(),
								"uri malformed: " + calendarUrl);
					}
				} else if (DISPLAYNAME.equals(localName)) {
					calendar.setDisplayName(stringBuilder.toString());
				} else if (GETCTAG.equals(localName)) {
					calendar.setCTag(stringBuilder.toString());
				}
			}
			stringBuilder.setLength(0);
		} else if (RESPONSE.equals(localName)) {
			Log.d(CalendarsHandler.class.getSimpleName(), localName + " = "
					+ calendar);
			if (calendar != null && calendar.getURI() != null
					&& calendar.getcTag() != null
					&& calendar.getDisplayName() != null) {
				calendars.add(calendar);
			}
		}
	}
}
