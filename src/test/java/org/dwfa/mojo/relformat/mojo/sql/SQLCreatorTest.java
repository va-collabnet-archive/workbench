package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class SQLCreatorTest {

    private SQLCreator creator;

    @Before
    public void setup() {
        creator = new SQLCreatorImpl();
    }
    
    @Test
    public void shouldCreateSQLForValues() {
        Table table = buildTable();
        String[] values = buildSQLValues();

        String sql = creator.createSQL(table, values);
        assertThat(sql, equalTo(buildExpectedSQL()));
    }

    private String[] buildSQLValues() {
        return new String[]{
                "100",
                "NULL",
                "'Testing'",
                "3",
                "'A'",
                "TIMESTAMP('2008-11-06 15:55:33')",
                "DATE('2008-11-11')",
                "TIME('10:35:46')"};
    }

    private Table buildTable() {
        return new TableBuilder().defaults();
    }

    private String buildExpectedSQL() {
        return new SQLLineBuilder().defaults();
    }
}
