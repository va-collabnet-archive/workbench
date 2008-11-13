package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.util.StringArrayCleanerImpl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class TableDataExtractorWithColumnTest {

    private TableDataExtractor extractor;

    @Before
    public void setup() {
        extractor = new TableDataExtractorImpl(new StringArrayCleanerImpl(), new ColumnTypeCleanerImpl());
    }

    @Test
    public void shouldExtractAColumn() {
        String[] columnSpec = extractor.extractColumn("ID VARCHAR(30),");
        assertThat(columnSpec.length, equalTo(2));
        assertThat(columnSpec[0], equalTo("ID"));
        assertThat(columnSpec[1], equalTo("VARCHAR"));
    }

    @Test
    public void shouldExtractAColumnWithPadding() {
        String[] columnSpec = extractor.extractColumn(" ID   VARCHAR(30)  , ");
        assertThat(columnSpec.length, equalTo(2));
        assertThat(columnSpec[0], equalTo("ID"));
        assertThat(columnSpec[1], equalTo("VARCHAR"));
    }
}
