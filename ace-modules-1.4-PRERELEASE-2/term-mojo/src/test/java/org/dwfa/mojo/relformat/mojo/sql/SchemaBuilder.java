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

import org.dwfa.mojo.relformat.util.SystemPropertyReaderImpl;

import java.util.ArrayList;
import java.util.List;

public final class SchemaBuilder {

    private static final String lineSeparator = new SystemPropertyReaderImpl().getLineSeparator();

    private final List<TableBuilder> tableBuilders;

    public SchemaBuilder() {
        tableBuilders = new ArrayList<TableBuilder>();
    }

    public TableBuilder createTable() {
        TableBuilder builder = new TableBuilder(this);
        tableBuilders.add(builder);
        return builder;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();

        for (TableBuilder tableBuilder : tableBuilders) {
            builder.append(tableBuilder.build()).append(lineSeparator);
        }

        return builder.toString();
    }

    public static class TableBuilder {

        private SchemaBuilder parent;
        private String tableName;
        private String compositeKey;
        private List<ColumnBuilder> columnBuilders;

        TableBuilder(final SchemaBuilder parent) {
            this.parent = parent;
            columnBuilders = new ArrayList<ColumnBuilder>();
        }

        public TableBuilder withName(final String tableName) {
            this.tableName = tableName;
            return this;
        }

        public ColumnBuilder createColumn() {
            ColumnBuilder columnBuilder = new ColumnBuilder(this);
            columnBuilders.add(columnBuilder);
            return columnBuilder;
        }

        public TableBuilder setCompositKeyOn(final String compositeKey) {
            this.compositeKey = compositeKey;
            return this;
        }

        public SchemaBuilder addToSchema() {
            return parent;
        }

        public String build() {
            StringBuilder builder = new StringBuilder();

            builder.append("create table ").append(tableName).append(" (").append(lineSeparator);

            for (int x = 0; x < columnBuilders.size(); x++) {
                ColumnBuilder columnBuilder = columnBuilders.get(x);

                builder.append(columnBuilder.build());

                if (x < (columnBuilders.size() - 1)) {
                    builder.append(",").append(lineSeparator);
                }
            }

            if (compositeKey != null) {
                builder.append("PRIMARY KEY(").append(compositeKey).append(")");
            }

            builder.append(");").append(lineSeparator);

            return builder.toString();
        }
    }

    public static class ColumnBuilder {

        private TableBuilder parent;
        private String columnName;
        private String columnType;
        private boolean mandatory;
        private boolean primaryKey;

        public ColumnBuilder(final SchemaBuilder.TableBuilder parent) {
            this.parent = parent;
        }

        public ColumnBuilder withName(final String columnName) {
            this.columnName = columnName;
            return this;
        }

        public ColumnBuilder withType(final String columnType) {
            this.columnType = columnType;
            return this;
        }

        public ColumnBuilder isMandatory() {
            this.mandatory = true;
            return this;
        }

        public ColumnBuilder isPrimaryKey() {
            this.primaryKey = true;
            return this;
        }

        public TableBuilder addToTable() {
            return parent;
        }

        public String build() {
            StringBuilder builder = new StringBuilder();
            builder.append(columnName).append(" ").append(columnType);

            if (mandatory) {
                builder.append(" NOT NULL");
            }

            if (primaryKey) {
                builder.append(" PRIMARY KEY");
            }

            return builder.toString();
        }
    }
}
