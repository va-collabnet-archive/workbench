package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableColumn;

import java.util.List;

public final class SQLCreatorImpl implements SQLCreator {

    public String createSQL(final Table table, final String[] values) {
        return new StringBuilder().append("INSERT INTO ").
                append(table.getName()).append(" (").append(buildColumnNames(table)).append(") VALUES (").
                append(buildValues(values)).append(");").toString();
    }

    private String buildValues(final String[] values) {
        return buildCommaDelimitedValues(values);
    }

    private String buildColumnNames(final Table table) {
        List<TableColumn> columns = table.getColumns();
        String[] columnNames = new String[columns.size()];

        for (int index=0; index < columns.size(); index++) {
            columnNames[index] = columns.get(index).getName();
        }

        return buildCommaDelimitedValues(columnNames);        
    }

    private String buildCommaDelimitedValues(final String[] values) {
        StringBuilder builder = new StringBuilder();

        for (int index=0; index < values.length; index++) {
            builder.append(values[index]);

            if (index < (values.length - 1)) {
                builder.append(", ");                
            }
        }

        return builder.toString();
    }
}
