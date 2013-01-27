package org.osaf.caldav4j.model.response;

import org.apache.commons.lang.StringUtils;
import org.apache.webdav.lib.PropertyName;
import org.osaf.caldav4j.CalDAVConstants;

/*
Principal encapsulates the DAV:principal element which identifies the principal to which this ACE applies. RFC 3744 defines the following structure for this element:

	 <!ELEMENT principal (href | all | authenticated | unauthenticated | property | self)>
*/
public class Principal  {

	private String href;
	private PropertyName propertyName;
	private boolean all, authenticated, unauthenticated, self;
	private String value;

	private String[] principalValues = new String[] {"all", "authenticated", "unauthenticated", "self", "owner"};

	public Principal() {
		super();
	}
	public Principal(String defaultPrincipal) {
		super();
		int i=0;
		for (i=0;i<principalValues.length;i++) {
			if (StringUtils.equalsIgnoreCase(principalValues[i], defaultPrincipal)) 
				switch (i) {
				case 0:
					setAll(true);
					break;
				case 1:
					setAuthenticated(true);
					break;
				case 2:
					setUnauthenticated(true);
					break;
				case 3:
					setSelf(true);
					break;
				case 4:
					setOwner();
					break;
				default:
					break;
				}	
		}
		
		this.value = defaultPrincipal;
	}
public String getValue() {
	return value;
}
	
	// TODO set href
	public boolean isOwner() {
		return ( (getPropertyName() != null) &&
				StringUtils.equalsIgnoreCase("owner",getPropertyName().getLocalName())
				); 
	}
	
	// TODO set href
	public boolean isGroup() {
		return (getPropertyName()!=null) &&
			StringUtils.equalsIgnoreCase("group",getPropertyName().getLocalName());
	}
	
	public void setOwner() {
		PropertyName o = new PropertyName(CalDAVConstants.NS_DAV, "owner");
		setPropertyName(o);
	}
	public boolean isAll() {
		return all;
	}
	public void setAll(boolean all) {
		this.all = all;
	}
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	public boolean isSelf() {
		return self;
	}
	public void setSelf(boolean self) {
		this.self = self;
	}
	public String getHref() {
		return href;
	};
	public PropertyName getPropertyName() {
		return propertyName;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	public void setPropertyName(PropertyName property) {
		this.propertyName = property;
	}
	public void setUnauthenticated(boolean unauthenticated) {
		this.unauthenticated = unauthenticated;
	}
	public boolean isUnauthenticated() {
		return unauthenticated;
	}
	
}
