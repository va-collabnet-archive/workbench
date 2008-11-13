package org.dwfa.mojo.relformat.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Column")
public class Column {

    private final String schemaName;
    private final String exportName;

    public Column(final String schemaName, final String exportName) {
        this.schemaName = schemaName;
        this.exportName = exportName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getExportName() {
        return exportName;
    }
}
