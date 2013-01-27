package org.osaf.caldav4j.util;

import java.util.Comparator;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;

/* this class compares two calendar by the first event date. maybe the better place for this class is the package net.fortuna.ical4j.model.Calendar; */

public class CalendarComparator implements Comparator<Calendar> {

	public int compare(Calendar o1, Calendar  o2) {
		VEvent e1 = ICalendarUtils.getFirstEvent(o1);
		VEvent e2 = ICalendarUtils.getFirstEvent(o2);

		return e1.getStartDate().getDate().compareTo(e2.getStartDate().getDate());
	}

}
