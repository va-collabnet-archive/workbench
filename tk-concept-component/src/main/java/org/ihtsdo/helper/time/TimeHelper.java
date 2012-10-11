/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DateUtils;

/**
 * The Class TimeHelper helps format date and times according to specified
 * formats.
 *
 */
public class TimeHelper {

    private static final ThreadLocal<SimpleDateFormat> localDateFormat =
            new ThreadLocal< SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                }
            };
    private static final ThreadLocal<SimpleDateFormat> localLongFileFormat =
            new ThreadLocal< SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
                }
            };
    private static final ThreadLocal<SimpleDateFormat> altLongFileFormat =
            new ThreadLocal< SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                }
            };
    private static final ThreadLocal<SimpleDateFormat> localShortFileFormat =
            new ThreadLocal< SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyyMMdd");
                }
            };

    /**
     * Gets a string representing the time remaining.
     *
     * @param completedCount an <code>int</code> representing the amount
     * processed
     * @param totalCount an <code>int</code> representing total to process
     * @param elapsed a <code>long</code> representing the elapsed time
     * @return a string representing the time remaining
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
     * Gets a
     * <code>String</code> representation of time from a
     * <code>long</code>. Formatted as: "%d min, %d sec"
     *
     * @param elapsed the <code>long</code> value representing the time
     * @return a <code>String</code> representing the time
     */
    public static String getElapsedTimeString(long elapsed) {
        String elapsedStr = String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(elapsed),
                TimeUnit.MILLISECONDS.toSeconds(elapsed)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsed)));
        return elapsedStr;
    }

    /**
     * Gets a
     * <code>SimpleDateFormat</code> representing the date format.
     *
     * @return the date format: MM/dd/yy HH:mm:ss
     */
    public static SimpleDateFormat getDateFormat() {
        return localDateFormat.get();
    }

    /**
     * Gets a
     * <code>SimpleDateFormat</code> representing the file date format.
     *
     * @return the file date format: yyyy-MM-dd-HH.mm.ss
     */
    public static SimpleDateFormat getFileDateFormat() {
        return localLongFileFormat.get();
    }

    /**
     * Gets a
     * <code>SimpleDateFormat</code> representing the alternate file date
     * format.
     *
     * @return the alternate file date format: yyyy.MM.dd hh:mm:ss
     */
    public static SimpleDateFormat getAltFileDateFormat() {
        return altLongFileFormat.get();
    }

    /**
     * Gets a
     * <code>SimpleDateFormat</code> representing the short file date format.
     *
     * @return the short file date format: yyyyMMdd
     */
    public static SimpleDateFormat getShortFileDateFormat() {
        return localShortFileFormat.get();
    }

    /**
     * Gets the
     * <code>long</code> representation of time from a
     * <code>string</code>.
     *
     * @param time the formatted String representing the time
     * @param formatter the simple date format used to format the String
     * @return a <code>long</code> representation of the time
     * @throws ParseException indicates a parse exception has occurred
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
     * Gets the
     * <code>long</code> representation of time from a
     * <code>string</code>. Checks to see if the formatting of the string
     * matches: localDateFormat, localLongFileFormat, altLongFileFormat,
     * localShortFileFormat.
     *
     * @@param time the formatted String representing the time
     * @return a <code>long</code> representation of the time
     * @throws ParseException if the the time string is not formatted according
     * to the listed types
     */
    public static long getTimeFromString(String time) throws ParseException {
        if (time.toLowerCase().equals("latest")) {
            return Long.MAX_VALUE;
        }
        if (time.toLowerCase().equals("bot")) {
            return Long.MIN_VALUE;
        }

        String[] dateFormats = new String[]{
            localDateFormat.get().toPattern(),
            localLongFileFormat.get().toPattern(),
            altLongFileFormat.get().toPattern(),
            localShortFileFormat.get().toPattern()};
        return DateUtils.parseDate(time, dateFormats).getTime();
    }

    /**
     * Converts
     * <code>time</code> to a
     * <code>String</code> formatted: "yyyy-MM-dd-HH.mm.ss". Returns "beginning
     * of time" or "end of time" for Long.MIN_VALUE and Long.MAX_VALUE
     * respectively.
     *
     * @param a <code>long</code> representation of the time
     * @return the formatted <code>String</code> representing the time
     */
    public static String formatDateForFile(long time) {
        return FormatDateForFile(new Date(time));
    }

    /**
     * Converts
     * <code>time</code> to a
     * <code>String</code> formatted: "yyyy-MM-dd-HH.mm.ss". Returns "beginning
     * of time" or "end of time" for Long.MIN_VALUE and Long.MAX_VALUE
     * respectively.
     *
     * @param a <code>long</code> representation of the time
     * @return the formatted <code>String</code> representing the time
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
     * Converts
     * <code>time</code> to a
     * <code>String</code> formatted: "MM/dd/yy HH:mm:ss". Returns "beginning
     * of time" or "end of time" for Long.MIN_VALUE and Long.MAX_VALUE
     * respectively.
     *
     * @param a <code>long</code> representation of the time
     * @return the formatted <code>String</code> representing the time
     */
    public static String formatDate(long time) {
        return formatDate(new Date(time));
    }

    /**
     * Converts
     * <code>time</code> to a
     * <code>String</code> formatted: "MM/dd/yy HH:mm:ss". Returns "beginning
     * of time" or "end of time" for Long.MIN_VALUE and Long.MAX_VALUE
     * respectively.
     *
     * @param a <code>long</code> representation of the time
     * @return the formatted <code>String</code> representing the time
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
