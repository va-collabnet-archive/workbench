package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public final class TableBuilder {

    public Table defaults() {
        Table table = new Table("Special");
        table.addColumn("PID", "INT");
        table.addColumn("REASON", "VARCHAR");
        table.addColumn("DESCRIPTION", "VARCHAR");
        table.addColumn("COUNT", "INT");
        table.addColumn("STATUS", "CHAR");
        table.addColumn("EFFECTIVE_TS", "TIMESTAMP");
        table.addColumn("EFFECTIVE_DATE", "DATE");
        table.addColumn("EFFECTIVE_TIME", "TIME");
        return table;        
    }
}
