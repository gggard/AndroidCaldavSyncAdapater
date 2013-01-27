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

package org.osaf.caldav4j.util;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.model.response.TicketResponse;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class XMLUtils {
	private static final Log log = LogFactory.getLog(XMLUtils.class);

	private static DOMImplementation implementation = null;

	static {
		try {
			DOMImplementationRegistry registry = DOMImplementationRegistry
			.newInstance();
			implementation = registry.getDOMImplementation("XML 3.0");
			if (implementation == null) {
				log.error("Could not instantiate a DOMImplementation! Make sure you have "
						+ " a version of Xerces 2.7.0 or greater or a DOM impl that "
						+ " implements DOM 3.0");
				throw new RuntimeException(
				"Could not instantiate a DOMImplementation!");
			}
		} catch (Exception e) {
			log
			.error("Could not instantiate a DOMImplementation! Make sure you have "
					+ " a version of Xerces 2.7.0 or greater or a DOM impl that "
					+ " implements DOM 3.0");
			throw new RuntimeException(
					"Could not instantiate a DOMImplementation!", e);
		}
	}

	/**
	 * Creates a new xml DOM Document using a DOM 3.0 DOM Implementation
	 * 
	 * @param namespaceURI
	 *            the default XML Namespace for the document
	 * @param qualifiedName
	 *            the qualified name of the root element
	 * @return a new document
	 */
	public static Document createNewDocument(String namespaceURI,
			String qualifiedName) {

		Document document = implementation.createDocument(namespaceURI,
				qualifiedName, null);
		return document;
	}

	/**
	 * Serializes a DOM Document to XML
	 * 
	 * @param document
	 *            a DOM document
	 * @return the Document serialized to XML
	 */
	public static String toXML(Document document) {
		DOMImplementationLS domLS = (DOMImplementationLS) implementation;
		LSSerializer serializer = domLS.createLSSerializer();
		String s = serializer.writeToString(document);

		return s;
	}

	public static String toPrettyXML(Document document) {
		StringWriter stringWriter = new StringWriter();
		OutputFormat outputFormat = new OutputFormat(document, null, true);
		XMLSerializer xmlSerializer = new XMLSerializer(stringWriter,
				outputFormat);
		xmlSerializer.setNamespaces(true);
		try {
			xmlSerializer.asDOMSerializer().serialize(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return stringWriter.toString();

	}

	public static DOMImplementation getDOMImplementation() {
		return implementation;
	}

	/**
	 * Create a string representation of an DOM document
	 */
	public static String prettyPrint(OutputsDOMBase xml) {
		try {
			Document doc = xml.createNewDocument(XMLUtils
					.getDOMImplementation());
			return XMLUtils.toPrettyXML(doc);

		} catch (DOMValidationException domve) {
			throw new RuntimeException(domve);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;		
	}

	/**
	 * Takes a ticketinfo element and creates/returns it as a TicketResponse
	 * Object
	 * 
	 * @param element
	 * @return
	 */
	public static TicketResponse createTicketResponseFromDOM(Element element) {
		TicketResponse tr = new TicketResponse();

		NodeList list = element.getElementsByTagNameNS(
				CalDAVConstants.NS_XYTHOS, CalDAVConstants.ELEM_ID);
		Element temp = (Element) list.item(0);
		tr.setID(temp.getTextContent());

		list = element.getElementsByTagNameNS(CalDAVConstants.NS_DAV,
				CalDAVConstants.ELEM_HREF);
		temp = (Element) list.item(0);
		tr.setOwner(temp.getTextContent());

		list = element.getElementsByTagNameNS(CalDAVConstants.NS_XYTHOS,
				CalDAVConstants.ELEM_TIMEOUT);
		temp = (Element) list.item(0);
		String tempTO = temp.getTextContent();

		// Parses the timeout element's value into units and value in form Second-99999, otherwise no timeout (Infinite)
		int idx=tempTO.indexOf('-');
		if (idx!=-1)
		{
			// Store the Parsed Values
			//  default timeout is Integer("")
			tr.setUnits(tempTO.substring(0,idx));
			tr.setTimeout(new Integer(tempTO.substring(idx+1)));
		}
		list = element.getElementsByTagNameNS(CalDAVConstants.NS_XYTHOS,
				CalDAVConstants.ELEM_VISITS);
		temp = (Element) list.item(0);
		String visits = temp.getTextContent();
		Integer visitsInt = null;
		if (visits.equals(CalDAVConstants.INFINITY_STRING)) {
			visitsInt = CalDAVConstants.INFINITY;
		} else {
			visitsInt = new Integer(visits);
		}
		tr.setVisits(visitsInt);

		if (element.getElementsByTagNameNS(CalDAVConstants.NS_DAV,
				CalDAVConstants.ELEM_READ) != null) {
			tr.setRead(true);
		} else {
			tr.setRead(false);
		}

		if (element.getElementsByTagNameNS(CalDAVConstants.NS_DAV,
				CalDAVConstants.ELEM_WRITE) != null) {
			tr.setWrite(true);
		} else {
			tr.setWrite(false);
		}

		return tr;
	}

}
