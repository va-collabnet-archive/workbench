package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.File;

public final class FileNameExtractorImpl implements FileNameExtractor {

    private int count = 1;
    private final String extention;

    public FileNameExtractorImpl(final String extention) {
        this.extention = extention;
    }

    public String extractFileName(final Table table, final File file) {
        String name = file.getName();

        return new StringBuilder().
                append(name.substring(0, name.lastIndexOf("."))).
                append("-").append(count++).
                append(extention).toString();
    }
}
