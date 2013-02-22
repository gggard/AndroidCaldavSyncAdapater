package org.gege.caldavsyncadapter.caldav;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.gege.caldavsyncadapter.BuildConfig;
import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;
import org.gege.caldavsyncadapter.caldav.xml.CalendarHomeHandler;
import org.gege.caldavsyncadapter.caldav.xml.CalendarsHandler;
import org.gege.caldavsyncadapter.caldav.xml.ServerInfoHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

public class CaldavFacade {

	private static final String TAG = "CaldavFacade";

	private static HttpClient httpClient;

	private boolean trustAll = true;

	private URL url;

	private static HttpHost targetHost;

	protected HttpClient getHttpClient() {

		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE,
				new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		// ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
		// schemeRegistry);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", new PlainSocketFactory(), 80));
		registry.register(new Scheme("https", (trustAll ? EasySSLSocketFactory
				.getSocketFactory() : SSLSocketFactory.getSocketFactory()), 443));
		DefaultHttpClient client = new DefaultHttpClient(
				new ThreadSafeClientConnManager(params, registry), params);

		return client;
	}

	public CaldavFacade(String mUser, String mPassword, String mURL)
			throws MalformedURLException {
		url = new URL(mURL);

		httpClient = getHttpClient();

		UsernamePasswordCredentials upc = new UsernamePasswordCredentials(
				mUser, mPassword);

		AuthScope as = new AuthScope(url.getHost(), -1);
		((AbstractHttpClient) httpClient).getCredentialsProvider()
				.setCredentials(as, upc);

		BasicHttpContext localContext = new BasicHttpContext();

		BasicScheme basicAuth = new BasicScheme();
		localContext.setAttribute("preemptive-auth", basicAuth);

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

	public enum TestConnectionResult {
		WRONG_CREDENTIAL, WRONG_URL, WRONG_SERVER_STATUS, WRONG_ANSWER, SUCCESS
	}

	private final static String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

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
	public TestConnectionResult testConnection()
			throws HttpHostConnectException, IOException, URISyntaxException,
			ParserConfigurationException, SAXException {
		Log.d(TAG, "start testConnection ");
		try {
			List<Calendar> calendars = new ArrayList<Calendar>();
			calendars = forceGetCalendarsFromUri(url.toURI());
			if (calendars.size() != 0) {
				return TestConnectionResult.SUCCESS;
			}

			URI userPrincipal = getUserPrincipal();
			List<URI> calendarSets = getCalendarHomes(userPrincipal);
			for (URI calendarSet : calendarSets) {
				List<Calendar> calendarSetCalendars = getCalendarsFromSet(calendarSet);
				calendars.addAll(calendarSetCalendars);
			}
			if (calendarSets.size() == 0) {
				return TestConnectionResult.WRONG_ANSWER;
			}
		} catch (FileNotFoundException e) {
			return TestConnectionResult.WRONG_URL;
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

	private List<Calendar> forceGetCalendarsFromUri(URI uri)
			throws AuthenticationException, FileNotFoundException {
		List<Calendar> calendars = new ArrayList<Calendar>();
		Exception exception = null;
		try {
			calendars = getCalendarsFromSet(uri);
		} catch (ClientProtocolException e) {
			exception = e;
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			exception = e;
		} catch (CaldavProtocolException e) {
			exception = e;
		}
		if (exception != null && BuildConfig.DEBUG) {
			Log.e(TAG, "Force get calendars from '" + uri.toString()
					+ "' failed " + exception.getClass().getCanonicalName()
					+ ": " + exception.getMessage());
		}
		return calendars;
	}

	private final static String PROPFIND_USER_PRINCIPAL = XML_VERSION
			+ "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:current-user-principal /><d:principal-URL /></d:prop></d:propfind>";

	private URI getUserPrincipal() throws SocketException,
			ClientProtocolException, AuthenticationException,
			FileNotFoundException, IOException, CaldavProtocolException,
			URISyntaxException {
		URI uri = this.url.toURI();
		HttpPropFind request = createPropFindRequest(uri,
				PROPFIND_USER_PRINCIPAL, 0);
		HttpResponse response = httpClient.execute(targetHost, request);
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
		HttpResponse response = httpClient.execute(targetHost, request);
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
			// "<c:calendar-description /><c:calendar-timezone /><c:calendar-free-busy-set /><ic:calendar-color /><ic:calendar-order />"
			+ "<cs:getctag /></d:prop></d:propfind>";

	private List<Calendar> getCalendarsFromSet(URI calendarSet)
			throws ClientProtocolException, IOException,
			CaldavProtocolException, AuthenticationException,
			FileNotFoundException {
		HttpPropFind request = createPropFindRequest(calendarSet,
				PROPFIND_CALENDER_LIST, 1);
		HttpResponse response = httpClient.execute(targetHost, request);
		checkStatus(response);
		CalendarsHandler calendarsHandler = new CalendarsHandler(calendarSet);
		parseXML(response, calendarsHandler);
		List<Calendar> result = calendarsHandler.calendars;
		if (BuildConfig.DEBUG) {
			Log.d(TAG,
					result.size() + " calendars found in set "
							+ calendarSet.toString());
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
	 * @return List of {@link Calendar}
	 * @throws ClientProtocolException
	 *             http protocol error
	 * @throws IOException
	 *             Connection lost
	 * @throws URISyntaxException
	 *             url in Constructor malformed
	 * @throws CaldavProtocolException
	 *             caldav protocol error
	 */
	public Iterable<Calendar> getCalendarList() throws ClientProtocolException,
			IOException, URISyntaxException, ParserConfigurationException,
			CaldavProtocolException {
		try {
			List<Calendar> calendars = new ArrayList<Calendar>();
			calendars = forceGetCalendarsFromUri(this.url.toURI());
			if (calendars.size() != 0) {
				return calendars;
			}
			URI userPrincipal = getUserPrincipal();
			List<URI> calendarSets = getCalendarHomes(userPrincipal);
			for (URI calendarSet : calendarSets) {
				List<Calendar> calendarSetCalendars = getCalendarsFromSet(calendarSet);
				calendars.addAll(calendarSetCalendars);
			}
			return calendars;
		} catch (AuthenticationException e) {
			throw new IOException(e);
		}
	}

	public Iterable<CalendarEvent> getCalendarEvents(Calendar calendar)
			throws URISyntaxException, ClientProtocolException, IOException,
			ParserConfigurationException, SAXException {

		List<CalendarEvent> calendarEventList = new ArrayList<CalendarEvent>();

		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<D:propfind xmlns:D=\"DAV:\">" + "<D:prop>" + "<D:getetag/>"
				+ "</D:prop>" + "</D:propfind>";

		HttpPropFind request = null;

		request = new HttpPropFind();
		request.setURI(calendar.getURI());
		request.setHeader("Depth", "1");
		request.setEntity(new StringEntity(requestBody));

		Log.d(TAG, "Getting eTag by PROPFIND at " + request.getURI());

		HttpResponse response = httpClient.execute(targetHost, request);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF-8"));

		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line;
		} while (line != null);

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
			CalendarEvent calendarEvent = new CalendarEvent();

			Node node = items.item(i);

			if (node.getTextContent().trim().length() == 0)
				continue; // not an event

			calendarEvent.setETag(node.getTextContent().trim());

			node = node.getParentNode(); // prop
			node = node.getParentNode(); // propstat
			node = node.getParentNode(); // response

			NodeList children = node.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node childNode = children.item(j);
				if (childNode.getLocalName().equalsIgnoreCase("href")) {
					calendarEvent.setURI(new URI(childNode.getTextContent()));
				}
			}

			calendarEventList.add(calendarEvent);

		}

		return calendarEventList;
	}

	private void parseXML(HttpResponse response, ContentHandler contentHandler)
			throws IOException, CaldavProtocolException {
		InputStream is = response.getEntity().getContent();
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
		switch (statusCode) {
		case 401:
			throw new AuthenticationException();
		case 404:
			throw new FileNotFoundException();
		case 200:
		case 207:
			return;
		default:
			throw new ClientProtocolException("StatusCode: " + statusCode);
		}
	}

	private HttpPropFind createPropFindRequest(URI uri, String data, int depth) {
		HttpPropFind request = new HttpPropFind();
		request.setURI(uri);
		request.setHeader("Depth", Integer.toString(depth));
		try {
			request.setEntity(new StringEntity(data, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 is unknown");
		}
		return request;
	}

	public static void fetchEventBody(CalendarEvent calendarEvent)
			throws ClientProtocolException, IOException {
		HttpGet request = null;

		request = new HttpGet();
		request.setURI(calendarEvent.getUri());

		HttpResponse response = httpClient.execute(targetHost, request);

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "UTF-8"));

		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line + "\n";
		} while (line != null);

		calendarEvent.setICS(body);

		Log.d(TAG, "HttpResponse GET event status=" + response.getStatusLine()
				+ " body= " + body);
	}
}
