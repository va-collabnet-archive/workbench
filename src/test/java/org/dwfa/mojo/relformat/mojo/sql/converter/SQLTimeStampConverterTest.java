package org.dwfa.mojo.relformat.mojo.sql.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class SQLTimeStampConverterTest {

    private SQLTypeConverter converter;

    @Before
    public void setup() {
        converter = new SQLTimeStampConverterImpl();
    }

    @Test
    public void shouldConvertATimestamp() {
        String timestamp = converter.convert("2001-12-01 15:00:00.000000");
        assertThat(timestamp, equalTo("2001-12-01 15:00:00.000000"));
    }

    @Test
    public void shouldConvertATimezoneFormattedTimestamp() {
        String timestamp = converter.convert("20081031T000000Z");
        assertThat(timestamp, equalTo("2008-10-31 00:00:00.0"));
    }


    @Test
    public void shouldConvertATimestampWithADate() {
        String timestamp = converter.convert("20080731");
        assertThat(timestamp, equalTo("2008-07-31 00:00:00.0"));
    }

}
