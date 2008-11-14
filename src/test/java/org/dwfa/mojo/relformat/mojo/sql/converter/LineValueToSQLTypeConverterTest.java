package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.builder.LineValueToSQLTypeConverterBuilder;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.hamcrest.core.IsEqual;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class LineValueToSQLTypeConverterTest {

    private LineValueToSQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new LineValueToSQLTypeConverterBuilder().build();
    }

    @Test
    public void shouldConvertValues() {
        String[] values = {"100", "", "Testing", "3", "A", "2008-11-06 15:55:33", "2008-11-11", "10:35:46"};

        Table table = new Table("Blah");
        table.addColumn("CONCEPTID", "BIGINT");
        table.addColumn("AUTHOR", "VARCHAR");
        table.addColumn("DESCRIPTION", "VARCHAR");
        table.addColumn("ISPRIMITIVE", "INT");
        table.addColumn("INDICATOR", "CHAR");
        table.addColumn("EFFECTIVE_TS", "TIMESTAMP");
        table.addColumn("CURRENT_DATE", "DATE");
        table.addColumn("FREQ", "TIME");

        String[] convertedValues = converter.convert(table, values);
        assertThat(convertedValues, IsEqual.equalTo(createExpectedValues()));
    }

    private String[] createExpectedValues() {
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
}
