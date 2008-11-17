package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.File;

public final class DerbyImportFileNameExtractor implements FileNameExtractor {

    private final FileNameExtractor fileNameExtractor;

    public DerbyImportFileNameExtractor(final FileNameExtractor fileNameExtractor) {
        this.fileNameExtractor = fileNameExtractor;
    }

    public String extractFileName(final Table table, final File file) {
        return table.getName() + "-" + fileNameExtractor.extractFileName(table, file);
    }
}
