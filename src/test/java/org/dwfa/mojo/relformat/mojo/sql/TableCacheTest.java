package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.builder.TableCacheBuilder;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableColumn;
import org.dwfa.mojo.relformat.xml.ReleaseConfig;
import org.dwfa.mojo.relformat.xml.ReleaseFormat;
import org.hamcrest.core.IsEqual;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
        return new SchemaBuilder().
                        createTable().withName("CURRENT_CONCEPT").
                        createColumn().withName("ID").withType("INT(10)").addToTable().
                        createColumn().withName("DESCRIPTION").withType("VARCHAR(30)").addToTable().
                   addToSchema().build();
    }
}
