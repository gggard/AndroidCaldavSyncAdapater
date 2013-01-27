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

import java.io.IOException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.osaf.caldav4j.CalDAVConstants;

/**
 * This class was extended in order to handle tickets. If a ticket is set on a
 * resouce is will be included in the request. The default Action is to put the
 * ticket in the Request Header.
 * 
 * @author EdBindl
 * 
 */
public class HttpClient extends org.apache.commons.httpclient.HttpClient {

    private String ticket = null;

    /**
     * Describes where the ticket should be put when making requests
     */
    public enum TicketLocation {
        
        /**
         * Indicates that the ticket should be passed in the header
         */
        HEADER,
        
        /**
         * Indicates that the ticket should be passed as a query parameter
         */
        QUERY_PARAM
    };

    private TicketLocation ticketLocation = TicketLocation.HEADER;

    public HttpClient() {
        super();
    }

    public HttpClient(HttpConnectionManager httpConnectionManager) {
        super();
    }

    public void setTicket(String ticket) {
        this.ticket = new String(ticket);
    }

    public String getTicket() {
        return this.ticket;
    }

    public void setTicketLocation(TicketLocation ticketLocation) {
        this.ticketLocation = ticketLocation;
    }

    /**
     * Indicates where to pass the header
     * @return
     */
    public TicketLocation getTicketLocation() {
        return this.ticketLocation;
    }

    /**
     * Overwritten to Handle tickets.
     */
    public int executeMethod(HostConfiguration hostConfiguration,
            HttpMethod method, HttpState state) throws IOException,
            HttpException {
        
        if (ticket != null){
            if (ticketLocation == TicketLocation.HEADER) {
                method.addRequestHeader(CalDAVConstants.TICKET_HEADER, ticket);
            }
            
            //FIXME what if there are other query parameters! 
            if (ticketLocation == TicketLocation.QUERY_PARAM) {
                method.setPath(method.getPath() + CalDAVConstants.URL_APPENDER
                        + ticket);
            }
        }
        return super.executeMethod(hostConfiguration, method, state);
    }

}
