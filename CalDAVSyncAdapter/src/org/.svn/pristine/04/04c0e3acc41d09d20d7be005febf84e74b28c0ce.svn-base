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
package org.osaf.caldav4j.model.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.ResponseEntity;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.util.XMLUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * @author EdBindl
 */
public class TicketDiscoveryProperty extends BaseProperty {

	private static final Log log = LogFactory
			.getLog(TicketDiscoveryProperty.class);

	List<TicketResponse> tickets = null;

	public static final String ELEMENT_CALENDAR_DATA = "ticketdiscovery";

	public TicketDiscoveryProperty(ResponseEntity response, Element element) {
		super(response, element);
		parseTickets();
	}

	public List<TicketResponse> getTickets() {
		if (tickets == null) {
			parseTickets();
		}
		return tickets;
	}
	
	public List<String> getTicketIDs(){
		if (tickets == null) {
			parseTickets();
		}
		List<String> ticketIDs = new ArrayList<String>();
		
		int length = tickets.size();

		for (int r = 0; r < length; r++) {
			ticketIDs.add(tickets.get(r).getID());
		}
		return ticketIDs;
	}

	/**
	 * Handles the reponse and Stores each ticketinfo as a TicketResponse
	 * Object, in a List of TicketResponses
	 * 
	 */
	public void parseTickets() {
		tickets = new ArrayList<TicketResponse>();
		if (element != null) {
			NodeList list = element.getElementsByTagNameNS(
					CalDAVConstants.NS_XYTHOS,
					CalDAVConstants.ELEM_TICKETINFO);
			int length = list.getLength();

			for (int r = 0; r < length; r++) {
				Element element = (Element) list.item(r);
				tickets.add(XMLUtils.createTicketResponseFromDOM(element));
			}

		}
	}
}
