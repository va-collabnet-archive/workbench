package org.dwfa.mojo.relformat.mojo.sql;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class ColumnTypeCleanerTest {

    private ColumnTypeCleaner cleaner;

    @Before
    public void setup() {
        cleaner = new ColumnTypeCleanerImpl();
    }

    @Test
    public void shouldCleanTrialingCommas() {
        String cleaned = cleaner.clean("VARCHAR(30),");
        assertThat(cleaned, equalTo("VARCHAR(30)"));
    }

    @Test
    public void shouldCleanTrailingClosingStatements() {
        String cleaned = cleaner.clean("INT(3));");
        assertThat(cleaned, equalTo("INT(3)"));
    }    
}
