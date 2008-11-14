package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public interface LineValueToSQLTypeConverter {

    String DEFAULT_CONVERTER   = "DEFAULT";
    final String NULL           = "NULL";
    
    String[] convert(Table table, String[] values);
}
