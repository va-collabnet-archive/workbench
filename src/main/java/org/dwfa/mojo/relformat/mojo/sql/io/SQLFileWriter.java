package org.dwfa.mojo.relformat.mojo.sql.io;

import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.File;

public interface SQLFileWriter {

    void writer(File file, Table table, String outputDirectory, LineToSQLConverter lineToSQLConverter);
}
