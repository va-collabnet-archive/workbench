package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class TimestampSQLTypeConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new TimestampSQLTypeConverter(new SQLTimeStampConverterImpl());
    }
    
    @Test
    public void shouldConvertATimestamp() {
        String timestamp = converter.convert("2001-12-01 15:00:00.000000");
        assertThat(timestamp, equalTo("TIMESTAMP('2001-12-01 15:00:00.000000')"));
    }
}
