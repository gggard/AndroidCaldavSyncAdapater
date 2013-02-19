package org.gege.caldavsyncadapter.caldav.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpReport extends HttpEntityEnclosingRequestBase implements HttpUriRequest {

	private static final String REPORT = "REPORT";

	@Override
	public String getMethod() {
		return REPORT;
	}



}
