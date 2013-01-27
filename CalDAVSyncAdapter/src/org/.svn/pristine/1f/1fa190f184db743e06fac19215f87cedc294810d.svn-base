 /* Copyright 2008 Babel srl
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
 * */
package org.osaf.caldav4j.model.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osaf.caldav4j.CalDAVConstants;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.OutputsDOMBase;



/**
 *  http://tools.ietf.org/html/rfc2518#section-12.3     
 *      <!ELEMENT href (#PCDATA)>
 * 
 *  ex. <D:HREF>
 * @author rpolli@babel.it
 * 
 */
public class DavHref extends OutputsDOMBase {
    
    public static final String ELEMENT_NAME = "href";
    
    private String davNamespaceQualifier = null;
    private String uri = null;

    public DavHref(String davNapespaceQualifier, String uri) {
        this.davNamespaceQualifier = davNapespaceQualifier;
        this.uri = uri.toString();
    }

    protected String getElementName() {
        return ELEMENT_NAME;
    }

    protected String getNamespaceQualifier() {
        return davNamespaceQualifier;
    }

    protected String getNamespaceURI() {
        return CalDAVConstants.NS_DAV;
    }
    
    protected String getUri() {
		return uri;
	}
    
    protected void setUri(String u) {
    	uri=u;
	}
    
	protected String getTextContent() {
		return uri.toString();
	}
    
   
    /**
     *   <!ELEMENT comp-filter (is-defined | time-range)?
     *                        comp-filter* prop-filter*>
     *                        
     *   <!ATTLIST comp-filter name CDATA #REQUIRED> 
     */
    public void validate() throws DOMValidationException{
    	return;
    }

	@Override
	protected Map<String, String> getAttributes() {
		// TODO Auto-generated method stub
        Map<String, String> m = null;
        m = new HashMap<String, String>();
		return m;
	}

	@Override // it has no children
	protected Collection<? extends OutputsDOM> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

}
