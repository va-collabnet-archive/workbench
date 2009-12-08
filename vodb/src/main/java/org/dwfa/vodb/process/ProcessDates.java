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
package org.dwfa.vodb.process;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.dwfa.util.AceDateFormat;

public class ProcessDates {
    private class DateFormatterThreadLocal extends ThreadLocal<SimpleDateFormat> {
        String formatStr;

        private DateFormatterThreadLocal(String formatStr) {
            super();
            this.formatStr = formatStr;
        }

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(formatStr);
        }

    }

    DateFormatterThreadLocal formatter = new DateFormatterThreadLocal(AceDateFormat.DATA_INPUT_FORMAT);

    DateFormatterThreadLocal formatter2 = new DateFormatterThreadLocal(AceDateFormat.IDS_FORMAT);

    DateFormatterThreadLocal formatter3 = new DateFormatterThreadLocal(AceDateFormat.RF2_FORMAT);

    DateFormatterThreadLocal formatter3Timezone = new DateFormatterThreadLocal(AceDateFormat.RF2_FORMAT);

    DateFormatterThreadLocal formatter5 = new DateFormatterThreadLocal(AceDateFormat.OLD_ACE_EXPORT_FORMAT);

    DateFormatterThreadLocal formatter4 = new DateFormatterThreadLocal(AceDateFormat.RF1_FORMAT);

    private static ProcessDates dateProcessor = new ProcessDates();

    private Date getDateFromString(String dateStr) throws ParseException {
        if (dateStr.contains("-") && dateStr.contains(":") && dateStr.contains("T")) {
            return formatter5.get().parse(dateStr.replace("Z", "-0000"));
        } else if (dateStr.contains("-") && dateStr.contains(":")) {
            return formatter.get().parse(dateStr);
        } else if (dateStr.contains("T")) {
            if (dateStr.contains("Z")) {
                return formatter3.get().parse(dateStr);
            } else {
                return formatter3Timezone.get().parse(dateStr);
            }
        } else if (dateStr.length() < 9) {
            return formatter4.get().parse(dateStr);
        } else {
            return formatter2.get().parse(dateStr);
        }
    }

    public static Date getDate(String dateStr) throws ParseException {
        return dateProcessor.getDateFromString(dateStr);
    }

}
