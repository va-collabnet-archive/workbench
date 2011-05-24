/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kec
 */
public class TimeHelper {

    
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
		return FormatDateForFile(new Date(time));
	}

   private static String FormatDateForFile(Date date) {
      if (date.getTime() == Long.MIN_VALUE) {
         return "beginning of time";
      }
      if (date.getTime() == Long.MAX_VALUE) {
         return "end of time";
      }
      return localFileFormat.get().format(date);
   }

	public static String formatDate(long time) {
		return formatDate(new Date(time));
	}

   private static String formatDate(Date date) {
      if (date.getTime() == Long.MIN_VALUE) {
         return "beginning of time";
      }
      if (date.getTime() == Long.MAX_VALUE) {
         return "end of time";
      }
      return localDateFormat.get().format(date);
   }
}

