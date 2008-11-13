package org.dwfa.mojo.relformat.mojo.sql.builder;

import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractorImpl;
import org.dwfa.mojo.relformat.mojo.sql.SQLCreatorImpl;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractorImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.SQLFileWriter;
import org.dwfa.mojo.relformat.mojo.sql.io.SQLFileWriterImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;

public final class SQLFileWriterBuilder {

    private FileUtil fileUtil;

    public SQLFileWriterBuilder(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public SQLFileWriter build() {
        LineToSQLConverter lineToSQLConverter =
                new LineToSQLConverterImpl(
                        new LineToValuesExtractorImpl(),
                        new LineValueToSQLTypeConverterImpl(),
                        new SQLCreatorImpl());
        return new SQLFileWriterImpl(lineToSQLConverter, new FileNameExtractorImpl(), fileUtil);
    }
}
