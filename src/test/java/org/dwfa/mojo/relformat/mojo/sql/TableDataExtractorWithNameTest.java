package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.util.StringArrayCleanerImpl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public final class TableDataExtractorWithNameTest {

    private TableDataExtractor extractor;

    @Before
    public void setup() {
        extractor = new TableDataExtractorImpl(new StringArrayCleanerImpl(), new ColumnTypeCleanerImpl());
    }

    @Test
    public void shouldExtractATableName() {
        String tableName = extractor.extractName("create table MyTableName");
        assertThat(tableName, equalTo("MyTableName"));
    }

    @Test
    public void shouldExtractATableNameWithPadding() {
        String tableName = extractor.extractName("  create   table    PeriodicTable   ");
        assertThat(tableName, equalTo("PeriodicTable"));        
    }

    @Test
    public void shouldThrowAnExceptionIfTheTableNameIsAbsent() {
        try {
            extractor.extractName("create table ");
            fail();
        } catch (TableDataExtractorException e) {
            assertThat(e.getMessage(), equalTo("The table name could not be extracted from: [create table ]"));
        }
    }
}
