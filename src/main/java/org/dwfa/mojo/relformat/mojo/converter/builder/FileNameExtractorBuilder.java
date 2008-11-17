package org.dwfa.mojo.relformat.mojo.converter.builder;

import org.dwfa.mojo.relformat.mojo.sql.DerbyImportFileNameExtractor;
import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractor;
import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractorImpl;

public final class FileNameExtractorBuilder {

    private String extension;
    private boolean genericExporter = true;

    public FileNameExtractorBuilder withExtension(final String extension) {
        this.extension = extension;
        return this;
    }

    public FileNameExtractorBuilder withGenericExporter() {
        this.genericExporter = true;
        return this;
    }

    public FileNameExtractorBuilder withDerbyExporter() {
        this.genericExporter = false;
        return this;
    }

    public FileNameExtractor build() {
        if (genericExporter) {
            return new FileNameExtractorImpl(extension);
        }

        return new DerbyImportFileNameExtractor(new FileNameExtractorImpl(extension));

    }
}
