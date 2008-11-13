package org.dwfa.mojo.relformat.mojo.sql.parser;

import org.dwfa.mojo.relformat.mojo.sql.TableDataExtractor;
import org.dwfa.mojo.relformat.util.StringArrayCleaner;

public final class TableSchemaParserImpl implements TableSchemaParser {
    
    private static final String NEW_LINE    = "(\\r\\n|\\n)";
    private static final String PRIMARY_KEY = "PRIMARY KEY";

    private final TableDataExtractor extractor;
    private final StringArrayCleaner cleaner;

    public TableSchemaParserImpl(final TableDataExtractor extractor, final StringArrayCleaner cleaner) {
        this.extractor = extractor;
        this.cleaner = cleaner;
    }

    public Table parse(final String schema) {
        String[] lines = cleaner.clean(schema.split(NEW_LINE));

        Table table = new Table(extractor.extractName(lines[0]));
        addTableColumns(lines, table);

        return table;
    }


    private void addTableColumns(final String[] lines, final Table table) {
        for (int index=1; index < lines.length; index++) {
            String aLine = lines[index];

            if (isColumn(aLine)) {
                String[] colSpec = extractor.extractColumn(aLine);
                table.addColumn(colSpec[0], colSpec[1]);
            }
        }
    }

    private boolean isColumn(final String aLine) {
        return !aLine.startsWith(PRIMARY_KEY);
    }
}
