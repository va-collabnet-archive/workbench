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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.ihtsdo.mojo.mojo.relformat.util.StringArrayCleanerImpl;
import org.junit.Before;
import org.junit.Test;

public final class TableDataExtractorWithColumnTest {

    private TableDataExtractor extractor;

    @Before
    public void setup() {
        extractor = new TableDataExtractorImpl(new StringArrayCleanerImpl(), new ColumnTypeCleanerImpl());
    }

    @Test
    public void shouldExtractAColumn() {
        String[] columnSpec = extractor.extractColumn("ID VARCHAR(30),");
        assertThat(columnSpec.length, equalTo(2));
        assertThat(columnSpec[0], equalTo("ID"));
        assertThat(columnSpec[1], equalTo("VARCHAR"));
    }

    @Test
    public void shouldExtractAColumnWithPadding() {
        String[] columnSpec = extractor.extractColumn(" ID   VARCHAR(30)  , ");
        assertThat(columnSpec.length, equalTo(2));
        assertThat(columnSpec[0], equalTo("ID"));
        assertThat(columnSpec[1], equalTo("VARCHAR"));
    }
}
