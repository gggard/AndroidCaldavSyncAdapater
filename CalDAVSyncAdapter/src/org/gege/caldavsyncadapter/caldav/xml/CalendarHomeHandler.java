package org.gege.caldavsyncadapter.caldav.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.gege.caldavsyncadapter.BuildConfig;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class CalendarHomeHandler extends DefaultHandler {

	private static final String HREF = "href";
	private static final String CALENDAR_HOME_SET = "calendar-home-set";
	private boolean isInCalendarHomeSet = false;
	private StringBuilder stringBuilder = new StringBuilder();
	private String currentElement;
	private URI principalURI;
	
	public List<URI> calendarHomeSet = new ArrayList<URI>();

	public CalendarHomeHandler(URI principalURI) {
		this.principalURI = principalURI;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (CALENDAR_HOME_SET.equals(localName)) {
			isInCalendarHomeSet = true;
		}
		currentElement = localName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (HREF.equals(currentElement) && isInCalendarHomeSet) {
			stringBuilder.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (HREF.equals(localName) && isInCalendarHomeSet) {
			String calendarHomeSet = stringBuilder.toString();
			try {
				URI calendarHomeSetURI = new URI(calendarHomeSet);
				calendarHomeSetURI = principalURI.resolve(calendarHomeSetURI);
				this.calendarHomeSet.add(calendarHomeSetURI);
			} catch (URISyntaxException e) {
				if (BuildConfig.DEBUG) {
					Log.e(CalendarHomeHandler.class.getSimpleName(),
							"uri malformed: " + calendarHomeSet);
				} else {
					Log.e(CalendarHomeHandler.class.getSimpleName(),
							"uri malformed in calendar-home-set/href");
				}
			}
			stringBuilder.setLength(0);
		}
		if (CALENDAR_HOME_SET.equals(localName)) {
			isInCalendarHomeSet = false;
		}
	}
}
