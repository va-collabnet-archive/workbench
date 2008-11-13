package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.SQLCreator;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractor;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public final class LineToSQLConverterImpl implements LineToSQLConverter {

    private final LineToValuesExtractor lineToValuesExtractor;
    private final LineValueToSQLTypeConverter lineValueToSQLTypeConverter;
    private final SQLCreator sqlCreator;


    public LineToSQLConverterImpl(final LineToValuesExtractor lineToValuesExtractor,
                                  final LineValueToSQLTypeConverter lineValueToSQLTypeConverter,
                                  final SQLCreator sqlCreator) {
        this.lineToValuesExtractor = lineToValuesExtractor;
        this.lineValueToSQLTypeConverter = lineValueToSQLTypeConverter;
        this.sqlCreator = sqlCreator;
    }

    public String convert(final Table table, final String line) {
        String[] values = lineToValuesExtractor.extract(line);
        String[] convertedValues = lineValueToSQLTypeConverter.convert(table, values);
        return sqlCreator.createSQL(table, convertedValues);
    }
}
