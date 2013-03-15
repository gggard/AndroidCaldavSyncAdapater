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

package org.gege.caldavsyncadapter.android.entities;

import android.net.Uri;

public class AndroidEvent {
	
	Uri uri;
	
	String eTag;

	private Uri calendarUri;

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getETag() {
		return eTag;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	public Uri getCalendarUri() {
		return this.calendarUri;
	}

	public void setCalendarUri(Uri calendarUri) {
		this.calendarUri = calendarUri;
	}
	
	
	
}
