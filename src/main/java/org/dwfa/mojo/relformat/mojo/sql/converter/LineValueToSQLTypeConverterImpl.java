package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableColumn;

import java.util.Map;

public final class LineValueToSQLTypeConverterImpl implements LineValueToSQLTypeConverter {

    private final Map<String, SQLTypeConverter> converterMap;

    public LineValueToSQLTypeConverterImpl(final Map<String, SQLTypeConverter> converterMap) {
        this.converterMap = converterMap;
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
