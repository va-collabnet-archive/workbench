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
package org.ihtsdo.mojo.mojo.relformat.mojo.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.SQLTypeConverter;
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