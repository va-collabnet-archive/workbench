package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class StringSQLTypeConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new StringSQLTypeConverter();
    }
    
    @Test
    public void shouldConvertAStringValue() {
        String converted = converter.convert("BooHoo");
        assertThat(converted, equalTo("'BooHoo'"));
    }
    
    @Test
    public void shouldEscapeQuotations() {
      String converted = converter.convert("Chrone's disease");
      assertThat(converted, equalTo("'Chrone''s disease'"));
    }
}
