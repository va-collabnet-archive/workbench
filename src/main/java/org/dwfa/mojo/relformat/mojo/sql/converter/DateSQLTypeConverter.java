package org.dwfa.mojo.relformat.mojo.sql.converter;

public final class DateSQLTypeConverter implements SQLTypeConverter {

    public String convert(final String value) {
        return "DATE('" + value + "')";
    }
}
