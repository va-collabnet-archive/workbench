package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class TimeSQLTypeConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new TimeSQLTypeConverter();
    }

    @Test
    public void shouldConvertATime() {
        String time = converter.convert("15:54:22");
        assertThat(time, equalTo("TIME('15:54:22')"));
    }
}
