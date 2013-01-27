package org.osaf.caldav4j.methods;

import org.osaf.caldav4j.util.UrlUtils;


public class DeleteMethod extends org.apache.webdav.lib.methods.DeleteMethod {

	public DeleteMethod() {
		super();
	}
	
	public DeleteMethod(String s) {
		super(s);
	}

    // remove double slashes
    public void setPath(String path) {
    	super.setPath(UrlUtils.removeDoubleSlashes(path));
    }
}
