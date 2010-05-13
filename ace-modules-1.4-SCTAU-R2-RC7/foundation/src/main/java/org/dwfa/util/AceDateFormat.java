/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Class used to get handles on <code>DateFormat</code>ters without using
 * <code>new SimpleDateFormat(...insert your favorite format here...)</code> in
 * an attempt to limit and one day rein in the number of different
 * date formats used in the code base.
 * <p>
 * This class was created by drawing existing date formats from the codebase.
 * <p>
 * If you need a <code>DateFormat</code> use one from here, please don't invent
 * yet another one. If you absolutely have to have one that isn't here please
 * add it here, don't use <code>new SimpleDateFormat()</code>. If you see one
 * here that can be removed by refactoring the code that uses that date format
 * to use one of the other date formats, fantastic, please do it! Any reduction
 * in the number of these formats is a bonus.
 *
 */
public class AceDateFormat {

    public static final String TIME_ONLY_FORMAT = "HH:mm:ss.SSS";
    public static final String CLOCK_FORMAT = "EEE, MMM d, ''yy h:mm:ss a z";
    public static final String CANDIDATE_WRITER_FORMAT = "d MMM yyyy HH:mm:ss z";
    public static final String SHORT_DISPLAY_FORMAT = "dd/MM/yyyy";
    public static final String DISPLAY_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    public static final String TABLE_DISPLAY_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATA_INPUT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String COMMIT_LOG_FORMAT = "MM/dd/yy HH:mm:ss";
    public static final String IDS_FORMAT = "yyyyMMdd HH:mm:ss";
    public static final String RF1_DIRECTORY_FORMAT = "yyyy-MM-dd";
    public static final String RF1_FORMAT = "yyyyMMdd";
    public static final String RF2_FORMAT = "yyyyMMdd";
    public static final String RF2_TZ_FORMAT = "yyyyMMdd'T'HHmmssZ";
    public static final String OLD_ACE_EXPORT_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String ACE_EXPORT_FORMAT = "yyyyMMdd'T'HHmmss";
    public static final String VERSION_HELPER_TZ_FORMAT = "yyyy.MM.dd HH:mm:ss z";
    public static final String VERSION_HELPER_FORMAT = "yyyy.MM.dd HH:mm:ss";

    public synchronized static DateFormat getVersionHelperDateFormat() {
        return new SimpleDateFormat(VERSION_HELPER_FORMAT);
    }

    public synchronized static DateFormat getVersionHelperWithTimezoneDateFormat() {
        return new SimpleDateFormat(VERSION_HELPER_TZ_FORMAT);
    }

    public synchronized static DateFormat getRf2TimezoneDateFormat() {
        return new SimpleDateFormat(RF2_TZ_FORMAT);
    }

    public synchronized static DateFormat getRf2DateFormat() {
        return new SimpleDateFormat(RF2_FORMAT);
    }

    public synchronized static DateFormat getOldAceExportDateFormat() {
        return new SimpleDateFormat(OLD_ACE_EXPORT_FORMAT);
    }

    public synchronized static DateFormat getAceExportDateFormat() {
        return new SimpleDateFormat(ACE_EXPORT_FORMAT);
    }

    public synchronized static DateFormat getRf1DateOnlyDateFormat() {
        return new SimpleDateFormat(RF1_FORMAT);
    }

    public synchronized static DateFormat getRf1DirectoryDateFormat() {
        return new SimpleDateFormat(RF1_DIRECTORY_FORMAT);
    }

    public synchronized static DateFormat getIdsDateFormat() {
        return new SimpleDateFormat(IDS_FORMAT);
    }

    public synchronized static DateFormat getCommitLogDateFormat() {
        return new SimpleDateFormat(COMMIT_LOG_FORMAT);
    }

    public synchronized static DateFormat getDataInputDateFormat() {
        return new SimpleDateFormat(DATA_INPUT_FORMAT);
    }

    public synchronized static DateFormat getTableDisplayDateFormat() {
        return new SimpleDateFormat(TABLE_DISPLAY_FORMAT);
    }

    public synchronized static DateFormat getDisplayDateFormat() {
        return new SimpleDateFormat(DISPLAY_FORMAT);
    }

    public synchronized static DateFormat getShortDisplayDateFormat() {
        return new SimpleDateFormat(SHORT_DISPLAY_FORMAT);
    }

    public synchronized static DateFormat getCandidateWriterDateFormat() {
        return new SimpleDateFormat(CANDIDATE_WRITER_FORMAT);
    }

    public synchronized static DateFormat getClockDateFormat() {
        return new SimpleDateFormat(CLOCK_FORMAT);
    }

    public synchronized static DateFormat getTimeOnlyDateFormat() {
        return new SimpleDateFormat(TIME_ONLY_FORMAT);
    }
}
