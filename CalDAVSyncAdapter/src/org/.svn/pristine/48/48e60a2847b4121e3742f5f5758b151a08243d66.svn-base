package org.osaf.caldav4j.scheduling.methods;

import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;

public class CalDAV4JScheduleMethodFactory extends CalDAV4JMethodFactory {

	public SchedulePostMethod createSchedulePostMethod() {
        SchedulePostMethod postMethod = new SchedulePostMethod();
        postMethod.setProcID(prodID);
        postMethod.setCalendarOutputter(getCalendarOutputterInstance());
        return postMethod;
	}
}
