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

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.Table;
import org.junit.Before;
import org.junit.Test;

public final class SQLCreatorTest {

    private SQLCreator creator;

    @Before
    public void setup() {
        creator = new SQLCreatorImpl();
    }

    @Test
    public void shouldCreateSQLForValues() {
        Table table = buildTable();
        String[] values = buildSQLValues();

        String sql = creator.createSQL(table, values);
        assertThat(sql, equalTo(buildExpectedSQL()));
    }

    private String[] buildSQLValues() {
        return new String[] { "100", "NULL", "'Testing'", "3", "'A'", "TIMESTAMP('2008-11-06 15:55:33')",
                             "DATE('2008-11-11')", "TIME('10:35:46')" };
    }

    private Table buildTable() {
        return new TableBuilder().defaults();
    }

    private String buildExpectedSQL() {
        return new SQLLineBuilder().defaults();
    }
}
