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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.exceptions.DOMValidationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class OutputsDOMBase implements OutputsDOM{

    private static final Log log = LogFactory.getLog(OutputsDOMBase.class);
    
    protected abstract String getElementName();
    
    protected abstract String getNamespaceQualifier() ;

    protected abstract String getNamespaceURI();

    protected abstract Collection<? extends OutputsDOM> getChildren();
    
    protected abstract Map<String, String> getAttributes();
    
    protected String getQualifiedName() {
        return getNamespaceQualifier() + ":" + getElementName();
    }
    
    protected abstract String getTextContent();
    
    public void validate() throws DOMValidationException{
        return;
    }
    
    public Element outputDOM(Document document) throws DOMValidationException{
        Element e = document.createElementNS(getNamespaceURI(),
                getQualifiedName());
        
        fillElement(e);
        return e;
    }
    
    public Document createNewDocument(DOMImplementation domImplementation)
            throws DOMException, DOMValidationException {
        validate();

        Document d = domImplementation.createDocument(getNamespaceURI(),
                getQualifiedName(), null);

        Element root = (Element) d.getFirstChild();

        fillElement(root);

        return d;

    }

    protected void fillElement(Element e) throws DOMValidationException{
        /*
         * Add children elements
         */
        Collection<? extends OutputsDOM>children = getChildren();
        if (children != null && children.size() != 0) {
            Iterator<? extends OutputsDOM> i = children.iterator();
            while (i.hasNext()) {
                OutputsDOM node = (OutputsDOM) i.next();
                Element childNode = node.outputDOM(e.getOwnerDocument());
                e.appendChild(childNode);
            }
        }
               
        if (getTextContent() != null){
            e.setTextContent(getTextContent());
        }
        
        /*
         * Add Attributes
         */
        Map<String, String> attributes = getAttributes();
        if (attributes != null && attributes.size() > 0) {
            Iterator<Entry<String, String>> i = attributes.entrySet().iterator();
            while (i.hasNext()) {
                Entry<String, String> entry = i.next();
                e.setAttribute(entry.getKey().toString(), entry.getValue()
                        .toString());
            }
        }
    }
    
    /**
     * Convienience method for validating all the objects in a collection
     * @param c
     * @throws DOMValidationException
     */
    protected void validate(Collection<? extends OutputsDOM> c) throws DOMValidationException{
        for (Iterator<? extends OutputsDOM> i = c.iterator(); i.hasNext(); ){
            OutputsDOM o = (OutputsDOM) i.next();
            o.validate();
        }
    }
    
    protected void throwValidationException(String m) throws DOMValidationException{
        String message = getQualifiedName() + " - " + m;
        throw new DOMValidationException(message);
    }
}
