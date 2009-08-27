package org.dwfa.vodb.bind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dwfa.ace.log.AceLog;

/**
 * Granularity of SNOMED version stamp is seconds, not milliseconds. Version
 * stamps to not need to go before 1960 since SNOMED was created in the 1960s.
 * 
 * Integer.MAX_INT = 2,147,483,647
 * 
 * If this is seconds, then it represents 35,791,394 minutes 596,523 hours
 * 24,855 days 68 years
 * 
 * So if we set 0 = Jan 1, 2028 (1830407753464L), and granularity = seconds, we
 * can represent versions from 1960 -> 2096.
 * 
 * @author kec
 * 
 */
public class ThinVersionHelper {
	private static long timeZero = 1830407753464L;
	private static int timeZeroInt = 1830407753;

	public ThinVersionHelper() {
		/*
		 * timeZero = Calendar.getInstance(); timeZero.set(2028, 0, 1); zeroRef
		 * = timeZero.getTimeInMillis();
		 */
		AceLog.getAppLog().info("Zero ref is: " + timeZero);
	}

	private static ThreadLocal<SimpleDateFormat> dateFormatterTL = new ThreadLocal<SimpleDateFormat>();

	public static int convert(long time) {
		if (time == Long.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (time == Long.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		int timeInSec = (int) (time / 1000);
		return timeInSec - timeZeroInt;
	}

	public static long convert(int version) {
		if (version == Integer.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		if (version == Integer.MIN_VALUE) {
			return Long.MIN_VALUE;
		}
		long added = timeZeroInt + version;
		return added * 1000;
	}

	public static int convert(String dateStr) throws ParseException {
		if (dateStr.equalsIgnoreCase("latest")) {
			return Integer.MAX_VALUE;
		}
		SimpleDateFormat formatter = dateFormatterTL.get();
		if (formatter == null) {
			formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			dateFormatterTL.set(formatter);
		}
		Date d = formatter.parse(dateStr);
		return convert(d.getTime());
	}

	/*
	 * Convert with time zone sensitivity
	 * 
	 * <br> 2009.07.31 00:00:00 GMT
	 */
	public static int convertTz(String dateStr) throws ParseException {
		if (dateStr.equalsIgnoreCase("latest")) {
			return Integer.MAX_VALUE;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy.MM.dd HH:mm:ss z");
		Date d = formatter.parse(dateStr);
		return convert(d.getTime());
	}

	public static String format(int version) {
		if (version == Integer.MAX_VALUE) {
			return "uncommitted";
		}
		if (version == Integer.MIN_VALUE) {
			return "beginning of time";
		}
		long time = convert(version);
		SimpleDateFormat formatter = dateFormatterTL.get();
		if (formatter == null) {
			formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			dateFormatterTL.set(formatter);
		}
		return formatter.format(new Date(time));
	}

	/*
	 * Format using default time zone
	 */
	public static String formatTz(int version) {
		return formatTz(version, null);
	}

	/*
	 * Format using specified time zone (e.g. GMT)
	 */
	public static String formatTz(int version, String tz) {
		if (version == Integer.MAX_VALUE) {
			return "uncommitted";
		}
		if (version == Integer.MIN_VALUE) {
			return "beginning of time";
		}
		long time = convert(version);
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy.MM.dd HH:mm:ss z");
		if (tz != null)
			formatter.setTimeZone(TimeZone.getTimeZone(tz));
		return formatter.format(new Date(time));
	}

	public static long getTimeZero() {
		return timeZero;
	}

	public static int getTimeZeroInt() {
		return timeZeroInt;
	}

	public static String uncommittedHtml() {
		return "<html><font color='80E048'>uncommitted";
	}
}
