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
package org.dwfa.mojo.relformat.mojo.sql.extractor;

import org.dwfa.mojo.relformat.mojo.sql.converter.LineBuilder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class LineToValuesExtractorTest {

    private LineToValuesExtractor extractor;

    @Before
    public void setup() {
        extractor = new LineToValuesExtractorImpl();
    }

    @Test
    public void shouldExtractALine() {
        String line = new LineBuilder().
                        addValue("2011000036100").
                        addValue("32506021000036107").
                        addValue("20081031T000000Z").
                        addValue("30430011000036108").
                        build();

        String[] values = extractor.extract(line);
        assertThat(values.length, equalTo(4));
        assertThat(values[0], equalTo("2011000036100"));
        assertThat(values[1], equalTo("32506021000036107"));
        assertThat(values[2], equalTo("20081031T000000Z"));
        assertThat(values[3], equalTo("30430011000036108"));
    }
    
    @Test
    public void shouldExtractALineWithMissingData() {
        String line = new LineBuilder().
                        addValue("2011000036100").
                        addValue("0").
                        addValue("current (active status type)").
                        addBlankValue().
                        addBlankValue().
                        addValue("1").
                        build();
        
        String[] values = extractor.extract(line);
        assertThat(values.length, equalTo(6));
        assertThat(values[0], equalTo("2011000036100"));
        assertThat(values[1], equalTo("0"));
        assertThat(values[2], equalTo("current (active status type)"));
        assertThat(values[3], equalTo(""));
        assertThat(values[4], equalTo(""));
        assertThat(values[5], equalTo("1"));
    }
}
