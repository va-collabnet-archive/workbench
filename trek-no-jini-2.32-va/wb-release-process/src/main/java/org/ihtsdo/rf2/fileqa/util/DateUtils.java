package org.ihtsdo.rf2.fileqa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	public static boolean isValidDateStr(String date, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			sdf.parse(date);
		} catch (ParseException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	public static String elapsedTime(String message, Date sDate, Date eDate) {

		// elpsed time
		final long timeInMillis = eDate.getTime() - sDate.getTime();
		final int days = (int) (timeInMillis / (24L * 60 * 60 * 1000));
		int remdr = (int) (timeInMillis % (24L * 60 * 60 * 1000));
		final int hours = remdr / (60 * 60 * 1000);
		remdr %= 60 * 60 * 1000;
		final int minutes = remdr / (60 * 1000);
		remdr %= 60 * 1000;
		final int seconds = remdr / 1000;
		final int ms = remdr % 1000;

		String displayString = message += String.format("%02d:%02d:%02d:%02d.%03d (days:hours:mins:secs.millisecs) ", days, hours, minutes, seconds, ms);

		return displayString;
	}
}