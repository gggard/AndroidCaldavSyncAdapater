package org.gege.caldavsyncadapter.caldav.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;

public class HttpPut extends HttpEntityEnclosingRequestBase implements HttpUriRequest {

    @Override
    public String getMethod() {
        return "PUT";
    }
}
