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
