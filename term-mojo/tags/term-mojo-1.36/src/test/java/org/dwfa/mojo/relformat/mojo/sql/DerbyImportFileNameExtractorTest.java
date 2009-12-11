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
package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

import java.io.File;

public final class DerbyImportFileNameExtractorTest {
    
    @Test
    public void shouldPrependATableNameToTheFileName() {
        FileNameExtractor extractor = new DerbyImportFileNameExtractor(new FileNameExtractorImpl(".txt"));
        assertThat(extractor.extractFileName(new Table("my_table_name"), new File(
                "/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.sql")),
                equalTo("my_table_name-arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT-1.txt"));
    }

    @Test
    public void shouldPrependAnotherTableNameToTheFileName() {
        FileNameExtractor extractor = new DerbyImportFileNameExtractor(new FileNameExtractorImpl(".sql"));
        assertThat(extractor.extractFileName(new Table("yet_another_table_name"), new File(
                "/somepath/parent/subset/arf_uuid_descriptions_au.gov.nehta.au-ct-release_1.2.dde")),
                equalTo("yet_another_table_name-arf_uuid_descriptions_au.gov.nehta.au-ct-release_1.2-1.sql"));
    }
}
