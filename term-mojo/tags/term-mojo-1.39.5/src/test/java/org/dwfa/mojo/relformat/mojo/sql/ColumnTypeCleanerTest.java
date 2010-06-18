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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class ColumnTypeCleanerTest {

    private ColumnTypeCleaner cleaner;

    @Before
    public void setup() {
        cleaner = new ColumnTypeCleanerImpl();
    }

    @Test
    public void shouldCleanTrialingCommas() {
        String cleaned = cleaner.clean("VARCHAR(30),");
        assertThat(cleaned, equalTo("VARCHAR(30)"));
    }

    @Test
    public void shouldCleanTrailingClosingStatements() {
        String cleaned = cleaner.clean("INT(3));");
        assertThat(cleaned, equalTo("INT(3)"));
    }    
}
