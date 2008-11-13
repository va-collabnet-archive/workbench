package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public interface SQLCreator {

    String createSQL(final Table table, final String[] values);
}
