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

import java.util.Arrays;
import java.util.List;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.builder.TableCacheBuilder;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.Table;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.TableColumn;
import org.ihtsdo.mojo.mojo.relformat.xml.ReleaseConfig;
import org.ihtsdo.mojo.mojo.relformat.xml.ReleaseFormat;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;

public final class TableCacheTest {

    private static final String FORMAT_TYPE = "CURRENT_CONCEPT_TYPE";

    private TableCache cache;

    @Before
    public void setup() {
        cache = new TableCacheBuilder().build();
    }

    @Test
    public void shouldReturnACacheFile() {
        cache.cache(buildReleaseConfig(buildSchema()));

        Table table = cache.getTable(FORMAT_TYPE);
        assertThat(table.getName(), IsEqual.equalTo("CURRENT_CONCEPT"));

        List<TableColumn> tableColumns = table.getColumns();
        assertThat(tableColumns.size(), equalTo(2));
        expectColumn(tableColumns.get(0), "ID", "INT");
        expectColumn(tableColumns.get(1), "DESCRIPTION", "VARCHAR");
    }

    @Test
    public void shouldThrowAnExceptionForAnUncachedFile() {
        try {
            cache.getTable(FORMAT_TYPE);
        } catch (TableCacheException e) {
            assertThat(e.getMessage(), equalTo("Could not retrieve table for format: CURRENT_CONCEPT_TYPE"));
        }
    }

    private ReleaseConfig buildReleaseConfig(final String schema) {
        ReleaseFormat releaseFormat = new ReleaseFormat(FORMAT_TYPE, schema);
        return new ReleaseConfig(Arrays.asList(releaseFormat));
    }

    private void expectColumn(final TableColumn tableColumn, final String name, final String type) {
        assertThat(tableColumn.getName(), equalTo(name));
        assertThat(tableColumn.getType(), equalTo(type));
    }

    private String buildSchema() {
        return new SchemaBuilder().createTable()
            .withName("CURRENT_CONCEPT")
            .createColumn()
            .withName("ID")
            .withType("INT(10)")
            .addToTable()
            .createColumn()
            .withName("DESCRIPTION")
            .withType("VARCHAR(30)")
            .addToTable()
            .addToSchema()
            .build();
    }
}
