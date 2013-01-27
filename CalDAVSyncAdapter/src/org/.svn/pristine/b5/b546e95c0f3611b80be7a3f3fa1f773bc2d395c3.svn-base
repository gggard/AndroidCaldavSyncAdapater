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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class will hold all pertinant information for the MkTicket Response. Any
 * null values in a TicketResponse indicate that the request did not contain
 * that information.
 * 
 * @author EdBindl
 * 
 */

public class TicketResponse {
	private static final Log log = LogFactory.getLog(TicketResponse.class);

	private Integer timeout = null;

	private Integer visits = null;

	private boolean read = false;

	private boolean write = false;

	private String id = null;

	private String owner = null;

	private String units = null;

	public TicketResponse() {
	}

	public TicketResponse(Integer timeout, Integer visits, boolean read,
			boolean write, String id, String owner, String units) {
		this.timeout = timeout;
		this.visits = visits;
		this.read = read;
		this.write = write;
		this.id = id;
		this.owner = owner;
		this.units = units;
	}

	public String getID() {
		return this.id;
	}

	public String getOwner() {
		return this.owner;
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
	 * org.osaf.caldav4j.CalDAVConstants.INFINITY .
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
	 * org.osaf.caldav4j.CalDAVConstants.INFINITY .
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

	public void setID(String id) {
		this.id = id;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getUnits() {
		return this.units;
	}

}
