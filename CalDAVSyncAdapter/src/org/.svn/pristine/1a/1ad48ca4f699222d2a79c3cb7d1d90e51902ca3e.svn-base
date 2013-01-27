/*
 * Copyright 2005 Open Source Applications Foundation
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

package org.osaf.caldav4j;

import org.apache.webdav.lib.util.QName;
import org.osaf.caldav4j.model.request.PropProperty;

public interface CalDAVConstants {
    
    public static final String TICKET_HEADER = "Ticket";

    public static final String METHOD_MKCALENDAR = "MKCALENDAR";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_REPORT = "REPORT";
    public static final String METHOD_MKTICKET = "MKTICKET";
    public static final String METHOD_DELTICKET = "DELTICKET";
    
    public static final String NS_CALDAV = "urn:ietf:params:xml:ns:caldav";
    public static final String NS_DAV = "DAV:";
    public static final String NS_QUAL_DAV = "D";
    public static final String NS_QUAL_CALDAV = "C";
    public static final String NS_XYTHOS = "http://www.xythos.com/namespaces/StorageServer";
    public static final String NS_QUAL_TICKET = "ticket";


    public static final String PROC_ID_DEFAULT =  "-//OSAF//NONSGML CalDAV4j Client//EN";
    
    public static final String HEADER_IF_NONE_MATCH = "If-None-Match";
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ETAG = "ETag";
    
    public static final String CONTENT_TYPE_CALENDAR = "text/calendar";
    public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
    
    
	public static final String DAV_ACL ="acl";
	public static final String DAV_PROP ="prop";
	public static final String DAV_PROPFIND ="propfind";
	public static final String DAV_DISPLAYNAME = "displayname" ;
	public static final String DAV_PRINCIPAL_OWNER = "owner";
	public static final String DAV_PRINCIPAL_AUTHENTICATED = "authenticated";
	
	public static final String CALDAV_PRIVILEGE_READ_FREE_BUSY =  "read-free-busy";
	public static final String CALDAV_PRIVILEGE_SCHEDULE =  "schedule";
	public static final String CALDAV_CALENDAR_DESCRIPTION = "calendar-description" ;
    public static final String CALDAV_CALENDAR_QUERY = "calendar-query";
    public static final String CALDAV_CALENDAR_DATA = "calendar-data";

    public static final String ATTR_START = "start";
    public static final String ATTR_END = "end";

    public static final String ELEM_TICKETDISCOVERY = "ticketdiscovery";
    public static final String ELEM_TICKETINFO = "ticketinfo";
    public static final String ELEM_TIMEOUT = "timeout";
    public static final String ELEM_VISITS = "visits";
    public static final String ELEM_PRIVILIGE = "privilege";
    public static final String ELEM_READ = "read";
    public static final String ELEM_WRITE = "write";
    public static final String ELEM_ID = "id";
    public static final String ELEM_OWNER = "owner";
    public static final String ELEM_HREF = "href";
    public static final String ELEM_ALLPROP = "allprop";
    public static final String ELEM_EXPAND_RECURRENCE_SET = "expand";
    public static final String ELEM_LIMIT_RECURRENCE_SET = "limit-recurrence-set";
    public static final String ELEM_PROPNAME = "propname";
    public static final String ELEM_FILTER = "filter";
    public static final String ELEM_GETETAG = "getetag";


    public static final String COLLATION_ASCII = "i;ascii-casemap";
    public static final String COLLATION_OCTET = "i;octet";
   
    public static final Integer INFINITY = -1;
    public static final String  INFINITY_STRING = "infinity";
    
    public static final String TIMEOUT_UNITS_SECONDS = "Second-";

    public static final String URL_APPENDER = "?ticket=";

    // request property
	public static final PropProperty PROP_GETETAG = new PropProperty(NS_DAV,NS_QUAL_DAV, ELEM_GETETAG);
	public static final PropProperty PROP_ALLPROP = new PropProperty(NS_DAV,NS_QUAL_DAV, ELEM_ALLPROP);

	// response tag
    public static final QName QNAME_GETETAG = new QName(NS_DAV, ELEM_GETETAG);
	public static final QName QNAME_DISPLAYNAME = new QName(NS_DAV, DAV_DISPLAYNAME);
	public static final QName QNAME_CALENDAR_DESCRIPTION = new QName(NS_CALDAV,CALDAV_CALENDAR_DESCRIPTION);
	public static final QName QNAME_ACL = new QName(NS_DAV, "acl");
	
}
