package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public interface LineToSQLConverter {

    String convert(Table table, String line);
}
