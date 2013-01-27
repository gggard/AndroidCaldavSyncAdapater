package org.gege.caldavsyncadapter.caldav.entities;

import java.net.URI;

public class Calendar {

	URI uri;
	private String displayName;
	private String cTag;



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

}
