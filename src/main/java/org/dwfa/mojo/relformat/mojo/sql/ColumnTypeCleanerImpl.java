package org.dwfa.mojo.relformat.mojo.sql;

public final class ColumnTypeCleanerImpl implements ColumnTypeCleaner {

    private static final String TRAILING_COLUMN_SEPARATOR       = ",";
        private static final String TRAILING_CLOSE_TABLE        = ");";

    public String clean(final String column) {
        return removeClosingTable(removeTrailingComma(column));
    }

    private String removeClosingTable(final String type) {
        return type.endsWith(TRAILING_CLOSE_TABLE) ?
                type.substring(0, type.length() - TRAILING_CLOSE_TABLE.length()) : type;
    }

    private String removeTrailingComma(final String column) {
        return column.endsWith(TRAILING_COLUMN_SEPARATOR) ?
                column.substring(0, column.length() - TRAILING_COLUMN_SEPARATOR.length()) : column;
    }
}
