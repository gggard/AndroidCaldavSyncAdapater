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
package org.osaf.caldav4j.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDOMOutputtingObject extends OutputsDOMBase{
    
    private String elementName = null;
    private String namespaceQualifier = null;
    private String namespaceURI = null;
    private String textContent = null;
    private List<OutputsDOM> children = new ArrayList<OutputsDOM>();
    private Map<String, String> attributes = new HashMap<String, String>();
    
    public SimpleDOMOutputtingObject(){
        
    }
    
    public SimpleDOMOutputtingObject(String namespaceURI,
            String namespaceQualifier, String elementName) {
        this.namespaceURI = namespaceURI;
        this.namespaceQualifier = namespaceQualifier;
        this.elementName = elementName;
    }
    
    public SimpleDOMOutputtingObject(String namespaceURI,
            String namespaceQualifier, String elementName, Map<String,String> attributes) {
        this.namespaceURI = namespaceURI;
        this.namespaceQualifier = namespaceQualifier;
        this.elementName = elementName;
        this.attributes = attributes;
    }
    
    public Collection<? extends OutputsDOM> getChildren() {
        return children;
    }

    public void setNamespaceQualifier(String namespaceQualifier) {
        this.namespaceQualifier = namespaceQualifier;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
    }

    public String getElementName() {
        return elementName;
    }
    
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getNamespaceQualifier() {
        return namespaceQualifier;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public void setChildren(List<OutputsDOM> children) {
        this.children = children;
    }
    
    public void addChild(OutputsDOM outputsDOM) {
        children.add(outputsDOM);
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
    
    public void setQualifiedName(String qualifiedName){
        int colonLocation = qualifiedName.indexOf(":");
        String qualifier = qualifiedName.substring(0, colonLocation);
        String localName = qualifiedName.substring(colonLocation + 1);
        this.namespaceQualifier = qualifier;
        this.elementName = localName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    public void addAttribute(String key, String value){
        attributes.put(key, value);
    }
}


