package org.osaf.caldav4j.methods;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.caldav4j.util.UrlUtils;

public class OptionsMethod extends org.apache.webdav.lib.methods.OptionsMethod {
    private static final Log log = LogFactory.getLog(GetMethod.class);

    public OptionsMethod() {
    	super();
    }
    
    // remove double slashes
    public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
}
