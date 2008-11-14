package org.dwfa.mojo.relformat.mojo.converter.builder;

import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractor;
import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractorImpl;

public final class FileNameExtractorBuilder {

    private String extension;

    public FileNameExtractorBuilder withExtension(final String extension) {
        this.extension = extension;
        return this;
    }

    public FileNameExtractorBuilder withDefaults() {
        extension = ".sql";
        return this;
    }

    public FileNameExtractor build() {
        return new FileNameExtractorImpl(extension);
    }
}
