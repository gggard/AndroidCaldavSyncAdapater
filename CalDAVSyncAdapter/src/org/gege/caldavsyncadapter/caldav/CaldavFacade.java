package org.gege.caldavsyncadapter.caldav;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;
import org.gege.caldavsyncadapter.caldav.xml.CalendarHomeHandler;
import org.gege.caldavsyncadapter.caldav.xml.CalendarsHandler;
import org.gege.caldavsyncadapter.caldav.xml.ServerInfoHandler;
import org.w3c.dom.DOMException;
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
	private final static String PROPFIND_USER_PRINCIPAL = XML_VERSION
			+ "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:current-user-principal /></d:prop></d:propfind>";

	public URI getUserPrincipal() throws URISyntaxException,
			ClientProtocolException, AuthenticationException,
			FileNotFoundException, IOException {
		URI uri = new URI(this.url.toString());
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
			throw new ClientProtocolException("no principal url found");
		}
		try {
			URI userPrincipalUri = new URI(userPrincipal);
			return uri.resolve(userPrincipalUri);
		} catch (URISyntaxException e) {
			throw new ClientProtocolException("principal url '" + userPrincipal
					+ "' malformed");
		}
	}

	public TestConnectionResult testConnection() throws IOException,
			URISyntaxException, ParserConfigurationException, SAXException {
		Log.d(TAG, "start testConnection ");
		// TODO add WRONG_SERVER_STATUS
		try {
			URI userPrincipal = getUserPrincipal();
			Log.d(TAG, userPrincipal.toString());
			List<String> calendarSets = getCalendarHomes(userPrincipal);
			List<Calendar> calendars = new ArrayList<Calendar>();
			for (String calendarSet : calendarSets) {
				try {
					URI calendarSetURI = new URI(calendarSet);
					calendarSetURI = userPrincipal.resolve(calendarSetURI);
					Log.d(TAG, "calendarSetURI : " + calendarSetURI.toString());
					calendars.addAll(getCalendarsFromSet(calendarSetURI));
				} catch (URISyntaxException e) {
					// TODO: handle exception
				}
			}
			for (Calendar calendar : calendars) {
				Log.d(TAG,
						calendar.getURI() + " : " + calendar.getDisplayName());
			}
		} catch (FileNotFoundException e) {
			return TestConnectionResult.WRONG_URL;
		} catch (AuthenticationException e) {
			return TestConnectionResult.WRONG_CREDENTIAL;
		} catch (ClientProtocolException e) {
			return TestConnectionResult.WRONG_ANSWER;
		}
		return TestConnectionResult.SUCCESS;
	}

	private final static String PROPFIND_CALENDAR_HOME_SET = XML_VERSION
			+ "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:prop><c:calendar-home-set/></d:prop></d:propfind>";

	private List<String> getCalendarHomes(URI userPrincipal)
			throws ClientProtocolException, SAXException, IOException,
			AuthenticationException, FileNotFoundException {
		HttpPropFind request = createPropFindRequest(userPrincipal,
				PROPFIND_CALENDAR_HOME_SET, 0);
		HttpResponse response = httpClient.execute(targetHost, request);
		checkStatus(response);
		CalendarHomeHandler calendarHomeHandler = new CalendarHomeHandler();
		parseXML(response, calendarHomeHandler);
		return calendarHomeHandler.calendarHomeSet;
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
			throws ClientProtocolException, SAXException, IOException,
			AuthenticationException, FileNotFoundException {
		HttpPropFind request = createPropFindRequest(calendarSet,
				PROPFIND_CALENDER_LIST, 1);
		HttpResponse response = httpClient.execute(targetHost, request);
		checkStatus(response);
		CalendarsHandler calendarsHandler = new CalendarsHandler(calendarSet);
		parseXML(response, calendarsHandler);
		return calendarsHandler.calendars;
	}

	public Iterable<Calendar> getCalendarList() throws ClientProtocolException,
			IOException, URISyntaxException, ParserConfigurationException,
			SAXException, CaldavProtocolException {
		try {

			URI userPrincipal = getUserPrincipal();
			Log.d(TAG, userPrincipal.toString());
			List<String> calendarSets = getCalendarHomes(userPrincipal);
			List<Calendar> calendars = new ArrayList<Calendar>();
			for (String calendarSet : calendarSets) {
				try {
					URI calendarSetURI = new URI(calendarSet);
					calendarSetURI = userPrincipal.resolve(calendarSetURI);
					Log.d(TAG, "calendarSetURI : " + calendarSetURI.toString());
					calendars.addAll(getCalendarsFromSet(calendarSetURI));
				} catch (URISyntaxException e) {
					// TODO: handle exception
				}
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
			throws IOException, AssertionError, ClientProtocolException {
		InputStream is = response.getEntity().getContent();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		XMLReader reader;
		try {
			parser = factory.newSAXParser();
			reader = parser.getXMLReader();
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(is));
		} catch (ParserConfigurationException e) {
			throw new AssertionError("ParserConfigurationException "
					+ e.getMessage());
		} catch (IllegalStateException e) {
			throw new ClientProtocolException(e);
		} catch (SAXException e) {
			throw new ClientProtocolException(e);
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
			throw new ClientProtocolException();
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
