/**
 * Copyright (c) 2012-2013, Gerald Garcia
 * 
 * This file is part of Andoid Caldav Sync Adapter Free.
 *
 * Andoid Caldav Sync Adapter Free is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or at your option any later version.
 *
 * Andoid Caldav Sync Adapter Free is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoid Caldav Sync Adapter Free.  
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.gege.caldavsyncadapter.caldav.entities;

import java.net.URI;

public class Calendar {

	URI uri;
	private String displayName;
	private String cTag;
	private String calendarColor = "";



	public URI getURI() {
		return uri;
	}

	public void setURI(URI uri) {
		this.uri = uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setCTag(String cTag) {
		this.cTag = cTag;
	}
	
	public String getcTag() {
		return cTag;
	}
	
	public void setCalendarColor(String color) {
		calendarColor = color;
	}
	
	public String getCalendarColor() {
		return calendarColor;
	}

}
