package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableColumn;

import java.util.HashMap;
import java.util.Map;

public final class LineValueToSQLTypeConverterImpl implements LineValueToSQLTypeConverter {

    private final Map<String, SQLTypeConverter> converterMap;

    //TODO: MOve to an ENUM.
    private static final String DEFAULT_CONVERTER   = "DEFAULT";
    private static final String NULL                = "NULL";
    private static final String VARCHAR_TYPE        = "VARCHAR";
    private static final String CHAR_TYPE           = "CHAR";
    private static final String TIMESTAMP_TYPE      = "TIMESTAMP";
    private static final String TIME_TYPE           = "TIME";
    private static final String DATE_TYPE           = "DATE";

    public LineValueToSQLTypeConverterImpl() {
        converterMap = new HashMap<String, SQLTypeConverter>();
        StringSQLTypeConverter stringConverter = new StringSQLTypeConverter();
        converterMap.put(VARCHAR_TYPE, stringConverter);
        converterMap.put(CHAR_TYPE, stringConverter);
        converterMap.put(TIMESTAMP_TYPE, new TimestampSQLTypeConverter());
        converterMap.put(TIME_TYPE, new TimeSQLTypeConverter());
        converterMap.put(DATE_TYPE, new DateSQLTypeConverter());
        converterMap.put(DEFAULT_CONVERTER, new GenericSQLTypeConverter());
    }

    public String[] convert(final Table table, final String[] values) {
        String[] convertedValues = new String[values.length];

        for (int index=0; index < values.length; index++) {
            String value = values[index];
            convertedValues[index] = (value.length() == 0) ? NULL : getConverter(table, index).convert(value);
        }

        return convertedValues;
    }

    private SQLTypeConverter getConverter(final Table table, final int index) {
        TableColumn column = table.getColumns().get(index);
        if (converterMap.containsKey(column.getType())) {
            return converterMap.get(column.getType());
        }

        return converterMap.get(DEFAULT_CONVERTER);
    }
}
