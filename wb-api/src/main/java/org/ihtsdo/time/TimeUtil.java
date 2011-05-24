package org.ihtsdo.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @deprecated use TimeHelper instead.
 * @author kec
 */
public class TimeUtil {

    
     private static final ThreadLocal <SimpleDateFormat> localDateFormat = 
         new ThreadLocal < SimpleDateFormat > () {
             @Override protected SimpleDateFormat initialValue() {
                 return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
         }
     };
 
     private static final ThreadLocal <SimpleDateFormat> localFileFormat = 
         new ThreadLocal < SimpleDateFormat > () {
             @Override protected SimpleDateFormat initialValue() {
                 return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
         }
     };

	public static String getRemainingTimeString(int completedCount, int totalCount, long elapsed) {
		float conceptCountFloat = totalCount;
		float completedFloat = completedCount;
		float percentComplete = completedFloat / conceptCountFloat;
		float estTotalTime = elapsed / percentComplete;
		long remaining = (long) (estTotalTime - elapsed);
		String remainingStr = getElapsedTimeString(remaining);
		return remainingStr;
	}

	public static String getElapsedTimeString(long elapsed) {
		String elapsedStr = String.format("%d min, %d sec",
			    TimeUnit.MILLISECONDS.toMinutes(elapsed),
			    TimeUnit.MILLISECONDS.toSeconds(elapsed) -
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed))
			);
		return elapsedStr;
	}

	public static SimpleDateFormat getDateFormat() {
		return localDateFormat.get();
	}

	public static SimpleDateFormat getFileDateFormat() {
		return localFileFormat.get();
	}

	public static String formatDateForFile(long time) {
		if (time == Long.MIN_VALUE) {
			return "beginning of time";
		}
		if (time == Long.MAX_VALUE) {
			return "end of time";
		}
		return localFileFormat.get().format(new Date(time));
	}

	public static String formatDate(long time) {
		if (time == Long.MIN_VALUE) {
			return "beginning of time";
		}
		if (time == Long.MAX_VALUE) {
			return "end of time";
		}
		return localDateFormat.get().format(new Date(time));
	}
}
