/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DateUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class TimeHelper.
 *
 * @author kec
 */
public class TimeHelper {

    /** The Constant localDateFormat. */
    private static final ThreadLocal<SimpleDateFormat> localDateFormat =
            new ThreadLocal< SimpleDateFormat>() {

                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                }
            };
    
    /** The Constant localLongFileFormat. */
    private static final ThreadLocal<SimpleDateFormat> localLongFileFormat =
            new ThreadLocal< SimpleDateFormat>() {

                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
                }
            };
    
    /** The Constant altLongFileFormat. */
    private static final ThreadLocal<SimpleDateFormat> altLongFileFormat =
            new ThreadLocal< SimpleDateFormat>() {

                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                }
            };

    
    /** The Constant localShortFileFormat. */
    private static final ThreadLocal<SimpleDateFormat> localShortFileFormat =
            new ThreadLocal< SimpleDateFormat>() {

                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyyMMdd");
                }
            };

    /**
     * Gets the remaining time string.
     *
     * @param completedCount the completed count
     * @param totalCount the total count
     * @param elapsed the elapsed
     * @return the remaining time string
     */
    public static String getRemainingTimeString(int completedCount, int totalCount, long elapsed) {
        float conceptCountFloat = totalCount;
        float completedFloat = completedCount;
        float percentComplete = completedFloat / conceptCountFloat;
        float estTotalTime = elapsed / percentComplete;
        long remaining = (long) (estTotalTime - elapsed);
        String remainingStr = getElapsedTimeString(remaining);
        return remainingStr;
    }

    /**
     * Gets the elapsed time string.
     *
     * @param elapsed the elapsed
     * @return the elapsed time string
     */
    public static String getElapsedTimeString(long elapsed) {
        String elapsedStr = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(elapsed),
                TimeUnit.MILLISECONDS.toSeconds(elapsed)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));
        return elapsedStr;
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public static SimpleDateFormat getDateFormat() {
        return localDateFormat.get();
    }

    /**
     * Gets the file date format.
     *
     * @return the file date format
     */
    public static SimpleDateFormat getFileDateFormat() {
        return localLongFileFormat.get();
    }
    
    /**
     * Gets the alt file date format.
     *
     * @return the alt file date format
     */
    public static SimpleDateFormat getAltFileDateFormat() {
        return altLongFileFormat.get();
    }

    /**
     * Gets the short file date format.
     *
     * @return the short file date format
     */
    public static SimpleDateFormat getShortFileDateFormat() {
        return localShortFileFormat.get();
    }

    /**
     * Gets the time from string.
     *
     * @param time the time
     * @param formatter the formatter
     * @return the time from string
     * @throws ParseException the parse exception
     */
    public static long getTimeFromString(String time, SimpleDateFormat formatter) throws ParseException {
        if (time.toLowerCase().equals("latest")) {
            return Long.MAX_VALUE;
        }
        if (time.toLowerCase().equals("bot")) {
            return Long.MIN_VALUE;
        }
        return formatter.parse(time).getTime();
    }
    
    /**
     * Gets the time from string.
     *
     * @param time the time
     * @return the time from string
     * @throws ParseException the parse exception
     */
    public static long getTimeFromString(String time) throws ParseException{
        if (time.toLowerCase().equals("latest")) {
            return Long.MAX_VALUE;
        }
        if (time.toLowerCase().equals("bot")) {
            return Long.MIN_VALUE;
        }
        
        String[] dateFormats = new String[] {
            localDateFormat.get().toPattern(),
            localLongFileFormat.get().toPattern(),
            altLongFileFormat.get().toPattern(),
            localShortFileFormat.get().toPattern()};
        return DateUtils.parseDate(time, dateFormats).getTime();
    }
    
    /**
     * Format date for file.
     *
     * @param time the time
     * @return the string
     */
    public static String formatDateForFile(long time) {
        return FormatDateForFile(new Date(time));
    }

    /**
     * Format date for file.
     *
     * @param date the date
     * @return the string
     */
    private static String FormatDateForFile(Date date) {
        if (date.getTime() == Long.MIN_VALUE) {
            return "beginning of time";
        }
        if (date.getTime() == Long.MAX_VALUE) {
            return "end of time";
        }
        return localLongFileFormat.get().format(date);
    }

    /**
     * Format date.
     *
     * @param time the time
     * @return the string
     */
    public static String formatDate(long time) {
        return formatDate(new Date(time));
    }

    /**
     * Format date.
     *
     * @param date the date
     * @return the string
     */
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
