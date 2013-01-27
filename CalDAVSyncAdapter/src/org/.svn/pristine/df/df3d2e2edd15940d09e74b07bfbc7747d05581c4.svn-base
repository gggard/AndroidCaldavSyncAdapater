package org.osaf.caldav4j.util;

import java.util.Enumeration;

import org.apache.webdav.lib.Ace;
import org.apache.webdav.lib.Privilege;
import org.osaf.caldav4j.model.response.Principal;


/**
 * Ace methods for easy Principal settings
 * @author rpolli
 *
 */
public class AceUtils {
	@SuppressWarnings("unchecked")
	public static Ace clone(Ace ace) {
		Ace ret = new Ace(ace.getPrincipal(),ace.isNegative(), ace.isProtected(), ace.isInherited(), ace.getInheritedFrom());
		ret.setProperty(ace.getProperty());
		Enumeration<Privilege> p = ace.enumeratePrivileges();
		while(p.hasMoreElements()) {
			ret.addPrivilege(p.nextElement());
		}
		return ret;
	}

	/**
	 * Create an ACE given a Principal
	 * @param p
	 * @return
	 */
	public static Ace createAce(Principal p) {
		Ace a = null;
		if (p.getPropertyName()!=null) {
			a = new Ace("property");
			a.setProperty(p.getPropertyName());
		} else if (p.isAll()||p.isAuthenticated()||p.isSelf()||p.isUnauthenticated()) {
			a= new Ace(p.getValue());
		}
		return a;		
	}
	
	/**
	 * Retrieve a Caldav Principal from a slide Ace.
	 * If  ace.getprincipal is set to "property", it returns directly the underlying property
	 * @param ace
	 * @return
	 */
	public static Principal getDavPrincipal(Ace ace) {
		Principal p = new Principal();
		String pString = ace.getPrincipal();
		if ("property".equals(pString)) {
			p.setPropertyName(ace.getProperty());			
		}
		return p;
	}
}
