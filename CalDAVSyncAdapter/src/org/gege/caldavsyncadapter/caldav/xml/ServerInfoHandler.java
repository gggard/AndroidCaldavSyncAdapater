package org.gege.caldavsyncadapter.caldav.xml;

import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ServerInfoHandler extends DefaultHandler {

	private static final String HREF = "href";
	private static final String PRINCIPAL_URL = "principal-URL";
	private static final String CURRENT_USER_PRINCIPAL = "current-user-principal";
	private final static List<String> TAGS = Arrays.asList(
			CURRENT_USER_PRINCIPAL, PRINCIPAL_URL);
	private StringBuilder stringBuilder = new StringBuilder();
	private String inParentElement;
	private String currentElement;

	public String currentUserPrincipal = null;
	public String principalUrl = null;

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if (TAGS.contains(localName)) {
			inParentElement = localName;
		}
		currentElement = localName;
		stringBuilder.setLength(0);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (HREF.equals(currentElement) && TAGS.contains(inParentElement)) {
			stringBuilder.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (HREF.equals(currentElement) && TAGS.contains(inParentElement)) {
			if (CURRENT_USER_PRINCIPAL.equals(inParentElement)) {
				currentUserPrincipal = stringBuilder.toString();
			} else {
				principalUrl = stringBuilder.toString();
			}
		}
		if(TAGS.contains(localName)){
			inParentElement = null;
		}
		currentElement = null;
	}
}
