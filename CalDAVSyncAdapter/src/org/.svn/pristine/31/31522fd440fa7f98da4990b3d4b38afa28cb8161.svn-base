/*
 * Copyright 2007 Open Source Applications Foundation
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

package org.osaf.caldav4j.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

public class UrlUtils {
    public static String stripHost(String href){
        if (!href.startsWith("http")){
            return href;
        }
        int indexOfColon = href.indexOf(":");
        int index = href.indexOf("/", indexOfColon + 3);
        return href.substring(index);
    }
    
    public static String removeDoubleSlashes(String s) {
    	return s.replaceAll("([^:])/{2,}","$1/");
    }
    
    public static String getHeaderPrettyValue(HttpMethod method, String headerName) {
    	if (method != null && headerName != null) {
    		Header header  = method.getResponseHeader(headerName);  
    		if (header != null) {
    			return header.getValue();
    		}
    	}
    	
    	return null;
    }
    
    public static String parseISToString(InputStream is){
    	StringBuffer sb = new StringBuffer();
    	try {
    		BufferedReader din = new BufferedReader(new InputStreamReader(is));

    		String line = null;
    		while((line=din.readLine()) != null){
    			sb.append(line+"\n");
    		}
    	}catch(Exception ex){
    		ex.getMessage();
    	}finally{
    		try{
    			is.close();
    		}catch(Exception ex){}
    	}
    	return sb.toString();
    }

}
