package org.dwfa.vodb.process;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


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

    DateFormatterThreadLocal formatter = new DateFormatterThreadLocal("yyyy-MM-dd HH:mm:ss");

    DateFormatterThreadLocal formatter2 = new DateFormatterThreadLocal("yyyyMMdd HH:mm:ss");
    
    DateFormatterThreadLocal formatter3 = new DateFormatterThreadLocal("yyyyMMdd'T'HHmmssZ");
    
    DateFormatterThreadLocal formatter4 = new DateFormatterThreadLocal("yyyyMMdd");

    private static ProcessDates dateProcessor = new ProcessDates();
    
    private Date getDateFromString(String dateStr) throws ParseException {
        if (dateStr.contains("-") && dateStr.contains(":")) {
            return formatter.get().parse(dateStr);
        } else if (dateStr.contains("T")) {
            return formatter3.get().parse(dateStr.replace("Z", "-0000"));
        } else if (dateStr.length() < 8) {
            return formatter4.get().parse(dateStr);
        } else {
            return formatter2.get().parse(dateStr);
        }
    }
    

    public static Date getDate(String dateStr) throws ParseException {
        return dateProcessor.getDateFromString(dateStr);
    }

}
