/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.caldav4j.model.request;

import java.util.List;

import org.osaf.caldav4j.xml.OutputsDOM;
import org.osaf.caldav4j.xml.SimpleDOMOutputtingObject;

/**
 * a simple, customizable DAV property with children
 * ex. <D:ACL></D:ACL>
 * @author rpolli
 * 
 */
public class PropProperty extends SimpleDOMOutputtingObject{

    public PropProperty(){
        
    }
    
    public PropProperty(String namespaceURI, String namespaceQualifier,
            String propertyName) {
        this.setNamespaceURI(namespaceURI);
        this.setElementName(propertyName);
        this.setNamespaceQualifier(namespaceQualifier);
    }
    
    public PropProperty(String namespaceURI, String namespaceQualifier,
            String propertyName, List<OutputsDOM> children) {
        this.setNamespaceURI(namespaceURI);
        this.setElementName(propertyName);
        this.setNamespaceQualifier(namespaceQualifier);
        this.setChildren(children);
    }

}
