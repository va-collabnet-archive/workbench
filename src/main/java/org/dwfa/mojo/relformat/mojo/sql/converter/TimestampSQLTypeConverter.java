package org.dwfa.mojo.relformat.mojo.sql.converter;

public final class TimestampSQLTypeConverter implements SQLTypeConverter {

    private final SQLTypeConverter converter;

    public TimestampSQLTypeConverter(final SQLTypeConverter converter) {
        this.converter = converter;
    }

    public String convert(final String value) {
        return "TIMESTAMP('" + converter.convert(value) + "')";
    }
}
