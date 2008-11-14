package org.dwfa.mojo.relformat.mojo.converter;

import org.dwfa.mojo.relformat.mojo.sql.converter.SQLTypeConverter;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class DerbyStringConverterTest {
    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new DerbyStringConverter();
    }

    @Test
    public void shouldEscapeQuaotes() {
        String escapedValue = converter.convert("6\" sub");
        assertThat(escapedValue, equalTo("6\"\" sub"));
    }
}
