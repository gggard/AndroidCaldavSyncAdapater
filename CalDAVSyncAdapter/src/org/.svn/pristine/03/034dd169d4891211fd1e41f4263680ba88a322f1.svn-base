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
package org.osaf.caldav4j.methods;

import static org.osaf.caldav4j.CalDAVConstants.NS_CALDAV;
import static org.osaf.caldav4j.CalDAVConstants.NS_DAV;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Property;
import org.apache.webdav.lib.properties.AclProperty;
import org.apache.webdav.lib.properties.PropertyFactory;
import org.apache.webdav.lib.util.DOMUtils;
import org.apache.webdav.lib.util.QName;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.model.response.CalDAVResponse;
import org.osaf.caldav4j.model.response.TicketDiscoveryProperty;
import org.osaf.caldav4j.util.CaldavStatus;
import org.osaf.caldav4j.util.XMLUtils;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This method is overwritten in order to register the ticketdiscovery element
 * with the PropertyFactory.
 * 
 * @author EdBindl
 * 
 */
public class PropFindMethod extends org.apache.webdav.lib.methods.PropFindMethod {
    private static final Log log = LogFactory
    	.getLog(PropFindMethod.class);
    private OutputsDOM propFindRequest;


    /**
     * Registers the TicketDiscoveryProperty with the PropertyFactory
     */
    static {
        try {
            PropertyFactory.register(CalDAVConstants.NS_XYTHOS,
                    CalDAVConstants.ELEM_TICKETDISCOVERY,
                    TicketDiscoveryProperty.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not register TicketDiscoveryProperty!", e);
        }
    }
    
    public PropFindMethod() {
        super();
    }
    
    public PropFindMethod(String path, Enumeration propertyNames) {
        super(path, propertyNames);
    }
//    
//    private parseResponseProperties(String urlPath) {
//    	Enumeration<Property> myEnum = getResponseProperties(urlPath);
//    	
//    	while (getResponseProperties(urlPath).hasMoreElements()) {
//    		BaseProperty e =  (BaseProperty) myEnum.nextElement();
//            String[] types={"acl","calendar-description","displayname"};
//            for (int i=0 ; i<types.length ; i++) {
//            	if (! types[i].equals(e.getName()))
//            		continue;
//            	
//            	switch (i) {
//				case 0:
//					aclProperty = (AclProperty) e;
//					
//					break;
//
//				default:
//					break;
//				}
//            }
//
//    		
//    	}
//    }
    /**
     * Returns an enumeration of <code>Property</code> objects.
     */
//    public Enumeration<Property> getResponseProperties(String urlPath) {
//    	checkUsed();
//
//    	Response response = (Response) getResponseHashtable().get(urlPath);
//    	if (response == null){
//    		response = (Response) getResponseHashtable().get(stripHost(urlPath));
//    	}
//    	if (response != null) {
//    		return  response.getProperties();
//    	} else {
//    		return  (new Vector()).elements();
//    	}
//    }
    
    public void setPropFindRequest(OutputsDOM myprop) {
        this.propFindRequest = myprop;
    }

    /**
     * Generates a request body from the calendar query.
     */
    protected String generateRequestBody() {
        Document doc = null;
        try {
            doc = propFindRequest.createNewDocument(XMLUtils
                    .getDOMImplementation());
        } catch (DOMValidationException domve) {
            log.error("Error trying to create DOM from CalDAVPropfindRequest: ", domve);
            throw new RuntimeException(domve);
        }
        return XMLUtils.toPrettyXML(doc);
    }


	
	//
	// recycle reportmethods
	//
    private Hashtable<String, CalDAVResponse> responseHashtable = null;
    private static Map<QName, Error> errorMap = null;
    private Error error = null;
    
    public enum ErrorType{PRECONDITION, POSTCONDITON}

    /**
     * Precondtions and Postconditions
     * @author bobbyrullo
     *
     */
    public enum Error {
        SUPPORTED_CALENDAR_DATA(ErrorType.PRECONDITION, NS_CALDAV, "supported-calendar-data"),
        VALID_FILTER(ErrorType.PRECONDITION, NS_CALDAV, "valid-filter"),
        NUMBER_OF_MATCHES_WITHIN_LIMITS(ErrorType.POSTCONDITON, NS_DAV, "number-of-matches-within-limits");
        
        private final ErrorType errorType;
        private final String namespaceURI;
        private final String elementName;
        
        Error(ErrorType errorType, String namespaceURI, String elementName){
            this.errorType = errorType;
            this.namespaceURI = namespaceURI;
            this.elementName = elementName;
        }
        
        public ErrorType errorType() { return errorType; }
        public String namespaceURI() { return namespaceURI; }
        public String elementName(){ return elementName; }
        
    }
    
    static {
        errorMap = new HashMap<QName, Error>();
        for (Error error : Error.values()) {
            errorMap.put(new QName(error.namespaceURI(), error.elementName()),
                    error);
        }
    }
    
    public static final String ELEMENT_ERROR ="error";
    
    /**
     * Return an enumeration containing the responses.
     *
     * @return An enumeration containing objects implementing the
     * ResponseEntity interface
     */
    public Enumeration<CalDAVResponse> getResponses() {
        return getResponseHashtable().elements();
    }
    
    public Error getError(){
        return error;
    }
    
    /**
     * return the AclProperty relative to a given url
     * @author rpolli
     * @param urlPath
     * @return AclProperty xml response or null if missing
     */
    public AclProperty getAcl(String urlPath) {
    	return (AclProperty) getWebDavProperty(urlPath, CalDAVConstants.QNAME_ACL);
    }
    public Ace[] getAces(String urlPath) throws CalDAV4JException {
    	int status = -1;
    	AclProperty acls = (AclProperty) getWebDavProperty(urlPath, CalDAVConstants.QNAME_ACL);
    	if (acls != null) {
        	status = acls.getStatusCode();
    		switch (status) {
			case CaldavStatus.SC_OK:
				return acls.getAces();
			default:
				break;
			}
    	}
    	throw new CalDAV4JException("Error gettinh ACLs. PROPFIND status is: " + status);
    }
    public String getCalendarDescription(String urlPath) {
    	Property p =  getWebDavProperty(urlPath, CalDAVConstants.QNAME_CALENDAR_DESCRIPTION);
    	if (p!= null) {
    		return p.getPropertyAsString();
    	} else {
    		return "";
    	}
    }
    public String getDisplayName(String urlPath) {
    	Property p= getWebDavProperty(urlPath, CalDAVConstants.QNAME_DISPLAYNAME);
    	if (p != null) {
    		return  p.getPropertyAsString();
    	} else {
    		return "";
    	}
    }
    /**
     * 
     * @param caldavProperty can be CaldavConstants.QNAME_XXXXX
     * @return
     * 
     * TODO check equivalent URIs (eg. duplicate|trailing "/")
     */
    private Property getWebDavProperty(String urlPath, QName property) {
    	CalDAVResponse response = getResponseHashtable().get(urlPath);
    	if (response != null) {
    		return response.getProperty(property);
    	} else {
    		 response = getResponseHashtable().get(urlPath+"/");
    	}
    	if (response != null) {
    		return response.getProperty(property);
    	} else {
    		log.warn("Can't find object at: " + urlPath);
    		return null;
    	}
    		
    }
    
    protected Hashtable<String, CalDAVResponse> getResponseHashtable() {
        checkUsed();
        if (responseHashtable == null) {
            initHashtable();
        }
        return responseHashtable;
    }

    protected Vector<String> getResponseURLs() {
        checkUsed();
        if (responseHashtable == null) {
            initHashtable();
        }
        return responseURLs;
    }
    /**
     * A lot of this code had to be copied from the parent XMLResponseMethodBase, since it's 
     * initHashtable doesn't allow for new types of Responses.
     * 
     * Of course, the same mistake is being made here, so it is a TODO to fix that
     *
     */
    @SuppressWarnings("unchecked")
    private void initHashtable(){
        responseHashtable = new Hashtable<String, CalDAVResponse>();
        responseURLs = new Vector<String>();
        // Also accept OK sent by buggy servers in reply to a PROPFIND
        // or REPORT (Xythos, Catacomb, ...?).
        int statusCode = getStatusCode();
        if (statusCode == CaldavStatus.SC_MULTI_STATUS) {


            Document rdoc = getResponseDocument();

            NodeList list = null;
            if (rdoc != null) {
                Element multistatus = getResponseDocument().getDocumentElement();
                list = multistatus.getChildNodes();
            }

            if (list != null) {
                for (int i = 0; i < list.getLength(); i++) {
                    try {
                        Element child = (Element) list.item(i);
                        String name = DOMUtils.getElementLocalName(child);
                        String namespace = DOMUtils.getElementNamespaceURI
                            (child);
                        if (Response.TAG_NAME.equals(name) &&
                            "DAV:".equals(namespace)) {
                            CalDAVResponse response =
                                new CalDAVResponse(child);
                            String href = response.getHref() ;
                            responseHashtable.put(href,response);
                            responseURLs.add(href);
                        }
                    } catch (ClassCastException e) {
                    }
                }
            }
        } else if (statusCode == CaldavStatus.SC_CONFLICT || statusCode == CaldavStatus
                .SC_FORBIDDEN){
            Document rdoc = getResponseDocument();
            Element errorElement = rdoc.getDocumentElement();
            
            // first make sure that the element is actually an error.
            if (!errorElement.getNamespaceURI().equals(NS_DAV)
                    || !errorElement.getLocalName().equals(ELEMENT_ERROR)) {
                Node condition = errorElement.getChildNodes().item(0);
                error = errorMap.get(new QName(condition.getNamespaceURI(),
                        condition.getLocalName()));
            }
        }
    }

}
