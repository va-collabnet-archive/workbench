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

import java.io.File;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.Table;
import org.junit.Before;
import org.junit.Test;

public final class FileNameExtractorWithDifferentExtentionsTest {

    private Table table;

    @Before
    public void setup() {
        table = Table.nullTable();
    }

    @Test
    public void shouldCreateATxtFileExtention() {

        FileNameExtractor extractor = new FileNameExtractorImpl(".txt");
        String fileName = extractor.extractFileName(table, new File(
            "/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.txt"));
        assertThat(fileName, equalTo("arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT-1.txt"));
    }

    @Test
    public void shouldCreateAPropertiesFileExtention() {
        FileNameExtractor extractor = new FileNameExtractorImpl(".properties");
        String fileName = extractor.extractFileName(table, new File(
            "/root/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5.txt"));
        assertThat(fileName, equalTo("arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-1.properties"));
    }
}
