package org.dwfa.mojo.relformat.mojo.sql.converter;

public final class TimeSQLTypeConverter implements SQLTypeConverter {

    public String convert(final String value) {
        return "TIME('" + value + "')";
    }
}
