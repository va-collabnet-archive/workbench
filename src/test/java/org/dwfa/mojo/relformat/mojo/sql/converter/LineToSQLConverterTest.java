package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.SQLCreatorImpl;
import org.dwfa.mojo.relformat.mojo.sql.SQLLineBuilder;
import org.dwfa.mojo.relformat.mojo.sql.TableBuilder;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractorImpl;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class LineToSQLConverterTest {

    private LineToSQLConverter converter;

    @Before
    public void setup() {
        converter = new LineToSQLConverterImpl(new LineToValuesExtractorImpl(),
                new LineValueToSQLTypeConverterImpl(), new SQLCreatorImpl());
    }

    @Test
    public void shouldConvertALine() {
        String line = new LineBuilder().defaults();
        String expectedSQL = new SQLLineBuilder().defaults();
        Table table = new TableBuilder().defaults();

        String sql = converter.convert(table, line);
        assertThat(sql, equalTo(expectedSQL));
    }
}
