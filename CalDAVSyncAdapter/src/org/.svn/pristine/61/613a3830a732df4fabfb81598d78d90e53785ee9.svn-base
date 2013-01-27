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
 * 
 * 
 */

package org.osaf.caldav4j;

import static org.osaf.caldav4j.util.ICalendarUtils.getMasterEvent;
import static org.osaf.caldav4j.util.ICalendarUtils.getUIDValue;
import static org.osaf.caldav4j.util.UrlUtils.stripHost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.ResourceNotFoundException;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.CalDAVReportMethod;
import org.osaf.caldav4j.methods.DelTicketMethod;
import org.osaf.caldav4j.methods.DeleteMethod;
import org.osaf.caldav4j.methods.GetMethod;
import org.osaf.caldav4j.methods.HttpClient;
import org.osaf.caldav4j.methods.MkCalendarMethod;
import org.osaf.caldav4j.methods.MkTicketMethod;
import org.osaf.caldav4j.methods.PropFindMethod;
import org.osaf.caldav4j.methods.PutMethod;
import org.osaf.caldav4j.model.request.CalDAVProp;
import org.osaf.caldav4j.model.request.CalendarData;
import org.osaf.caldav4j.model.request.CalendarMultiget;
import org.osaf.caldav4j.model.request.CalendarQuery;
import org.osaf.caldav4j.model.request.Comp;
import org.osaf.caldav4j.model.request.CompFilter;
import org.osaf.caldav4j.model.request.PropFilter;
import org.osaf.caldav4j.model.request.PropProperty;
import org.osaf.caldav4j.model.request.TextMatch;
import org.osaf.caldav4j.model.request.TicketRequest;
import org.osaf.caldav4j.model.request.TimeRange;
import org.osaf.caldav4j.model.response.CalDAVResponse;
import org.osaf.caldav4j.model.response.TicketDiscoveryProperty;
import org.osaf.caldav4j.model.response.TicketResponse;
import org.osaf.caldav4j.model.util.PropertyFactory;
import org.osaf.caldav4j.util.CaldavStatus;
import org.osaf.caldav4j.util.ICalendarUtils;

/**
 * This class provides a high level API to a calendar collection on a CalDAV server.
 * 
 * @author bobbyrullo
 * @author ptr.ventura_at_gmail.com changed addEvent
 * @deprecated Use CalDAVCollection instead
 */
public class CalDAVCalendarCollection extends CalDAVCalendarCollectionBase{

	

	public CalDAVCalendarCollection(){

	}

	/**
	 * Creates a new CalDAVCalendar collection with the specified paramters
	 * 
	 * @param path The path to the collection 
	 * @param hostConfiguration Host information for the CalDAV Server 
	 * @param methodFactory methodFactory to obtail HTTP methods from
	 * @param prodId String identifying who creates the iCalendar objects
	 */
	public CalDAVCalendarCollection(String path,
			HostConfiguration hostConfiguration,
			CalDAV4JMethodFactory methodFactory, String prodId) {
		setCalendarCollectionRoot(path);
		this.hostConfiguration = hostConfiguration;
		this.methodFactory = methodFactory;
		this.prodId = prodId;
	}

	//Configuration Methods

	/**
	 * Returns the icalendar object which contains the event with the specified
	 * UID.
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param uid The uniqueID of the event to find
	 * @return the Calendar object containing the event with this UID
	 * @throws CalDAV4JException if there was a problem, or if the resource could 
	 *         not be found.
	 */
	public Calendar getCalendarForEventUID(HttpClient httpClient, String uid)
	throws CalDAV4JException, ResourceNotFoundException {
		return getCalDAVResourceForEventUID(httpClient, uid).getCalendar();
	}

	/**
	 * Gets an icalendar object at the specified path, relative to the
	 * collection path
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param relativePath the path, relative to the collection path
	 * @return the Calendar object at the specified path
	 * @throws CalDAV4JException
	 */
	public Calendar getCalendarByPath(HttpClient httpClient, String relativePath) throws CalDAV4JException{
		CalDAVResource resource = getCalDAVResource(httpClient, getAbsolutePath(relativePath));
		return resource.getCalendar();
	}

	/**
	 * Returns all Calendars which contain events which have instances who fall within 
	 * the two dates. Note that recurring events are NOT expanded. 
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param beginDate the beginning of the date range. Must be a UTC date
	 * @param endDate the end of the date range. Must be a UTC date.
	 * @return a List of Calendars
	 * @throws CalDAV4JException if there was a problem
	 */
	public List<Calendar> getEventResources(HttpClient httpClient,
			Date beginDate, Date endDate)
			throws CalDAV4JException {
		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");

		query.addProperty(CalDAVConstants.PROP_GETETAG);
		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(Component.VEVENT);
		vEventCompFilter.setTimeRange(new TimeRange("C", beginDate, endDate));

		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);

		return getComponentByQuery(httpClient, Component.VEVENT, query);
	}

	/**
	 * Deletes an event based on it's uid. If the calendar resource containing the
	 * event contains no other VEVENT's, the entire resource will be deleted.
	 * 
	 * If the uid is for a recurring event, the master event and all exceptions will
	 * be deleted
	 * 
	 * @param uid
	 */
	public void deleteEvent(HttpClient httpClient,String uid) throws CalDAV4JException{
		CalDAVResource resource = getCalDAVResourceForEventUID(httpClient, uid);
		Calendar calendar = resource.getCalendar();
		ComponentList eventList = calendar.getComponents().getComponents(Component.VEVENT);
		List<Component> componentsToRemove = new ArrayList<Component>();
		boolean hasOtherEvents = false;
		for (Object o : eventList){
			VEvent event = (VEvent) o;
			String curUID = ICalendarUtils.getUIDValue(event);
			if (!uid.equals(curUID)){
				hasOtherEvents = true;
			} else {
				componentsToRemove.add(event);
			}
		}

		if (hasOtherEvents){
			if (componentsToRemove.size() == 0){
				throw new ResourceNotFoundException(
						ResourceNotFoundException.IdentifierType.UID, uid);
			}

			for (Component removeMe : componentsToRemove){
				calendar.getComponents().remove(removeMe);
			}
			put(httpClient, calendar, stripHost(resource.getResourceMetadata().getHref()),
					resource.getResourceMetadata().getETag());
			return;
		} else {
			delete(httpClient, stripHost(resource.getResourceMetadata().getHref()));
		}
	}

	/**
	 * Creates a calendar at the specified path 
	 *
	 */
	public void createCalendar(HttpClient httpClient) throws CalDAV4JException{
		MkCalendarMethod mkCalendarMethod = new MkCalendarMethod();
		mkCalendarMethod.setPath(getCalendarCollectionRoot());
		try {
			httpClient.executeMethod(hostConfiguration, mkCalendarMethod);
			int statusCode = mkCalendarMethod.getStatusCode();
			if (statusCode != CaldavStatus.SC_CREATED){
				throw new CalDAV4JException("Create Failed with Status: "
						+ statusCode + " and body: \n"
						+ mkCalendarMethod.getResponseBodyAsString());
			}
		} catch (Exception e){
			throw new CalDAV4JException("Trouble executing MKCalendar", e);
		}
	}

	/**
	 * Adds a new Calendar with the given VEvent and VTimeZone to the collection.
	 * 
	 * Tries to use the event UID followed by ".ics" as the name of the 
	 * resource, otherwise will use the UID followed by a random number and 
	 * ".ics" 
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param vevent The VEvent to put in the Calendar
	 * 
	 * @param timezone The VTimeZone of the VEvent if it references one, 
	 *                 otherwise null
	 * @throws CalDAV4JException
	 * @todo specify somewhere the kind of caldav error...
	 */
	public void addEvent(HttpClient httpClient, VEvent vevent, VTimeZone timezone)
	throws CalDAV4JException {
		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId(prodId));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		if (timezone != null){
			calendar.getComponents().add(timezone);
		}
		calendar.getComponents().add(vevent);

		boolean didIt = false;
		for (int x = 0; x < 3 && !didIt; x++) {
			String resourceName = null;
			if (x == 0) {
				resourceName = ICalendarUtils.getUIDValue(vevent) + ".ics";
			} else {
				resourceName = ICalendarUtils.getUIDValue(vevent) + "-"
				+ random.nextInt() + ".ics";
			}
			
			PutMethod putMethod = createPutMethodForNewResource(resourceName,
					calendar);
			try {
				httpClient.executeMethod(getHostConfiguration(), putMethod);
				//fixed for nullpointerexception
				String etag = ( putMethod.getResponseHeader("ETag") != null) ? putMethod.getResponseHeader("ETag").getValue() :  ""; 
				CalDAVResource calDAVResource = new CalDAVResource(calendar,
						etag, getHref((putMethod.getPath())));
				
				cache.putResource(calDAVResource);
				
			} catch (Exception e) {
				throw new CalDAV4JException("Trouble executing PUT", e);
			}
			int statusCode = putMethod.getStatusCode();

			if ((CaldavStatus.SC_CREATED == statusCode) || 
					(CaldavStatus.SC_NO_CONTENT == statusCode)){
				didIt = true;
			} else if (CaldavStatus.SC_PRECONDITION_FAILED != statusCode){
				//must be some other problem, throw an exception
				try {
					throw new CalDAV4JException("Unexpected status code: "
							+ statusCode + "\n"
							+ putMethod.getResponseBodyAsString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new CalDAV4JException("Unexpected status code: "
							+ statusCode + "\n"
							+ "error in getResponseBodyAsString()");
				}
			}
		}
	}
	/**
	 * adds a calendar object to caldav collection using UID.ics as file name
	 * @param httpClient
	 * @param c
	 * @throws CalDAV4JException
	 */
	public void addCalendar(HttpClient httpClient, Calendar c) 
		throws CalDAV4JException {
		
		String uid = null;
		
		ComponentList cl = c.getComponents();
		
		// get uid from first non VTIMEZONE event
		Iterator it = cl.iterator();
		while((uid==null) && it.hasNext()) {
			Object comp = it.next();
			if (!(comp instanceof TimeZone)) {
				CalendarComponent cc = (CalendarComponent) comp;
				try {
					uid = cc.getProperty(Property.UID).getValue();
				} catch (NullPointerException  e) {
					// TODO log missing uid
				}				
			} 					
		}
		
		if (uid == null) {
			uid = random.nextLong() + "-" + random.nextLong() ; 
		}
		
		PutMethod putMethod = createPutMethodForNewResource(uid + ".ics", c);
		try {
			httpClient.executeMethod(getHostConfiguration(), putMethod);
			//fixed for nullpointerexception
			String etag = ( putMethod.getResponseHeader("ETag") != null) ? putMethod.getResponseHeader("ETag").getValue() :  ""; 
			CalDAVResource calDAVResource = new CalDAVResource(c,
					etag, getHref((putMethod.getPath())));
			
			cache.putResource(calDAVResource);
			
		} catch (Exception e) {
			throw new CalDAV4JException("Trouble executing PUT", e);
		}
		int statusCode = putMethod.getStatusCode();

		switch (statusCode) {
		case CaldavStatus.SC_CREATED:			
			break;
		case CaldavStatus.SC_NO_CONTENT:
			break;
		case CaldavStatus.SC_PRECONDITION_FAILED:
			break;
		default:
			//must be some other problem, throw an exception
			try {
				throw new CalDAV4JException("Unexpected status code: "
						+ statusCode + "\n"
						+ putMethod.getResponseBodyAsString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new CalDAV4JException("Unexpected status code: "
						+ statusCode + "\n"
						+ "error in getResponseBodyAsString()");
			}
		} 
		
	


		
	}

	/**
	 * Updates the resource containing the VEvent with the same UID as the given 
	 * VEvent with the given VEvent
	 * 
	 *  TODO: Deal with SEQUENCE
	 *  TODO: Handle timezone!!! Right now ignoring the param...
	 *
	 * @param httpClient the httpClient which will make the request
	 * @param vevent the vevent to update
	 * @param timezone The VTimeZone of the VEvent if it references one, 
	 *                 otherwise null
	 * @throws CalDAV4JException
	 */
	public void updateMasterEvent(HttpClient httpClient, VEvent vevent, VTimeZone timezone)
	throws CalDAV4JException{
		String uid = getUIDValue(vevent);
		CalDAVResource resource = getCalDAVResourceForEventUID(httpClient, uid);
		Calendar calendar = resource.getCalendar();

		//let's find the master event first!
		VEvent originalVEvent = getMasterEvent(calendar, uid);

		calendar.getComponents().remove(originalVEvent);
		calendar.getComponents().add(vevent);

		put(httpClient, calendar,
				stripHost(resource.getResourceMetadata().getHref()),
				resource.getResourceMetadata().getETag());
	}
	/**
	 * Creates a ticket for the specified resource and returns the ticket id.
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param relativePath the path, relative to the collection path for 
	 *                     which to grant the ticket on
	 * @param visits
	 * @param timeout
	 * @param read
	 * @param write
	 * @return The id of the created ticket
	 * @throws CalDAV4JException
	 *             Is thrown if the execution of the MkTicketMethod fails
	 */
	public String createTicket(HttpClient httpClient, String relativePath,
			Integer visits, Integer timeout, boolean read, boolean write)
	throws CalDAV4JException {
		TicketRequest ticketRequest = new TicketRequest();
		ticketRequest.setVisits(visits);
		ticketRequest.setTimeout(timeout);
		ticketRequest.setRead(read);
		ticketRequest.setWrite(write);

		// Make the ticket
		MkTicketMethod mkTicketMethod = methodFactory.createMkTicketMethod();
		mkTicketMethod.setPath(getAbsolutePath(relativePath));
		mkTicketMethod.setTicketRequest(ticketRequest);
		try {
			httpClient.executeMethod(hostConfiguration, mkTicketMethod);
			int statusCode = mkTicketMethod.getStatusCode();
			if (statusCode != CaldavStatus.SC_OK) {
				throw new CalDAV4JException("Create Ticket Failed with Status: "
						+ statusCode + " and body: \n"
						+ mkTicketMethod.getResponseBodyAsString());
			}
		} catch (Exception e) {
			throw new CalDAV4JException("Trouble executing MKTicket", e);
		}

		TicketResponse ticketResponse = null;

		try {
			ticketResponse = mkTicketMethod.getResponseBodyAsTicketResponse();
		} catch (Exception e) {
			throw new CalDAV4JException("Trouble handling MkTicket Response", e);
		}

		return ticketResponse.getID();

	}

	/**
	 * Deletes the specified ticket on the specified resource.
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param relativePath the path, relative to the collection path for
	 *                     which to revoke the ticket 
	 * @param ticketID the ticketID which to revoke
	 * @throws CalDAV4JException
	 *             Is thrown if the execution of the DelTicketMethod fails
	 */
	public void deleteTicket(HttpClient httpClient, String relativePath, String ticketId)
	throws CalDAV4JException {
		DelTicketMethod delTicketMethod = methodFactory.createDelTicketMethod();
		delTicketMethod.setPath(getAbsolutePath(relativePath));
		delTicketMethod.setTicket(ticketId);
		try {
			httpClient.executeMethod(hostConfiguration, delTicketMethod);
			int statusCode = delTicketMethod.getStatusCode();
			if (statusCode != CaldavStatus.SC_NO_CONTENT) {
				throw new CalDAV4JException(
						"Delete Ticket Failed with Status: " + statusCode
						+ " and body: \n"
						+ delTicketMethod.getResponseBodyAsString());
			}
		} catch (Exception e) {
			throw new CalDAV4JException("Trouble executing DelTicket", e);
		}

	}

	/**
	 * Returns all the ticket ID's from all tickets the requesting user has
	 * permision to view on a resource.
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param relativePath the path, relative to the collection path for which
	 *                     to get the tickets
	 * @return
	 * @throws CalDAV4JException
	 * @throws HttpException
	 * @throws IOException
	 */
	public List<String> getTicketsIDs(HttpClient httpClient, String relativePath)
	throws CalDAV4JException, HttpException, IOException {

		PropProperty propFind = PropertyFactory.createProperty(PropertyFactory.PROPFIND);
		PropProperty properties = PropertyFactory.createProperty(PropertyFactory.PROP);
		propFind.addChild(properties);

		PropProperty ticketDiscoveryProperty = new PropProperty(CalDAVConstants.NS_XYTHOS,
			CalDAVConstants.NS_QUAL_TICKET, CalDAVConstants.ELEM_TICKETDISCOVERY);
		PropProperty ownerProperty = new PropProperty(CalDAVConstants.NS_DAV, CalDAVConstants.NS_QUAL_DAV,
		"owner");

		properties.addChild(ticketDiscoveryProperty);
		properties.addChild(ownerProperty);

		PropFindMethod propFindMethod = methodFactory.createPropFindMethod();

		propFindMethod.setDepth(0);
		propFindMethod.setType(0);
		propFindMethod.setPath(getAbsolutePath(relativePath));
		propFindMethod.setPropFindRequest(propFind);
		httpClient.executeMethod(hostConfiguration, propFindMethod);

		int statusCode = propFindMethod.getStatusCode();

		if (statusCode != CaldavStatus.SC_MULTI_STATUS) {
			throw new CalDAV4JException("PropFind Failed with Status: "
					+ statusCode + " and body: \n"
					+ propFindMethod.getResponseBodyAsString());
		}
		String href = getHref(getAbsolutePath(relativePath));
		Enumeration responses = propFindMethod.getResponseProperties(href);

		List<String> ticketIDList = new ArrayList<String>();
		while (responses.hasMoreElements()) {
			org.apache.webdav.lib.Property item = (org.apache.webdav.lib.Property) responses
			.nextElement();
			if (item.getLocalName()
					.equals(CalDAVConstants.ELEM_TICKETDISCOVERY)) {
				TicketDiscoveryProperty ticketDiscoveryProp = (TicketDiscoveryProperty) item;
				ticketIDList.addAll(ticketDiscoveryProp.getTicketIDs());
			}
		}
		return ticketIDList;
	}


	/**
	 * Returns the path to the resource that contains the VEVENT with the
	 * specified uid
	 * 
	 * @param uid
	 */
	protected String getPathToResourceForEventId(HttpClient httpClient, String uid) throws CalDAV4JException{
		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");

		query.addProperty(CalDAVConstants.PROP_GETETAG);

		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(Component.VEVENT);

		PropFilter propFilter = new PropFilter("C");
		propFilter.setName(Property.UID);
		propFilter.setTextMatch(new TextMatch("C", false,null,null, uid));
		vEventCompFilter.addPropFilter(propFilter);

		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);

		CalDAVReportMethod reportMethod = methodFactory
		.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		try {
			httpClient.executeMethod(hostConfiguration, reportMethod);
		} catch (Exception he) {
			throw new CalDAV4JException("Problem executing method", he);
		}

		Enumeration<CalDAVResponse> e = reportMethod.getResponses();
		if (!e.hasMoreElements()) {
			throw new ResourceNotFoundException(
					ResourceNotFoundException.IdentifierType.UID, uid);
		}

		return stripHost(e.nextElement().getHref());
	}


	/**
	 * get a calendar by UID
	 * it tries
	 *  - first by a REPORT
	 *  - then by GET /path
	 * @param httpClient
	 * @param uid
	 * @return
	 * @throws CalDAV4JException
	 * @throws ResourceNotFoundException
	 */
	protected CalDAVResource getCalDAVResourceForEventUID(
			HttpClient httpClient, String uid) throws CalDAV4JException, ResourceNotFoundException {

		//first check the cache!
		String href = cache.getHrefForEventUID(uid);
		CalDAVResource calDAVResource = null;

		if (href != null) {
			calDAVResource = getCalDAVResource(httpClient, stripHost(href));

			if (calDAVResource != null) {
				return calDAVResource;
			}
		}

		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");
		query.setCalendarDataProp(new CalendarData("C"));
		query.addProperty(CalDAVConstants.PROP_GETETAG);

		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(Component.VEVENT);

		PropFilter propFilter = new PropFilter("C");
		propFilter.setName(Property.UID);
		propFilter.setTextMatch(new TextMatch("C", null, null, null, uid)); // rpolli s/false/null/
		vEventCompFilter.addPropFilter(propFilter);

		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);

		CalDAVReportMethod reportMethod = methodFactory
		.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		try {
			httpClient.executeMethod(hostConfiguration, reportMethod);
		} catch (Exception he) {
			throw new CalDAV4JException("Problem executing method", he);
		}

		Enumeration<CalDAVResponse> e = reportMethod.getResponses();
		if (!e.hasMoreElements()) {
			throw new ResourceNotFoundException(
					ResourceNotFoundException.IdentifierType.UID, uid);
		}

		calDAVResource = new CalDAVResource(e.nextElement());
		cache.putResource(calDAVResource);
		return calDAVResource;
	}

	/**
	 * Gets the resource at the given path. Will check the cache first, and compare that to the
	 * latest etag obtained using a HEAD request.
	 * @param httpClient
	 * @param path
	 * @return
	 * @throws CalDAV4JException
	 */
	protected CalDAVResource getCalDAVResource(HttpClient httpClient,
			String path) throws CalDAV4JException {
		String currentEtag = getETag(httpClient, path);
		return getCalDAVResource(httpClient, path, currentEtag);
	}

	/**
	 * Gets the resource for the given href. Will check the cache first, and if a cached
	 * version exists that has the etag provided it will be returned. Otherwise, it goes
	 * to the server for the resource.
	 * 
	 * @param httpClient
	 * @param path
	 * @param currentEtag
	 * @return
	 * @throws CalDAV4JException
	 */
	protected CalDAVResource getCalDAVResource(HttpClient httpClient,
			String path, String currentEtag) throws CalDAV4JException {

		//first try getting from the cache
		CalDAVResource calDAVResource = cache.getResource(getHref(path));

		//ok, so we got the resource...but has it been changed recently?
		if (calDAVResource != null){
			String cachedEtag = calDAVResource.getResourceMetadata().getETag();
			if (cachedEtag.equals(currentEtag)){
				return calDAVResource;
			}
		}

		//either the etag was old, or it wasn't in the cache so let's get it
		//from the server       
		return getCalDAVResourceFromServer(httpClient, path);

	}

	/**
	 * Gets a CalDAVResource from the server - in other words DOES NOT check the cache.
	 * Adds the new resource to the cache, replacing any preexisting version.
	 * 
	 * The calendar is Thread-locally build by getResponseBodyAsCalendar()
	 * 
	 * @param httpClient
	 * @param path
	 * @return
	 * @throws CalDAV4JException
	 */
	protected CalDAVResource getCalDAVResourceFromServer(HttpClient httpClient,
			String path) throws CalDAV4JException {
		CalDAVResource calDAVResource = null;
		GetMethod getMethod = getMethodFactory().createGetMethod();
		getMethod.setPath(path);
		try {
			httpClient.executeMethod(hostConfiguration, getMethod);
			if (getMethod.getStatusCode() != CaldavStatus.SC_OK){
				throw new CalDAV4JException(
						"Unexpected Status returned from Server: "
						+ getMethod.getStatusCode());
			}
		} catch (Exception e){
			throw new CalDAV4JException("Problem executing get method",e);
		}

		String href = getHref(path);
		String etag = getMethod.getResponseHeader("ETag").getValue();
		Calendar calendar = null;
		try {
			calendar = getMethod.getResponseBodyAsCalendar();
		} catch (Exception e){
			throw new CalDAV4JException("Malformed calendar resource returned.", e);
		}

		calDAVResource = new CalDAVResource();
		calDAVResource.setCalendar(calendar);
		calDAVResource.getResourceMetadata().setETag(etag);
		calDAVResource.getResourceMetadata().setHref(href);

		cache.putResource(calDAVResource);
		return calDAVResource;

	}
	protected void delete(HttpClient httpClient, String path)
	throws CalDAV4JException {
		DeleteMethod deleteMethod = new DeleteMethod(path);
		try {
			httpClient.executeMethod(hostConfiguration, deleteMethod);
			if (deleteMethod.getStatusCode() != CaldavStatus.SC_NO_CONTENT){
				throw new CalDAV4JException(
						"Unexpected Status returned from Server: "
						+ deleteMethod.getStatusCode());
			}
		} catch (Exception e){
			throw new CalDAV4JException("Problem executing delete method",e);
		}

		cache.removeResource((getHref(path)));
	}

	protected String getAbsolutePath(String relativePath){
		return   (getCalendarCollectionRoot() + "/" + relativePath).replaceAll("/+", "/");
	}



	protected String getETag(HttpClient httpClient, String path) throws CalDAV4JException{
		HeadMethod headMethod = new HeadMethod(path);

		try {
			httpClient.executeMethod(hostConfiguration, headMethod);
			int statusCode = headMethod.getStatusCode();

			if (statusCode == CaldavStatus.SC_NOT_FOUND) {
				throw new ResourceNotFoundException(
						ResourceNotFoundException.IdentifierType.PATH, path);
			}

			if (statusCode != CaldavStatus.SC_OK){
				throw new CalDAV4JException(
						"Unexpected Status returned from Server: "
						+ headMethod.getStatusCode());
			}
		} catch (IOException e){
			throw new CalDAV4JException("Problem executing get method",e);
		}

		Header h = headMethod.getResponseHeader("ETag");
		String etag = null;
		if (h != null) {
			etag = h.getValue();
		}
		return etag;
	}

	/**
	 * Returns all Calendars (VCALENDAR/VEVENTS) which contain events with DTSTAMP between beginDate - endDate
	 * TODO Note that recurring events are NOT expanded. 
	 * 
	 * @param httpClient the httpClient which will make the request
	 * @param beginDate the beginning of the date range. Must be a UTC date
	 * @param endDate the end of the date range. Must be a UTC date.
	 * @return a List of Calendars
	 * @throws CalDAV4JException if there was a problem
	 */
	public List<Calendar> getEventResourcesByTimestamp(HttpClient httpClient,
			Date beginDate, Date endDate)
			throws CalDAV4JException {
		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");

		query.addProperty(CalDAVConstants.PROP_GETETAG);
		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(Component.VEVENT);
		//       vEventCompFilter.setTimeRange(new TimeRange("C", beginDate, endDate));

		/* TODO check the support from the caldav server. bedework may not work..
		 * 
		 */
		PropFilter pFilter = new PropFilter("C");
		pFilter.setName("DTSTAMP");
		pFilter.setTimeRange(beginDate, endDate);

		vEventCompFilter.addPropFilter(pFilter);
		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);


		return getComponentByQuery(httpClient,Component.VEVENT, query); 

	}
	/**
	 * Returns all Calendars (VCALENDAR/VEVENTS) which contain events with DTSTAMP between beginDate - endDate
	 * TODO Note that recurring events are NOT expanded. 
	 * TODO we can parametrize too the Component.VEVENT field to get a more flexible method
	 * TODO This method doesn't use cache
	 *   the search is the following...
<C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav">
       <D:prop xmlns:D="DAV:">
           <D:getetag/>
           <C:calendar-data>
               <C:comp name="VCALENDAR">
                   <C:comp name="VEVENT">
                       <C:prop name="UID"/>
                   </C:comp>
               </C:comp>
           </C:calendar-data>
       </D:prop>
       <C:filter>
           <C:comp-filter name="VCALENDAR">
               <C:comp-filter name="VEVENT">
               		<C:time-range end="20081210T000000Z" start="20080607T000000Z"/> XXX
                   <C:prop-filter name="DTSTAMP">
                       <C:time-range end="20071210T000000Z" start="20070607T000000Z"/>
                   </C:prop-filter>
               </C:comp-filter>
           </C:comp-filter>
       </C:filter>
   </C:calendar-query>

	 * @param httpClient the httpClient which will make the request
	 * @param propertyName the iCalendar property name ex. Property.UID @see Property
	 * @param beginDate the beginning of the date range. Must be a UTC date
	 * @param endDate the end of the date range. Must be a UTC date.
	 * @return a List of Property values (eg. a List of VEVENT UIDs
	 * @throws CalDAV4JException if there was a problem
	 */
	public List<String> getEventPropertyByTimestamp(HttpClient httpClient, String propertyName,
			Date beginDate, Date endDate)
			throws CalDAV4JException {
		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");

		query.addProperty(CalDAVConstants.PROP_GETETAG);

		// create the query fields 
		CalendarData calendarData = new CalendarData("C");

		Comp vCalendarComp = new Comp("C");
		vCalendarComp.setName(Calendar.VCALENDAR);

		Comp vEventComp = new Comp("C");
		vEventComp.setName(Component.VEVENT);
		vEventComp.addProp(new CalDAVProp("C", "name", propertyName, false, false)); // @see modification to CalDAVProp  

		List <Comp> comps = new ArrayList<Comp> ();
		comps.add(vEventComp);
		vCalendarComp.setComps(comps);
		calendarData.setComp(vCalendarComp);
		query.setCalendarDataProp(calendarData);

		// search for events matching...
		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(Component.VEVENT);

		// TODO check the support from the caldav server. bedework is ok
		// XXX if endDate is undefined, check into ine year
		PropFilter pFilter = new PropFilter("C");
		pFilter.setName("DTSTAMP");
		if (endDate == null) {
			endDate = new DateTime(beginDate.getTime()+86400*364);
			((DateTime)endDate).setUtc(true);
		}
		pFilter.setTimeRange(beginDate, endDate);

		vEventCompFilter.addPropFilter(pFilter);
		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);

		CalDAVReportMethod reportMethod = methodFactory.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		return getComponentPropertyByQuery(httpClient, Component.VEVENT, propertyName, query);
	}

	/**
	 * Returns all Calendars (VCALENDAR/VEVENTS) which contain events with DTSTAMP between beginDate - endDate
	 * TODO Note that recurring events are NOT expanded. 
	 * TODO we can parametrize too the Component.VEVENT field to get a more flexible method
	 * TODO This method doesn't use cache
	 *   the search is the following...
<C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav">
       <D:prop xmlns:D="DAV:">
           <D:getetag/>
           <C:calendar-data>
               <C:comp name="VCALENDAR">
                   <C:comp name="VEVENT">
                       <C:prop name="UID"/>
                   </C:comp>
               </C:comp>
           </C:calendar-data>
       </D:prop>
       <C:filter>
           <C:comp-filter name="VCALENDAR">
               <C:comp-filter name="VEVENT">
                   <C:prop-filter name="DTSTAMP">
                       <C:time-range end="20071210T000000Z" start="20070607T000000Z"/>
                   </C:prop-filter>
               </C:comp-filter>
           </C:comp-filter>
       </C:filter>
   </C:calendar-query>

	 * @param httpClient the httpClient which will make the request
	 * @param componentName the iCalendar component name ex. Component.VEVENT @see Property
	 * @param propertyName the iCalendar property name ex. Property.UID @see Property
	 * @param propertyFilter the iCalendar property key name ex. Property.DTSTAMP Property.LAST-MODIFIED @see Property
	 * @param beginDate the beginning of the date range. Must be a UTC date
	 * @param endDate the end of the date range. Must be a UTC date.
	 * @return a List of Property values (eg. a List of VEVENT UIDs
	 * @throws CalDAV4JException if there was a problem
	 * XXX check if you must cast Date to DateTime
	 */
	public List<String> getComponentPropertyByTimestamp(HttpClient httpClient, String componentName, String propertyName, String propertyFilter,
			Date beginDate, Date endDate)
			throws CalDAV4JException {
		// first create the calendar query
		CalendarQuery query = new CalendarQuery("C", "D");

		query.addProperty(CalDAVConstants.PROP_GETETAG);

		// create the query fields 
		CalendarData calendarData = new CalendarData("C");

		Comp vCalendarComp = new Comp("C");
		vCalendarComp.setName(Calendar.VCALENDAR);

		Comp vEventComp = new Comp("C");
		vEventComp.setName(componentName);
		vEventComp.addProp(new CalDAVProp("C", "name", propertyName, false, false)); // @see modification to CalDAVProp  

		List <Comp> comps = new ArrayList<Comp> ();
		comps.add(vEventComp);
		vCalendarComp.setComps(comps);
		calendarData.setComp(vCalendarComp);
		query.setCalendarDataProp(calendarData);

		// search for events matching...
		CompFilter vCalendarCompFilter = new CompFilter("C");
		vCalendarCompFilter.setName(Calendar.VCALENDAR);

		CompFilter vEventCompFilter = new CompFilter("C");
		vEventCompFilter.setName(componentName);

		// TODO check the support from the caldav server. bedework is ok
		// XXX if endDate is undefined, set it one year later
		// set the filter name Property.LAST_MODIFIED
		PropFilter pFilter = new PropFilter("C");
		pFilter.setName(propertyFilter);
		if (endDate == null) {
			endDate = new DateTime(beginDate.getTime()+86400*364);
			((DateTime)endDate).setUtc(true);
		}
		pFilter.setTimeRange(beginDate, endDate);

		vEventCompFilter.addPropFilter(pFilter);
		vCalendarCompFilter.addCompFilter(vEventCompFilter);
		query.setCompFilter(vCalendarCompFilter);

		return getComponentPropertyByQuery(httpClient, componentName, propertyName, query);
	}

	protected List <String> getComponentPropertyByQuery(HttpClient httpClient, String componentName, String propertyName, CalendarQuery query) throws CalDAV4JException 
	{
		CalDAVReportMethod reportMethod = methodFactory.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		try {
			httpClient.executeMethod(getHostConfiguration(), reportMethod);
		} catch (Exception he) {
			he.printStackTrace();
			throw new CalDAV4JException("Problem executing method", he);
		}

		List<String> propertyList = new ArrayList<String>();
		Enumeration<CalDAVResponse> e = reportMethod.getResponses();
		while (e.hasMoreElements()){
			CalDAVResponse response  = e.nextElement();
			Calendar cal =  response.getCalendar();
			if (cal.getComponent(componentName) != null) {
				propertyList.add( cal.getComponent(componentName)
						.getProperty(propertyName).getValue() );
			}
//			former code with cache
//			String etag = response.getETag();
//			CalDAVResource resource = getCalDAVResource(httpClient,
//			stripHost(response.getHref()), etag);
//			list.add(resource.getCalendar());
		}

		return propertyList;
	}

	/**
	 * implementing calendar multiget
	 * @link { http://tools.ietf.org/html/rfc4791#section-7.9 }
	 * 
	 * TODO test me
	 * @author rpolli
	 *
	 *<?xml version="1.0" encoding="utf-8" ?>
       <C:calendar-multiget xmlns:D="DAV:"
                        xmlns:C="urn:ietf:params:xml:ns:caldav">
         <D:prop>
           <D:getetag/>
           <C:calendar-data/>
         </D:prop>
         <D:href>/bernard/work/abcd1.ics</D:href>
         <D:href>/bernard/work/mtg1.ics</D:href>
       </C:calendar-multiget>
	 */
	public List<Calendar> multigetCalendarUris(HttpClient httpClient,
			List<String> calendarUris )
			throws CalDAV4JException {
		// first create the calendar query
		CalendarMultiget query = new CalendarMultiget("C", "D");
		CalendarData calendarData = new CalendarData("C");

		query.addProperty(CalDAVConstants.PROP_GETETAG);
		query.setCalendarDataProp(calendarData);

		query.setHrefs(calendarUris);


		return getComponentByMultiget(httpClient, Component.VEVENT, query);
	} 



	public List<Calendar> getComponentByQuery(HttpClient httpClient, String componentName,CalendarQuery query) throws CalDAV4JException 
	{
		CalDAVReportMethod reportMethod = methodFactory.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		try {
			httpClient.executeMethod(getHostConfiguration(), reportMethod);
		} catch (Exception he) {
			throw new CalDAV4JException("Problem executing method", he);
		}

		Enumeration<CalDAVResponse> e = reportMethod.getResponses();
		List<Calendar> list = new ArrayList<Calendar>();
		while (e.hasMoreElements()){
			CalDAVResponse response  = e.nextElement();
			String etag = response.getETag();
			CalDAVResource resource = getCalDAVResource(httpClient,
					stripHost(response.getHref()), etag);
			list.add(resource.getCalendar());
		}

		return list;
	}

	/**
	 * 
	 * @param httpClient
	 * @param componentName
	 * @param query
	 * @return a list of Calendar (?)
	 * @throws CalDAV4JException
	 */
	protected List<Calendar> getComponentByMultiget(HttpClient httpClient, String componentName,CalendarMultiget query) throws CalDAV4JException 
	{
		CalDAVReportMethod reportMethod = methodFactory.createCalDAVReportMethod();
		reportMethod.setPath(getCalendarCollectionRoot());
		reportMethod.setReportRequest(query);
		try {
			httpClient.executeMethod(getHostConfiguration(), reportMethod);
		} catch (Exception he) {
			throw new CalDAV4JException("Problem executing method", he);
		}

		Enumeration<CalDAVResponse> e = reportMethod.getResponses();
		List<Calendar> list = new ArrayList<Calendar>();

		while (e.hasMoreElements()){
			CalDAVResponse response  = e.nextElement();
			
			if (response.getStatusCode()==CaldavStatus.SC_OK){
				// TODO should use a thread-local builder ! 
				list.add(response.getCalendar());
			}
//			String etag = response.getETag();
//			try{
//			CalDAVResource resource = getCalDAVResource(httpClient,
//			stripHost(response.getHref()), etag);

//			list.add(resource.getCalendar());
//			}catch(Exception e1){}

		}

		return list;
	}
} //end of class
