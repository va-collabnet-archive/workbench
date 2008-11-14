package org.dwfa.mojo.relformat.mojo.sql.parser;

import java.util.ArrayList;
import java.util.List;

public final class Table {

    private final String name;
    private final List<TableColumn> columns;
    private String compositeKey;

    public Table(final String name) {
        this.name = name;
        columns = new ArrayList<TableColumn>();
    }

    public void addColumn(final String columnName, final String type) {
        columns.add(new TableColumn(columnName, type));
    }

    public String getName() {
        return name;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setCompositeKey(final String compositeKey) {
        this.compositeKey = compositeKey;
    }

    public boolean hasCompositeKey() {
        return compositeKey != null;
    }

    public String getCompositeKey() {
        return compositeKey;
    }

    public static Table nullTable() {
        return new Table("");
    }
}
