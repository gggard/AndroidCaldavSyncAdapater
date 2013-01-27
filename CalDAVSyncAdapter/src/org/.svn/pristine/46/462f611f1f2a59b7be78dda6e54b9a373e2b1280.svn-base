/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.caldav4j.exceptions;

import org.apache.commons.httpclient.HttpMethod;


/**
 * This is the root class for all CalDAV4J specific exceptions.
 * 
 * @author bobbyrullo
 * 
 */
public class BadStatusException extends CalDAV4JException {

    private static final long serialVersionUID = 1L;

    private static final String MESSAGE = "Bad status %d invoking method %s %s";
    
    public BadStatusException(String message) {
        super(message);
    }

    public BadStatusException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BadStatusException(int status, String method, String path) {
    	super(String.format(MESSAGE,status,method,path));
    }
    public BadStatusException(HttpMethod method) {
    	super(String.format(MESSAGE,method.getStatusCode(),method.getName(),method.getPath()));
    }
}
