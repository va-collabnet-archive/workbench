package org.dwfa.mojo.relformat.mojo.converter;

import static org.dwfa.mojo.relformat.mojo.sql.parser.Table.nullTable;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class DerbyLineCreatorTest {
    
    private DerbyLineCreator derbyLineCreator;

    @Before
    public void setup() {
        derbyLineCreator = new DerbyLineCreator();
    }
    
    @Test
    public void shouldCreateADerbyLine() {
        String derbyLine = derbyLineCreator.createSQL(nullTable(), new String[]{"1", "Message", "NULL",
                "2008-10-11 11:23:22.0"});

        String expectedDerbyLine = new DerbyLineBuilder().
                addValue("1").
                addValue("Message").
                addBlank().
                addValue("2008-10-11 11:23:22.0").
                build();

        assertThat(derbyLine, equalTo(expectedDerbyLine));
    }
}
