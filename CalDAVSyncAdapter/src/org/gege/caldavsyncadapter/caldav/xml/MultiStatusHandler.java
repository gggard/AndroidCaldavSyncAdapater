package org.gege.caldavsyncadapter.caldav.xml;

import org.gege.caldavsyncadapter.caldav.xml.sax.MultiStatus;
import org.gege.caldavsyncadapter.caldav.xml.sax.Prop;
import org.gege.caldavsyncadapter.caldav.xml.sax.PropStat;
import org.gege.caldavsyncadapter.caldav.xml.sax.Response;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class MultiStatusHandler extends DefaultHandler {
	public MultiStatus mMultiStatus;
	private Response mResponse;
	private PropStat mPropStat;
	private Prop mProp;
	private String mCurrentValue;
	
	private String RESPONSE = ":response";
	private String HREF = ":href";
	private String PROPSTAT = ":propstat";
	private String PROP = ":prop";
	private String STATUS = ":status";
	private String CALENDARDATA = ":calendar-data";
	private String GETETAG = ":getetag";
	private boolean NewTag = false;
	
	public MultiStatusHandler() {
		mMultiStatus = new MultiStatus();
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (NewTag)
			mCurrentValue = new String(ch, start, length);
		else
			mCurrentValue += new String(ch, start, length);
		
		NewTag = false;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		NewTag = true;
		if (qName.endsWith(RESPONSE)) {
			mResponse = new Response();
			mMultiStatus.ResponseList.add(mResponse);
		} else if (qName.endsWith(PROPSTAT)) {
			mPropStat = new PropStat();
			mResponse.propstat = mPropStat;
		} else if (qName.endsWith(PROP)) {
			mProp = new Prop();
			mPropStat.prop = mProp;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.endsWith(HREF)) {
			mResponse.href = mCurrentValue;
		} else if (qName.endsWith(STATUS)) {
			mPropStat.status = mCurrentValue;
		} else if (qName.endsWith(CALENDARDATA)) {
			mProp.calendardata = mCurrentValue;
		} else if (qName.endsWith(GETETAG)) {
			mProp.getetag = mCurrentValue;
		}
	}
}
