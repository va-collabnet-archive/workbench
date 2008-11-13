package org.dwfa.mojo.relformat.mojo.sql.converter;

public final class TimestampSQLTypeConverter implements SQLTypeConverter {

    private static final int TIMEZONE_FORMAT_LENGTH = 16;

    public String convert(final String value) {
        return "TIMESTAMP('" + getConvertedValue(value) + "')";
    }

    private String getConvertedValue(final String value) {
        //eg. 20081031T000000Z
        if (hasTimezoneInfo(value)) {
            return buildTimestamp(value.substring(0, 9));
        }

        //20080725
        if (hasDateOnly(value)) {
            return buildTimestamp(value);                
        }

        //assume correct format.
        return value;
    }

    private boolean hasDateOnly(final String value) {
        return value.length() == 8;
    }

    private String buildTimestamp(final String timestamp) {
        return new StringBuilder().
                    append(timestamp.substring(0, 4)).append("-").
                    append(timestamp.substring(4, 6)).append("-").
                    append(timestamp.substring(6, 8)).append(" ").
                    append(getDefaultTime()).
                    toString();
    }

    private String getDefaultTime() {
        return "00:00:00.0";
    }

    private boolean hasTimezoneInfo(final String value) {
        return value.length() == TIMEZONE_FORMAT_LENGTH && value.indexOf('T') != -1 && value.indexOf('Z') != -1;
    }
}
