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
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.methods.HttpRequestBodyMethodBase;
import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.CalDAV4JProtocolException;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.model.request.TicketRequest;
import org.osaf.caldav4j.model.response.TicketResponse;
import org.osaf.caldav4j.util.CaldavStatus;
import org.osaf.caldav4j.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Method to make a ticket on a valid URI.
 * 
 * @author EdBindl
 * 
 */
public class MkTicketMethod extends HttpRequestBodyMethodBase {
    private static final Log log = LogFactory.getLog(CalDAVReportMethod.class);

    private TicketRequest ticketRequest;

    private Document responseDocument = null;

    protected Vector<String> responseNames = null;

    protected DocumentBuilder builder = null;

    public MkTicketMethod(){
        
    }
    
    /**
     * 
     * @param path
     * @param ticketRequest
     */
    public MkTicketMethod(String path, TicketRequest ticketRequest) {
        this.ticketRequest = ticketRequest;
        setPath(path);
    }

    public String getName() {
        return CalDAVConstants.METHOD_MKTICKET;
    }

    public TicketRequest getTicketRequest() {
        return ticketRequest;
    }

    public void setTicketRequest(TicketRequest ticketRequest) {
        this.ticketRequest = ticketRequest;
    }

    public Document getResponseDocument() {
        return this.responseDocument;
    }

    /**
     * Creates a DOM Representation of the TicketRequest, turns into XML and
     * returns it in a string
     * 
     * @return
     */
    protected String generateRequestBody() {
        Document doc = null;
        try {
            doc = ticketRequest.createNewDocument(XMLUtils
                    .getDOMImplementation());
        } catch (DOMValidationException domve) {
            log
                    .error("Error trying to create DOM from MkTicketMethod: ",
                            domve);
            throw new RuntimeException(domve);
        }

        return XMLUtils.toPrettyXML(doc);
    }

    /**
     * Returns a TicketResponse from the response body.
     * 
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws CalDAV4JProtocolException -
     *             If the Response Body is not XML
     * @throws CalDAV4JException -
     *             if the Response Body is unavailable
     */
    public TicketResponse getResponseBodyAsTicketResponse() throws IOException,
            SAXException, CalDAV4JProtocolException, CalDAV4JException {
        Header header = getResponseHeader("Content-Type");
        String contentType = (header != null) ? header.getValue() : ""; 
        if (!contentType.startsWith("text/xml")) {
            log.error("Content type must be \"text/xml\" to parse as an "
                    + "xml resource. Type was: " + contentType);
            throw new CalDAV4JProtocolException(
                    "Content type must be \"text/xml\" to parse as an "
                            + "xml resource");
        }
        InputStream inStream = getResponseBodyAsStream();
        if (inStream != null) {

            if (builder == null) {
                try {
                    // TODO: avoid the newInstance call for each method
                    // instance for performance reasons.
                    DocumentBuilderFactory factory = DocumentBuilderFactory
                            .newInstance();
                    factory.setNamespaceAware(true);
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new HttpException("XML Parser Configuration error: "
                            + e.getMessage());
                }
            }
            this.responseDocument = builder.parse(inStream);

            inStream.close();
        } else {
            throw new CalDAV4JException(
                    "Response Body is not Available, Status Code: "
                            + getStatusCode());
        }

        int statusCode = getStatusCode();

        if (CaldavStatus.SC_OK == statusCode) {
            Element docElement = this.responseDocument.getDocumentElement();

            TicketResponse tr = XMLUtils
                    .createTicketResponseFromDOM(docElement);

            return tr;
        }
        return null;

    }
    
    public void addRequestHeaders(HttpState state, HttpConnection conn)
    throws IOException, HttpException {

        super.addRequestHeaders(state, conn);
        
        addRequestHeader(CalDAVConstants.HEADER_CONTENT_TYPE, CalDAVConstants.CONTENT_TYPE_TEXT_XML);

    }
    
    /**
     * Handles the authoring of the Request Body
     */
    protected void writeRequest(HttpState state, HttpConnection conn)
            throws IOException, HttpException {
        String contents = generateRequestBody();
        // be nice - allow overriding functions to return null or empty
        // strings for no content.
        if (contents == null) {
            contents = "";
        }
        setRequestBody(contents);
        super.writeRequest(state, conn);
    }

}
