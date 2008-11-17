package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.File;

public interface FileNameExtractor {

    String extractFileName(Table table, File file);
}
