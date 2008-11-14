package org.dwfa.mojo.relformat.mojo.converter.builder;

import org.dwfa.mojo.relformat.mojo.converter.DerbyLineCreator;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractorImpl;

public final class LineToDerbyLineConverterBuilder {

    public LineToSQLConverter build() {
        return new LineToSQLConverterImpl(
                        new LineToValuesExtractorImpl(),
                        new DerbyLineValueToSQLTypeConverterBuilder().build(),
                        new DerbyLineCreator());
    }
}
