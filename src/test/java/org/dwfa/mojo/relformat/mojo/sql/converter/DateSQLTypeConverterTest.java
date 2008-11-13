package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class DateSQLTypeConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new DateSQLTypeConverter();
    }

    @Test
    public void shouldConvertADate() {
        String date = converter.convert("2008-11-06");
        assertThat(date, equalTo("DATE('2008-11-06')"));
    }
}
