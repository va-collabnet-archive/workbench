package org.dwfa.mojo.relformat.mojo.sql.converter;

public final class StringSQLTypeConverter implements SQLTypeConverter {

    public String convert(final String value) {
        return "'" +  escapeQuotations(value) + "'";
    }

    private String escapeQuotations(final String value) {
        return value.replace("'", "''");
    }
}
