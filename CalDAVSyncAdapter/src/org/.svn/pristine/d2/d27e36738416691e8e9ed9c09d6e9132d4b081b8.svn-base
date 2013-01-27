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
package org.osaf.caldav4j.model.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 * This class will hold all pertinent information for the request of MkTicket.
 * If timeout or visits is null when the MkTicket request is sent, they will not
 * be included in the request and the response should be checked to determine
 * their value. The class extends OutputsDOMbase so that it can easily be
 * converted to DOM.
 * 
 * @author EdBindl
 * 
 */
public class TicketRequest extends OutputsDOMBase {

	private static final Log log = LogFactory.getLog(TicketRequest.class);

	private Integer timeout = null;

	private Integer visits = null;

	private boolean read = false;

	private boolean write = false;

	public TicketRequest() {

	}

	public TicketRequest(Integer timeout, Integer visits, boolean read,
			boolean write) {
		this.timeout = timeout;
		this.visits = visits;
		this.read = read;
		this.write = write;
	}

	/**
	 * The assumed unit for timeout is seconds.
	 * 
	 * @return
	 */
	public Integer getTimeout() {
		return this.timeout;
	}

	/**
	 * The value for infinity is stored in
	 * org.osaf.caldav4j.CalDAVConstants.INFINITY
	 * 
	 * @return 
	 */
	public Integer getVisits() {
		return this.visits;
	}

	public boolean getRead() {
		return read;
	}

	public boolean getWrite() {
		return write;
	}

	/**
	 * The assumed unit for timeout is seconds.
	 * 
	 * @param timeout
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * The value for infinity is stored in
	 * org.osaf.caldav4j.CalDAVConstants.INFINITY
	 * 
	 * @param visits
	 */
	public void setVisits(Integer visits) {
		this.visits = visits;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public void setWrite(boolean write) {
		this.write = write;
	}

	protected String getElementName() {
		return CalDAVConstants.ELEM_TICKETINFO;
	}

	protected String getNamespaceQualifier() {
		return CalDAVConstants.NS_QUAL_TICKET;
	}

	protected String getNamespaceURI() {
		return CalDAVConstants.NS_XYTHOS;

	}

	/**
	 * Creates and returns a Collection of all the TicketRequest's Children
	 */

	protected Collection<OutputsDOM> getChildren() {
		ArrayList<OutputsDOM> children = new ArrayList<OutputsDOM>();
		if (timeout != null) {
			SimpleDOMOutputtingObject tempTimeout = new SimpleDOMOutputtingObject(
					getNamespaceURI(), getNamespaceQualifier(),
					CalDAVConstants.ELEM_TIMEOUT);
			tempTimeout.setTextContent(CalDAVConstants.TIMEOUT_UNITS_SECONDS
					+ timeout.toString());
			children.add(tempTimeout);
		}
		if (visits != null) {
			SimpleDOMOutputtingObject tempVisits = new SimpleDOMOutputtingObject(
					getNamespaceURI(), getNamespaceQualifier(),
					CalDAVConstants.ELEM_VISITS);
			if (visits == CalDAVConstants.INFINITY) {
				tempVisits.setTextContent(CalDAVConstants.INFINITY_STRING);
			}

			else {
				tempVisits.setTextContent(visits.toString());
			}
			children.add(tempVisits);
		}
		if (read != false || write != false) {
			SimpleDOMOutputtingObject tempPrivliges = new SimpleDOMOutputtingObject(
					CalDAVConstants.NS_DAV, CalDAVConstants.NS_QUAL_DAV,
					CalDAVConstants.ELEM_PRIVILIGE);
			if (read) {
				tempPrivliges.addChild(new SimpleDOMOutputtingObject(
						CalDAVConstants.NS_DAV, CalDAVConstants.NS_QUAL_DAV,
						CalDAVConstants.ELEM_READ));
			}
			if (write) {
				tempPrivliges.addChild(new SimpleDOMOutputtingObject(
						CalDAVConstants.NS_DAV, CalDAVConstants.NS_QUAL_DAV,
						CalDAVConstants.ELEM_WRITE));
			}
			children.add(tempPrivliges);
		}
		return children;
	}

	protected Map<String, String> getAttributes() {
		return null;
	}

	protected String getTextContent() {
		return null;
	}

}
