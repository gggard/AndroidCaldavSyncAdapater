package org.gege.caldavsyncadapter.caldav.http;

import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpParams;

public class HttpPropFind extends HttpEntityEnclosingRequestBase implements HttpUriRequest {

	@Override
	public String getMethod() {
		return "PROPFIND";
	}



}
