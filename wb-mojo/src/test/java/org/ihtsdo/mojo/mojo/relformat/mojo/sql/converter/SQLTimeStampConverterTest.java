/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter;

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
