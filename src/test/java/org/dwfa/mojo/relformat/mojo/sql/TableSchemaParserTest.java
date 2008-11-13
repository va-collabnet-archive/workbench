package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableColumn;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableSchemaParser;
import org.dwfa.mojo.relformat.mojo.sql.parser.TableSchemaParserImpl;
import org.dwfa.mojo.relformat.util.StringArrayCleaner;
import org.dwfa.mojo.relformat.util.StringArrayCleanerImpl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public final class TableSchemaParserTest {

    private TableSchemaParser parser;

    @Before
    public void setup() {
        StringArrayCleaner stringArrayCleaner = new StringArrayCleanerImpl();
        ColumnTypeCleaner columnTypeCleaner = new ColumnTypeCleanerImpl();
        TableDataExtractor extractor = new TableDataExtractorImpl(stringArrayCleaner, columnTypeCleaner);

        parser = new TableSchemaParserImpl(extractor, stringArrayCleaner);
    }

    @Test
    public void shouldParseASchema() {
        String schema = new SchemaBuilder().
                createTable().withName("current_concepts").
                    createColumn().withName("CONCEPTID").withType("BIGINT(20)").isMandatory().isPrimaryKey().
                    addToTable().
                    createColumn().withName("CONCEPTSTATUS").withType("INT(2)").isMandatory().
                    addToTable().
                    createColumn().withName("FULLYSPECIFIEDNAME").withType("VARCHAR(255)").isMandatory().
                    addToTable().
                    createColumn().withName("CTV3ID").withType("VARCHAR(5)").
                    addToTable().
                    createColumn().withName("SNOMEDID").withType("VARCHAR(8)").
                    addToTable().
                    createColumn().withName("ISPRIMITVE").withType("INT(1)").
                    addToTable().
                addToSchema().
                build();

        Table table = parser.parse(schema);
        assertThat(table.getName(), equalTo("current_concepts"));

        List<TableColumn> tableColumns = table.getColumns();
        assertThat(tableColumns.size(), equalTo(6));

        expectTableColumn(tableColumns.get(0), "CONCEPTID", "BIGINT");
        expectTableColumn(tableColumns.get(1), "CONCEPTSTATUS", "INT");
        expectTableColumn(tableColumns.get(2), "FULLYSPECIFIEDNAME", "VARCHAR");
        expectTableColumn(tableColumns.get(3), "CTV3ID", "VARCHAR");
        expectTableColumn(tableColumns.get(4), "SNOMEDID", "VARCHAR");
        expectTableColumn(tableColumns.get(5), "ISPRIMITVE", "INT");
    }
    
    @Test
    public void shouldParseASchemaWithPadding() {
        String schema = new SchemaBuilder().
                createTable().withName("   arf_uuid_descriptions     ").
                    createColumn().withName("    ID").withType("VARCHAR(30)").isMandatory().
                    addToTable().
                    createColumn().withName("  PATH_ID").withType("     VARCHAR(30)       ").isMandatory().
                    addToTable().
                    createColumn().withName("EFFECTIVE_DATE      ").withType("TIMESTAMP             ").isMandatory().
                    addToTable().
                    createColumn().withName("  ACTIVE").withType("VARCHAR(30)").isMandatory().
                    addToTable().
                    createColumn().withName("CONCEPT_ID        ").withType("      VARCHAR(30)").isMandatory().
                    addToTable().
                    createColumn().withName("                 TERM").withType("VARCHAR(500)").isMandatory().
                    addToTable().
                    createColumn().withName("TYPE_ID").withType("VARCHAR(30)").isMandatory().
                    addToTable().
                    createColumn().withName("             LANGUAGE_ID").withType("VARCHAR(30)").isMandatory().
                    addToTable().
                    createColumn().withName("CASE_SENSITIVITY_ID").withType("            VARCHAR(30)").isMandatory().
                    addToTable().
                setCompositKeyOn("ID, PATH_ID, EFFECTIVE_DATE").
                addToSchema().
                build();

        Table table = parser.parse(schema);
        assertThat(table.getName(), equalTo("arf_uuid_descriptions"));
        List<TableColumn> tableColumns = table.getColumns();
        assertThat(tableColumns.size(), equalTo(9));

        expectTableColumn(tableColumns.get(0), "ID", "VARCHAR");
        expectTableColumn(tableColumns.get(1), "PATH_ID", "VARCHAR");
        expectTableColumn(tableColumns.get(2), "EFFECTIVE_DATE", "TIMESTAMP");
        expectTableColumn(tableColumns.get(3), "ACTIVE", "VARCHAR");
        expectTableColumn(tableColumns.get(4), "CONCEPT_ID", "VARCHAR");
        expectTableColumn(tableColumns.get(5), "TERM", "VARCHAR");
        expectTableColumn(tableColumns.get(6), "TYPE_ID", "VARCHAR");
        expectTableColumn(tableColumns.get(7), "LANGUAGE_ID", "VARCHAR");
        expectTableColumn(tableColumns.get(8), "CASE_SENSITIVITY_ID", "VARCHAR");
    }

    private void expectTableColumn(final TableColumn tableColumn, final String name, final String type) {
        assertThat(tableColumn.getName(), equalTo(name));
        assertThat(tableColumn.getType(), equalTo(type));
    }
}
