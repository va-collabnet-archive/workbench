package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class GenericSQLTypeConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new GenericSQLTypeConverter();
    }

    @Test
    public void shouldConvertAnInt() {
        assertThat(converter.convert("1"), equalTo("1"));
    }
    
    @Test
    public void shouldConvertAnDecimal() {
        assertThat(converter.convert("5.3"), equalTo("5.3"));
    }

    
}
