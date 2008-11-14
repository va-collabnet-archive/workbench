package org.dwfa.mojo.relformat.mojo.sql.builder;

import org.dwfa.mojo.relformat.mojo.sql.SQLCreatorImpl;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractorImpl;

public final class LineToSQLConverterBuilder {

    public LineToSQLConverter build() {
        return new LineToSQLConverterImpl(
                        new LineToValuesExtractorImpl(),
                        new LineValueToSQLTypeConverterBuilder().build(),
                        new SQLCreatorImpl());        
    }
}
