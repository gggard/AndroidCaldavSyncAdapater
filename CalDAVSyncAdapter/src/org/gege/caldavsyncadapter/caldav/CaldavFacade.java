/**
 * Copyright (c) 2012-2013, Gerald Garcia, David Wiesner, Timo Berger
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

package org.gege.caldavsyncadapter.caldav;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.gege.caldavsyncadapter.BuildConfig;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.caldav.entities.CalendarList;
import org.gege.caldavsyncadapter.caldav.entities.DavCalendar;
import org.gege.caldavsyncadapter.caldav.entities.DavCalendar.CalendarSource;
import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;
import org.gege.caldavsyncadapter.caldav.http.HttpReport;
import org.gege.caldavsyncadapter.caldav.xml.CalendarHomeHandler;
import org.gege.caldavsyncadapter.caldav.xml.CalendarsHandler;
import org.gege.caldavsyncadapter.caldav.xml.ServerInfoHandler;
import org.gege.caldavsyncadapter.syncadapter.notifications.NotificationsHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CaldavFacade {
    private static final String TAG = "CaldavFacade";

    private final static String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private String USER_AGENT = "CalDAV Sync Adapter (Android) https://github.com/gggard/AndroidCaldavSyncAdapater";
    private String VERSION = "";

    private static HttpClient httpClient;
    private HttpContext mContext = null;
    private AuthState mLastAuthState = null;
    private AuthScope mLastAuthScope = null;

    private boolean mTrustAll = true;

    private URL url;

    private static HttpHost targetHost;

    private int lastStatusCode;
    private String lastETag;
    private String lastDav;

    private String mstrcHeaderIfMatch = "If-Match";
    private String mstrcHeaderIfNoneMatch = "If-None-Match";

    private Account mAccount = null;
    private ContentProviderClient mProvider;

    protected HttpClient getHttpClient() {

        HttpParams params = new BasicHttpParams();
        params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
        params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
        params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", new PlainSocketFactory(), 80));
        registry.register(new Scheme("https", (mTrustAll ? EasySSLSocketFactory.getSocketFactory() : SSLSocketFactory
                .getSocketFactory()), 443));
        DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);

        return client;
    }

    public CaldavFacade(String mUser, String mPassword, String mURL, String trustAll) throws MalformedURLException {
        url = new URL(mURL);

        this.mTrustAll = Boolean.valueOf(trustAll);

        httpClient = getHttpClient();
        UsernamePasswordCredentials upc = new UsernamePasswordCredentials(mUser, mPassword);

        AuthScope as = null;
        as = new AuthScope(url.getHost(), -1);
        ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(as, upc);

        mContext = new BasicHttpContext();
        CredentialsProvider credProvider = ((AbstractHttpClient) httpClient).getCredentialsProvider();
        mContext.setAttribute(ClientContext.CREDS_PROVIDER, credProvider);

        //http://dlinsin.blogspot.de/2009/08/http-basic-authentication-with-android.html
        ((AbstractHttpClient) httpClient).addRequestInterceptor(preemptiveAuth, 0);

        String proto = "http";
        int port = 80;

        if (url.getProtocol().equalsIgnoreCase("https")) {
            proto = "https";
            if (url.getPort() == -1)
                port = 443;
            else
                port = url.getPort();
        }

        if (url.getProtocol().equalsIgnoreCase("http")) {
            proto = "http";
            if (url.getPort() == -1)
                port = 80;
            else
                port = url.getPort();
        }
        targetHost = new HttpHost(url.getHost(), port, proto);
    }

    //http://dlinsin.blogspot.de/2009/08/http-basic-authentication-with-android.html
    HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
        @Override
        public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);

            if (authState.getAuthScheme() == null) {
                if (mLastAuthState != null) {
                    Log.d(TAG, "LastAuthState: restored with user " + mLastAuthState.getCredentials()
                            .getUserPrincipal()
                            .getName());
                    authState.setAuthScheme(mLastAuthState.getAuthScheme());
                    authState.setCredentials(mLastAuthState.getCredentials());
                } else {
                    Log.d(TAG, "LastAuthState: nothing to do");
                }
                if (mLastAuthScope != null) {
                    authState.setAuthScope(mLastAuthScope);
                    Log.d(TAG, "LastAuthScope: restored");
                } else {
                    Log.d(TAG, "LastAuthScope: nothing to do");
                }
            } else {
                //AuthState and AuthScope have to be saved separate because of the AuthScope within AuthState gets lost, so we save it in a separate var.
                mLastAuthState = authState;
                Log.d(TAG, "LastAuthState: new with user " + mLastAuthState.getCredentials()
                        .getUserPrincipal()
                        .getName());
                if (authState.getAuthScope() != null) {
                    mLastAuthScope = authState.getAuthScope();
                    Log.d(TAG, "LastAuthScope: new");
                }
            }
        }
    };

    public enum TestConnectionResult {
        WRONG_CREDENTIAL,
        WRONG_URL,
        WRONG_SERVER_STATUS,
        WRONG_ANSWER,
        SSL_ERROR,
        SUCCESS
    }

    /**
     * TODO: testConnection should return only an instance of
     * TestConnectionResult without throwing an exception or only throw checked
     * exceptions so you don't have to check the result of this function AND
     * handle the exceptions
     *
     * @return {@link TestConnectionResult}
     * @throws HttpHostConnectException
     * @throws IOException
     * @throws URISyntaxException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public TestConnectionResult testConnection() throws IOException, URISyntaxException, ParserConfigurationException, SAXException {
        Log.d(TAG, "start testConnection ");
        try {
            List<DavCalendar> calendars = new ArrayList<DavCalendar>();
            calendars = forceGetCalendarsFromUri(null, url.toURI());
            if (calendars.size() != 0) {
                return TestConnectionResult.SUCCESS;
            }

            URI userPrincipal = getUserPrincipal();
            List<URI> calendarSets = getCalendarHomes(userPrincipal);
            for (URI calendarSet : calendarSets) {
                List<DavCalendar> calendarSetCalendars = getCalendarsFromSet(calendarSet);
                calendars.addAll(calendarSetCalendars);
            }
            if (calendarSets.size() == 0) {
                return TestConnectionResult.WRONG_ANSWER;
            }
        } catch (FileNotFoundException e) {
            return TestConnectionResult.WRONG_URL;
        } catch (SSLException e) {
            return TestConnectionResult.SSL_ERROR;
        } catch (SocketException e) {
            return TestConnectionResult.WRONG_URL;
        } catch (AuthenticationException e) {
            return TestConnectionResult.WRONG_CREDENTIAL;
        } catch (ClientProtocolException e) {
            return TestConnectionResult.WRONG_SERVER_STATUS;
        } catch (CaldavProtocolException e) {
            return TestConnectionResult.WRONG_ANSWER;
        }
        return TestConnectionResult.SUCCESS;
    }

    /**
     * @param context May be null if no notification is needed
     * @param uri
     * @return
     * @throws AuthenticationException
     * @throws FileNotFoundException
     */
    private List<DavCalendar> forceGetCalendarsFromUri(Context context, URI uri) throws AuthenticationException, FileNotFoundException {
        List<DavCalendar> calendars = new ArrayList<DavCalendar>();
        Exception exception = null;
        try {
            calendars = getCalendarsFromSet(uri);
        } catch (ClientProtocolException e) {
            if (context != null) {
                NotificationsHelper.signalSyncErrors(context, "Caldav sync problem", e.getMessage());
                //NotificationsHelper.getCurrentSyncLog().addException(e);
            }
            exception = e;
        } catch (FileNotFoundException e) {
            if (context != null) {
                NotificationsHelper.signalSyncErrors(context, "Caldav sync problem", e.getMessage());
                //NotificationsHelper.getCurrentSyncLog().addException(e);
            }
            throw e;
        } catch (IOException e) {
            if (context != null) {
                NotificationsHelper.signalSyncErrors(context, "Caldav sync problem", e.getMessage());
                //NotificationsHelper.getCurrentSyncLog().addException(e);
            }
            exception = e;
        } catch (CaldavProtocolException e) {

            if (context != null) {
                NotificationsHelper.signalSyncErrors(context, "Caldav sync problem", e.getMessage());
                //NotificationsHelper.getCurrentSyncLog().addException(e);
            }
            exception = e;
        }
        if (exception != null && BuildConfig.DEBUG) {
            Log.e(TAG, "Force get calendars from '" + uri.toString()
                    + "' failed " + exception.getClass().getCanonicalName()
                    + ": " + exception.getMessage());
        }
        return calendars;
    }

    private final static String PROPFIND_USER_PRINCIPAL = XML_VERSION +
            "<d:propfind xmlns:d=\"DAV:\">" +
            "<d:prop>" +
            "<d:current-user-principal />" +
            "<d:principal-URL />" +
            "</d:prop>" +
            "</d:propfind>";

    private URI getUserPrincipal() throws
            AuthenticationException,
            IOException, CaldavProtocolException,
            URISyntaxException {
        URI uri = this.url.toURI();
        HttpPropFind request = createPropFindRequest(uri,
                PROPFIND_USER_PRINCIPAL, 0);
        HttpResponse response = httpClient.execute(targetHost, request, mContext);
        checkStatus(response);
        ServerInfoHandler serverInfoHandler = new ServerInfoHandler();
        parseXML(response, serverInfoHandler);
        String userPrincipal = null;
        if (serverInfoHandler.currentUserPrincipal != null) {
            userPrincipal = serverInfoHandler.currentUserPrincipal;
        } else if (serverInfoHandler.principalUrl != null) {
            userPrincipal = serverInfoHandler.principalUrl;
        } else {
            throw new CaldavProtocolException("no principal url found");
        }
        try {
            URI userPrincipalUri = new URI(userPrincipal);
            userPrincipalUri = uri.resolve(userPrincipalUri);
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                        "Found userPrincipal: " + userPrincipalUri.toString());
            }
            return userPrincipalUri;
        } catch (URISyntaxException e) {
            throw new CaldavProtocolException("principal url '" + userPrincipal
                    + "' malformed");
        }
    }

    private final static String PROPFIND_CALENDAR_HOME_SET = XML_VERSION
            + "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:prop><c:calendar-home-set/></d:prop></d:propfind>";

    private List<URI> getCalendarHomes(URI userPrincipal)
            throws ClientProtocolException, IOException,
            AuthenticationException, FileNotFoundException,
            CaldavProtocolException {
        HttpPropFind request = createPropFindRequest(userPrincipal,
                PROPFIND_CALENDAR_HOME_SET, 0);
        HttpResponse response = httpClient.execute(targetHost, request, mContext);
        checkStatus(response);
        CalendarHomeHandler calendarHomeHandler = new CalendarHomeHandler(
                userPrincipal);
        parseXML(response, calendarHomeHandler);
        List<URI> result = calendarHomeHandler.calendarHomeSet;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, result.size() + " calendar-home-set found in "
                    + userPrincipal.toString());
        }
        return result;
    }

    private final static String PROPFIND_CALENDER_LIST = XML_VERSION
            + "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\" xmlns:cs=\"http://calendarserver.org/ns/\" xmlns:ic=\"http://apple.com/ns/ical/\">"
            + "<d:prop><d:displayname /><d:resourcetype />"
            // +
            // "<d:supported-method-set /><d:supported-report-set /><c:supported-calendar-component-set />"
            // +
            // "<c:calendar-description /><c:calendar-timezone /><c:calendar-free-busy-set />
            + "<ic:calendar-color />"
            //<ic:calendar-order />"
            + "<cs:getctag /></d:prop></d:propfind>";


    private List<DavCalendar> getCalendarsFromSet(URI calendarSet)
            throws ClientProtocolException, IOException,
            CaldavProtocolException, AuthenticationException,
            FileNotFoundException {
        HttpPropFind request = createPropFindRequest(calendarSet, PROPFIND_CALENDER_LIST, 1);
        HttpResponse response = httpClient.execute(targetHost, request, mContext);
        checkStatus(response);
        CalendarsHandler calendarsHandler = new CalendarsHandler(calendarSet);
        parseXML(response, calendarsHandler);
        List<DavCalendar> result = calendarsHandler.calendars;
        if (BuildConfig.DEBUG) {
            Log.i(TAG,
                    result.size() + " calendars found in set "
                            + calendarSet.toString()
            );
        }
        return result;
    }

    /**
     * Discover CalDAV Calendars Mentioned in
     * http://tools.ietf.org/html/draft-daboo-srv-caldav-10#section-6 and
     * http://code.google.com/p/sabredav/wiki/BuildingACalDAVClient#Discovery
     * <ol>
     * <li>PROPFIND calendar-home-set on url
     * <li>PROPFIND DAV:current-user-principal or principal-URL on url
     * <li>PROPFIND calendar-home-set on current-user-principal or principal-URL
     * <li>PROPFIND displayname, resourcetype, getctag on CalendarHomeSets
     * </ol>
     *
     * @param context
     * @return List of {@link DavCalendar}
     * @throws ClientProtocolException http protocol error
     * @throws IOException             Connection lost
     * @throws URISyntaxException      url in Constructor malformed
     * @throws CaldavProtocolException caldav protocol error
     */
    //public Iterable<Calendar> getCalendarList(Context context) throws ClientProtocolException,
    public CalendarList getCalendarList(Context context) throws ClientProtocolException,
            IOException, URISyntaxException, ParserConfigurationException,
            CaldavProtocolException {
        try {
            CalendarList Result = new CalendarList(this.mAccount, this.mProvider, CalendarSource.CalDAV, this.url
                    .toString());
            List<DavCalendar> calendars = new ArrayList<DavCalendar>();

            calendars = forceGetCalendarsFromUri(context, this.url.toURI());

            if (calendars.size() == 0) {
                // no calendars found, try the home-set
                URI userPrincipal = getUserPrincipal();
                List<URI> calendarSets = getCalendarHomes(userPrincipal);
                for (URI calendarSet : calendarSets) {
                    List<DavCalendar> calendarSetCalendars = getCalendarsFromSet(calendarSet);
                    calendars.addAll(calendarSetCalendars);
                }
            }
            for (DavCalendar cal : calendars) {
                Result.addCalendar(cal);
            }

            //return calendars;
            return Result;
        } catch (AuthenticationException e) {
            throw new IOException(e);
        }
    }

    //public Iterable<CalendarEvent> getCalendarEvents(DavCalendar calendar)
    public ArrayList<CalendarEvent> getCalendarEvents(DavCalendar calendar)
            throws URISyntaxException, ClientProtocolException, IOException,
            ParserConfigurationException, SAXException {

        ArrayList<CalendarEvent> calendarEventList = new ArrayList<CalendarEvent>();

        String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<D:propfind xmlns:D=\"DAV:\">" + "<D:prop>" + "<D:getetag/>"
                + "</D:prop>" + "</D:propfind>";

        HttpPropFind request = null;

        String EventUri;

		/*request = new HttpPropFind();
        request.setURI(calendar.getURI());
		request.setHeader("Host", targetHost.getHostName());
		request.setHeader("Depth", "1");
		request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");
		
		try {
			request.setEntity(new StringEntity(requestBody, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is unknown");
		}*/
        request = this.createPropFindRequest(calendar.getURI(), requestBody, 1);

        Log.d(TAG, "Getting eTag by PROPFIND at " + request.getURI());

        HttpResponse response = httpClient.execute(targetHost, request, mContext);
        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

        Log.d(TAG, "HttpResponse status=" + response.getStatusLine()
                + " body= " + body);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document dom = builder.parse(new InputSource(new ByteArrayInputStream(
                body.getBytes("utf-8"))));
        Element root = dom.getDocumentElement();
        NodeList items = root.getElementsByTagNameNS("*", "getetag");

        for (int i = 0; i < items.getLength(); i++) {
            CalendarEvent calendarEvent = new CalendarEvent(this.mAccount, this.mProvider);

            Node node = items.item(i);

            if (node.getTextContent().trim().length() == 0)
                continue; // not an event

            calendarEvent.setETag(node.getTextContent().trim());
            //calendarEvent.calendarURL = this.url;
            calendarEvent.calendarURL = calendar.getURI().toURL();

            node = node.getParentNode(); // prop
            node = node.getParentNode(); // propstat
            node = node.getParentNode(); // response

            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node childNode = children.item(j);
                if ((childNode.getLocalName() != null) && (childNode.getLocalName()
                        .equalsIgnoreCase("href"))) {
                    EventUri = childNode.getTextContent().trim();
                    //HINT: bugfix for zimbra calendar: replace("@", "%40")
                    EventUri = EventUri.replace("@", "%40");
                    calendarEvent.setUri(new URI(EventUri));
                }
            }

            calendarEventList.add(calendarEvent);

        }

        return calendarEventList;
    }

    private void parseXML(HttpResponse response, ContentHandler contentHandler)
            throws IOException, CaldavProtocolException {
        InputStream is = response.getEntity().getContent();
        /*BufferedReader bReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String Content = "";
		String Line = bReader.readLine();

		while (Line != null) {
			Content += Line;
			Line = bReader.readLine();
		}*/

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(is));
        } catch (ParserConfigurationException e) {
            throw new AssertionError("ParserConfigurationException "
                    + e.getMessage());
        } catch (IllegalStateException e) {
            throw new CaldavProtocolException(e.getMessage());
        } catch (SAXException e) {
            throw new CaldavProtocolException(e.getMessage());
        }
    }

    private void checkStatus(HttpResponse response)
            throws AuthenticationException, FileNotFoundException,
            ClientProtocolException {
        final int statusCode = response.getStatusLine().getStatusCode();
        lastStatusCode = statusCode;
        if (response.containsHeader("ETag"))
            lastETag = response.getFirstHeader("ETag").getValue();
        else
            lastETag = "";
        if (response.containsHeader("DAV"))
            lastDav = response.getFirstHeader("DAV").getValue();
        else
            lastDav = "";

        switch (statusCode) {
            case 401:
                throw new AuthenticationException();
            case 404:
                throw new FileNotFoundException();
            case 409: //Conflict
            case 412:
            case 200:
            case 201:
            case 204:
            case 207:
                return;
            default:
                throw new ClientProtocolException("StatusCode: " + statusCode);
        }
    }

    private HttpPropFind createPropFindRequest(URI uri, String data, int depth) {
        HttpPropFind request = new HttpPropFind();

        request.setURI(uri);
        //request.setHeader("Host", targetHost.getHostName());
        request.setHeader("Host", targetHost.getHostName() + ":" + String.valueOf(targetHost.getPort()));
        request.setHeader("Depth", Integer.toString(depth));
        request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");
        try {
            request.setEntity(new StringEntity(data, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        return request;
    }

    private HttpDelete createDeleteRequest(URI uri) {
        HttpDelete request = new HttpDelete();
        request.setURI(uri);
        //request.setHeader("Host", targetHost.getHostName());
        request.setHeader("Host", targetHost.getHostName() + ":" + String.valueOf(targetHost.getPort()));
        request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");
        return request;
    }

    private HttpPut createPutRequest(URI uri, String data, int depth) {
        HttpPut request = new HttpPut();
        request.setURI(uri);
        //request.setHeader("Host", targetHost.getHostName());
        request.setHeader("Host", targetHost.getHostName() + ":" + String.valueOf(targetHost.getPort()));
        //request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");
        request.setHeader("Content-Type", "text/calendar; charset=UTF-8");
        try {
            request.setEntity(new StringEntity(data, "UTF-8"));
            //request.setEntity(new StringEntity(data));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        return request;
    }

    private static HttpReport createReportRequest(URI uri, String data, int depth) {
        HttpReport request = new HttpReport();
        request.setURI(uri);
        //request.setHeader("Host", targetHost.getHostName());
        request.setHeader("Host", targetHost.getHostName() + ":" + String.valueOf(targetHost.getPort()));
        request.setHeader("Depth", Integer.toString(depth));
        request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");
        //request.setHeader("Content-Type", "text/xml;charset=\"UTF-8\"");
        try {
            request.setEntity(new StringEntity(data));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        return request;
    }

    public static void fetchEvent_old(CalendarEvent calendarEvent)
            throws ClientProtocolException, IOException {
        HttpGet request = null;

        request = new HttpGet();
        request.setURI(calendarEvent.getUri());
        request.setHeader("Host", targetHost.getHostName());
        request.setHeader("Content-Type", "application/xml;charset=\"UTF-8\"");

        HttpResponse response = httpClient.execute(targetHost, request);
        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

        calendarEvent.setICSasString(body);

        Log.d(TAG, "HttpResponse GET event status=" + response.getStatusLine()
                + " body= " + body);
    }

    public static boolean getEvent(CalendarEvent calendarEvent) throws ClientProtocolException, IOException {
        boolean Result = false;
        HttpReport request = null;

        //HINT: bugfix for google calendar: replace("@", "%40")
        String data = XML_VERSION +
                "<C:calendar-multiget xmlns:D=\"DAV:\" xmlns:C=\"urn:ietf:params:xml:ns:caldav\">" +
                "<D:prop>" +
                "<D:getetag />" +
                "<C:calendar-data />" +
                "</D:prop>" +
                "<D:href>" + calendarEvent.getUri().getRawPath().replace("@", "%40") + "</D:href>" +
                "</C:calendar-multiget>";

        URI calendarURI = null;
        try {
            calendarURI = calendarEvent.calendarURL.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //request = createReportRequest(calendarEvent.getUri(), data, 1);
        request = createReportRequest(calendarURI, data, 1);

        HttpResponse response = httpClient.execute(targetHost, request);
        String body = EntityUtils.toString(response.getEntity(), "UTF-8");

        if (calendarEvent.setICSasMultiStatus(body))
            Result = true;

        return Result;
    }


    /**
     * sends a update event request to the server
     *
     * @param uri  the full URI of the event on server side. example: http://caldav.example.com/principal/user/calendar/e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter.ics
     * @param data the full ical-data for the event
     * @param ETag the ETAG of this event is send within the "If-Match" Parameter to tell the server only to update this version
     * @return
     */
    public boolean updateEvent(URI uri, String data, String ETag) {
        boolean Result = false;

        try {
            HttpPut request = createPutRequest(uri, data, 1);
            request.addHeader(mstrcHeaderIfMatch, ETag);
            HttpResponse response = httpClient.execute(targetHost, request, mContext);
            checkStatus(response);
            if ((lastStatusCode == 200) || (lastStatusCode == 201) || (lastStatusCode == 204)) {
                Result = true;
            } else if (lastStatusCode == 412) {
                //Precondition failed
                Result = false;
            } else if (lastStatusCode == 409) {
                //Conflict
                Result = false;
            } else {
                Log.w(TAG, "Unkown StatusCode during creation of an event");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return Result;
    }

    /**
     * sends a create event request to server
     *
     * @param uri  the full URI of the new event on server side. example: http://caldav.example.com/principal/user/calendar/e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter.ics
     * @param data the full ical-data for the new event
     * @return success of this function
     */
    public boolean createEvent(URI uri, String data) {
        boolean Result = false;

        try {
            HttpPut request = createPutRequest(uri, data, 1);
            request.addHeader(mstrcHeaderIfNoneMatch, "*");
            HttpResponse response = httpClient.execute(targetHost, request, mContext);
            checkStatus(response);
            if (lastStatusCode == 201) {
                Result = true;
            } else {
                Log.w(TAG, "Unkown StatusCode during creation of an event");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        return Result;
    }

    /**
     * sends a delete event request to the server
     *
     * @param calendarEventUri the full URI of the event on server side. example: http://caldav.example.com/principal/user/calendar/e6be67c6-eff0-44f8-a1a0-6c2cb1029944-caldavsyncadapter.ics
     * @param ETag             the ETAG of this event is send within the "If-Match" Parameter to tell the server only to delete this version
     * @return success of this function
     */
    public boolean deleteEvent(URI calendarEventUri, String ETag) {
        boolean Result = false;

        try {
            HttpDelete request = createDeleteRequest(calendarEventUri);
            request.addHeader(mstrcHeaderIfMatch, ETag);
            HttpResponse response = httpClient.execute(targetHost, request, mContext);
            checkStatus(response);
            if ((lastStatusCode == 204) || (lastStatusCode == 200)) {
                Result = true;
            } else {
                Log.w(TAG, "Unkown StatusCode during deletion of an event");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (lastStatusCode == 404) {
                //the event has already been deleted on server side. no action needed
                Result = true;
            } else {
                e.printStackTrace();
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        return Result;
    }

    /**
     * returns the ETAG send by the last server response.
     *
     * @return the ETAG
     */
    public String getLastETag() {
        return lastETag;
    }

    /**
     * returns the DAV-Options send by the last server response.
     *
     * @return the DAV-Options
     */
    public String getLastDav() {
        return lastDav;
    }

    public void setVersion(String version) {
        VERSION = version;
        ((AbstractHttpClient) httpClient).getParams()
                .setParameter(CoreProtocolPNames.USER_AGENT, this.USER_AGENT + " Version:" + VERSION);
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public void setProvider(ContentProviderClient provider) {
        this.mProvider = provider;
    }
}
