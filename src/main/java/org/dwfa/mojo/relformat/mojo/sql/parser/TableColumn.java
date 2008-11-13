package org.dwfa.mojo.relformat.mojo.sql.parser;

public final class TableColumn {

    private final String name;
    private final String type;

    public TableColumn(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
