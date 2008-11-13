package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.util.StringArrayCleaner;

public final class TableDataExtractorImpl implements TableDataExtractor {

    private static final String SPACE = "(\\s|\\(|\\))";

    private final StringArrayCleaner cleaner;
    private final ColumnTypeCleaner columnTypeCleaner;

    public TableDataExtractorImpl(final StringArrayCleaner cleaner, final ColumnTypeCleaner columnTypeCleaner) {
        this.cleaner = cleaner;
        this.columnTypeCleaner = columnTypeCleaner;
    }

    public String extractName(final String text) {
        try {
            return cleaner.clean(text.split(SPACE))[2];
        } catch (Exception e) {
            throw new TableDataExtractorException("The table name could not be extracted from: [" + text + "]", e);
        }
    }

    public String[] extractColumn(final String text) {
        String[] columnSpec = cleaner.clean(text.split(SPACE));
        columnSpec[1] = columnTypeCleaner.clean(columnSpec[1]);                 
        return new String[] {columnSpec[0], columnSpec[1]};
    }
}
