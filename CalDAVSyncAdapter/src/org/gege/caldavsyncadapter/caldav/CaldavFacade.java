package org.gege.caldavsyncadapter.caldav;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
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
import org.gege.caldavsyncadapter.caldav.entities.Calendar;
import org.gege.caldavsyncadapter.caldav.entities.CalendarEvent;
import org.gege.caldavsyncadapter.caldav.http.HttpPropFind;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.util.Log;

public class CaldavFacade {

	private static final String TAG = "CaldavFacade";
	
	private static HttpClient httpClient;

	private boolean trustAll = true;

	private URL url;

	private static HttpHost targetHost;
	
	protected HttpClient getHttpClient()  {



		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		//ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
	        
	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", new PlainSocketFactory(), 80)); 
	    registry.register(new Scheme("https", (trustAll  ? EasySSLSocketFactory.getSocketFactory(): SSLSocketFactory.getSocketFactory()), 443));
	    DefaultHttpClient client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, registry), params);
		
	       return client;
	}
	
	public CaldavFacade(String mUser, String mPassword, String mURL) throws MalformedURLException  {
		url = new URL(mURL);
		
		httpClient = getHttpClient();
		
		AuthScope as = new AuthScope(url.getHost(), 443);
        UsernamePasswordCredentials upc = new UsernamePasswordCredentials(mUser, mPassword);

       ((AbstractHttpClient) httpClient).getCredentialsProvider().setCredentials(as, upc);
		
       BasicHttpContext localContext = new BasicHttpContext();

       BasicScheme basicAuth = new BasicScheme();
       localContext.setAttribute("preemptive-auth", basicAuth);

       String proto = "http";
       int port = 80;
       
       if (url.getProtocol().equalsIgnoreCase("https")) {
    	   proto="https";
    	   if (url.getPort() == -1)
    		   port = 443;
    	   else
    		   port = url.getPort();
       }
       
       if (url.getProtocol().equalsIgnoreCase("http")) {
    	   proto="http";
    	   if (url.getPort() == -1)
    		   port = 80;
    	   else
    		   port = url.getPort();
       }

	   targetHost = new HttpHost(url.getHost(), port, proto);
       
      
       
       
       
//		 HostConfiguration hostConfig = new HostConfiguration();
//	     hostConfig.setHost(url.getHost(), url.getPort(), url.getProtocol());
//		
//	     CalDAV4JMethodFactory methodFactory = new CalDAV4JMethodFactory();
//	     
//		calendarCollection = new CalDAVCollection(
//       		url.getPath(), hostConfig, methodFactory,
//               CalDAVConstants.PROC_ID_DEFAULT);
//		
//		
//		
//		Credentials credentials = new UsernamePasswordCredentials(mUser, mPassword); 
//		
//       httpClient.getState().setCredentials(new AuthScope(url.getHost(), url.getPort()),
//       		credentials);
//       httpClient.getParams().setAuthenticationPreemptive(true);
       
       
      
	}
	
	public enum TestConnectionResult {
		WRONG_CREDENTIAL,
		WRONG_URL,
		WRONG_SERVER_STATUS,
		WRONG_ANSWER, SUCCESS
	}
	
	public TestConnectionResult testConnection() throws HttpHostConnectException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, URISyntaxException {
		
		Log.d (TAG, "start testConnection ");
		
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<D:propfind xmlns:D=\"DAV:\">" +
				"<D:prop>" +
				"<D:resourcetype/>" +
				"</D:prop>" +
				"</D:propfind>";
		
		HttpPropFind request = null;
		
		request = new HttpPropFind();
		request.setURI(new URI(url.getPath()));
		request.setHeader("Depth", "1");
		request.setEntity(new StringEntity(requestBody));
		
		HttpResponse response = httpClient.execute(targetHost,request);
		
		Log.d(TAG, "HTTP response status = "+response.getStatusLine().getStatusCode());
		
		if (response.getStatusLine().getStatusCode()==401) {
			//unauthorised
			return TestConnectionResult.WRONG_CREDENTIAL;
		}
		
		if (response.getStatusLine().getStatusCode()==404) {
			//not found
			return TestConnectionResult.WRONG_URL;
		}
		
		if ((response.getStatusLine().getStatusCode() != 200) &&
				(response.getStatusLine().getStatusCode() != 207)) {
			return TestConnectionResult.WRONG_SERVER_STATUS;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line;
		} while (line != null);
		
		Log.d(TAG, "HttpResponse status="+response.getStatusLine()+ " body= "+body);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(new InputSource(new ByteArrayInputStream(body.getBytes("utf-8"))));
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagNameNS("*","principal");
		
		if (items.getLength()!=1) {
			Log.d (TAG, "endConnection failure");
			return TestConnectionResult.WRONG_ANSWER;
		} else {
			Log.d (TAG, "endConnection success");
			return TestConnectionResult.SUCCESS;
		}
		
		 
	}
	
	
	public Iterable<CalendarEvent> getCalendarEvents(Calendar calendar) throws URISyntaxException, ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		
		List<CalendarEvent> calendarEventList = new ArrayList<CalendarEvent>();
		
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<D:propfind xmlns:D=\"DAV:\">" +
				"<D:prop>" +
				"<D:getetag/>" +
				"</D:prop>" +
				"</D:propfind>";
		
		HttpPropFind request = null;
		
		request = new HttpPropFind();
		request.setURI(calendar.getURI());
		request.setHeader("Depth", "1");
		request.setEntity(new StringEntity(requestBody));
		
		Log.d(TAG, "Getting eTag by PROPFIND at "+request.getURI());
		
		HttpResponse response = httpClient.execute(targetHost,request);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line;
		} while (line != null);
		
		Log.d(TAG, "HttpResponse status="+response.getStatusLine()+ " body= "+body);

		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(new InputSource(new ByteArrayInputStream(body.getBytes("utf-8"))));
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagNameNS("*","getetag");
		
		for (int i=0;i<items.getLength();i++) {
			CalendarEvent calendarEvent = new CalendarEvent();
			
			Node node = items.item(i);
			
			if (node.getTextContent().trim().length() == 0) continue; // not an event
			
			calendarEvent.setETag(node.getTextContent().trim());
			
			node = node.getParentNode(); // prop
			node = node.getParentNode(); // propstat
			node = node.getParentNode(); // response
			
			NodeList children = node.getChildNodes();
			for (int j=0 ; j<children.getLength() ; j++) {
				Node childNode = children.item(j);
				if (childNode.getNodeName().equalsIgnoreCase("href")) {
					calendarEvent.setURI(new URI(childNode.getTextContent()));
				}
			}
			
			calendarEventList.add(calendarEvent);
			
		}
		
		return calendarEventList;
	}
	
	public Iterable<Calendar> getCalendarList() throws ClientProtocolException, IOException, URISyntaxException, ParserConfigurationException, SAXException, CaldavProtocolException  {
		
		List<Calendar> calendarList = new ArrayList<Calendar>();
		
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<D:propfind xmlns:D=\"DAV:\">" +
				"<D:prop>" +
				"<D:resourcetype/>" +
				"</D:prop>" +
				"</D:propfind>";
		
		HttpPropFind request = null;
		
		request = new HttpPropFind();
		request.setURI(new URI(url.getPath()));
		request.setHeader("Depth", "1");
		request.setEntity(new StringEntity(requestBody));
		
		HttpResponse response = httpClient.execute(targetHost,request);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line;
		} while (line != null);
		
		Log.d(TAG, "HttpResponse status="+response.getStatusLine()+ " body= "+body);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(new InputSource(new ByteArrayInputStream(body.getBytes("utf-8"))));
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagNameNS("*","calendar");
		
		for (int i=0;i<items.getLength();i++){
			Calendar calendar = new Calendar();
			Node item = items.item(i);
			item = item.getParentNode(); //resourcetype
			item = item.getParentNode(); //prop
			item = item.getParentNode(); // propstat
			item = item.getParentNode(); // response
			
			NodeList children = item.getChildNodes();
			for (int j=0 ; j<children.getLength() ; j++) {
				Node node = children.item(j);
				if (node.getNodeName().equalsIgnoreCase("href")) {
					calendar.setURI(new URI(node.getTextContent()));
				}
			}
			
			updateCalendarInfos(calendar);
			
			calendarList.add(calendar);
		}
		
		return calendarList;
	}

	private void updateCalendarInfos(Calendar calendar) throws URISyntaxException, ClientProtocolException, IOException, ParserConfigurationException, SAXException, CaldavProtocolException {
		String requestBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<D:propfind xmlns:D=\"DAV:\" xmlns:CS=\"http://calendarserver.org/ns/\">" +
				"<D:prop>" +
				"<D:displayname/>" +
				"<CS:getctag/>" +
				"</D:prop>" +
				"</D:propfind>";
		
		HttpPropFind request = null;
		
		request = new HttpPropFind();
		request.setURI(calendar.getURI());
		request.setHeader("Depth", "0");
		request.setEntity(new StringEntity(requestBody));
		
		HttpResponse response = httpClient.execute(targetHost,request);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line;
		} while (line != null);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dom = builder.parse(new InputSource(new ByteArrayInputStream(body.getBytes("utf-8"))));
		Element root = dom.getDocumentElement();
		NodeList items = root.getElementsByTagName("displayname");
		if (items.getLength() != 1) {
			throw new CaldavProtocolException("None or multiple display name");
		}
		calendar.setDisplayName(items.item(0).getTextContent().trim());
		
		items = root.getElementsByTagNameNS("*", "getctag");
		if (items.getLength() != 1) {
			throw new CaldavProtocolException("None or multiple ctags");
		}
		calendar.setCTag(items.item(0).getTextContent().trim());
		

	}

	public static void fetchEventBody(CalendarEvent calendarEvent) throws ClientProtocolException, IOException {
		HttpGet request = null;
		
		request = new HttpGet();
		request.setURI(calendarEvent.getUri());
		

		HttpResponse response = httpClient.execute(targetHost,request);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
		String line;
		String body = "";
		do {
			line = reader.readLine();
			if (line != null)
				body += line + "\n";
		} while (line != null);
		
		calendarEvent.setICS(body);
		
		Log.d(TAG, "HttpResponse GET event status="+response.getStatusLine()+ " body= "+body);
	}
}
