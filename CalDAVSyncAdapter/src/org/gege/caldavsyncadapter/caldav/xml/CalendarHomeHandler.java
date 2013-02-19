package org.gege.caldavsyncadapter.caldav.xml;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CalendarHomeHandler extends DefaultHandler {

	private static final String HREF = "href";
	private static final String CALENDAR_HOME_SET = "calendar-home-set";
	public List<String> calendarHomeSet = new ArrayList<String>();

	private String inParentElement = null;
	private StringBuilder stringBuilder = new StringBuilder();
	private String currentElement;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (CALENDAR_HOME_SET.equals(localName)) {
			inParentElement = localName;
		}
		currentElement = localName;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (HREF.equals(currentElement) && inParentElement != null) {
			stringBuilder.append(ch, start, length);
		}

//		Log.d(ServerInfoHandler.class.getSimpleName(), inParentElement + ""
//				+ new String(ch, start, length));
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (HREF.equals(localName) && inParentElement != null) {
			calendarHomeSet.add(stringBuilder.toString());
			stringBuilder.setLength(0);
		}
		if(CALENDAR_HOME_SET.equals(localName)){
			inParentElement = null;
		}
	}
}
