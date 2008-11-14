package org.dwfa.mojo.relformat.mojo.sql.builder;

import org.dwfa.mojo.relformat.mojo.converter.builder.FileNameExtractorBuilder;
import org.dwfa.mojo.relformat.mojo.sql.io.SQLFileWriter;
import org.dwfa.mojo.relformat.mojo.sql.io.SQLFileWriterImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;

public final class SQLFileWriterBuilder {

    private FileUtil fileUtil;
    private FileNameExtractorBuilder fileNameExtractorBuilder;

    public SQLFileWriterBuilder(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public SQLFileWriterBuilder withFileNameExtractor(final FileNameExtractorBuilder fileNameExtractorBuilder) {
        this.fileNameExtractorBuilder = fileNameExtractorBuilder;
        return this;
    }

    public SQLFileWriterBuilder withDefaults() {
        fileNameExtractorBuilder = new FileNameExtractorBuilder().withExtension(".sql");
        return this;
    }

    public SQLFileWriter build() {
        return new SQLFileWriterImpl(fileNameExtractorBuilder.build(), fileUtil);
    }
}
