package org.javautil.text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatFactory {
    
    public static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSS");;
	/**
	 * Example "2018-07-31"
	 *
	 * @return
	 */
	public static SimpleDateFormat getYyyyDashMmDashDd() {
		return new SimpleDateFormat("yyyy-MM-dd");

	}

	/**
	 * 2018-08-31T13:21:03.000
	 *
	 * @return
	 */
	public static SimpleDateFormat getDateTimeForFileName() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSS");
	}
	
	public static String getTimestamp() {
	   return timestampFormat.format(new Date());

	}

	//	yyyy-MM-dd HH:mm:ss	2012-01-31 23:59:59
	//	yyyy-MM-dd HH:mm:ss.SSS	2012-01-31 23:59:59.999
	//	yyyy-MM-dd HH:mm:ss.SSSZ	2012-01-31 23:59:59.999+0100
	//	EEEEE MMMMM yyyy HH:mm:ss.SSSZ	Saturday November 2012 10:45:42.720+0100
}
