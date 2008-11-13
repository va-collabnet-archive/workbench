package org.dwfa.mojo.relformat.mojo.sql;

import java.io.File;

public final class FileNameExtractorImpl implements FileNameExtractor {

    private static final String SQL_EXTENTION = ".sql";

    private int count = 1;

    public String extractFileName(final File file) {
        String name = file.getName();

        return new StringBuilder().
                append(name.substring(0, name.lastIndexOf("."))).
                append("-").append(count++).
                append(SQL_EXTENTION).toString();
    }
}
