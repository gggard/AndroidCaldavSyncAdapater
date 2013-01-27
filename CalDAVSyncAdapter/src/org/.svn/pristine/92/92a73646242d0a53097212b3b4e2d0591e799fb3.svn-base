/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.webdav.lib.methods.XMLResponseMethodBase.Response;
import org.apache.webdav.lib.util.QName;

/**
 * Some helpful routines to make using PropFindMethod a little more managable.
 * @author bobbyrullo
 *
 */
public class PropFindUtils {

    /**
     * Returns a map of qualifed property names ==> Property's for a given
     * Response
     * 
     * @param response
     * @return
     */
    public static Map<QName, org.apache.webdav.lib.Property> createPropertiesMap(
            Response response) {
        Map<QName, org.apache.webdav.lib.Property> m = new HashMap<QName, org.apache.webdav.lib.Property>();
        Enumeration e = response.getProperties();
        while (e.hasMoreElements()) {
            org.apache.webdav.lib.Property prop = (org.apache.webdav.lib.Property) e
                    .nextElement();
            QName qname = new QName(prop.getNamespaceURI(), prop.getLocalName());
            m.put(qname, prop);
        }
        return m;
    }

}
