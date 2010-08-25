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
package org.dwfa.mojo.relformat.mojo.sql.parser;

import java.util.ArrayList;
import java.util.List;

public final class Table {

    private final String name;
    private final List<TableColumn> columns;
    private String compositeKey;

    public Table(final String name) {
        this.name = name;
        columns = new ArrayList<TableColumn>();
    }

    public void addColumn(final String columnName, final String type) {
        columns.add(new TableColumn(columnName, type));
    }

    public String getName() {
        return name;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setCompositeKey(final String compositeKey) {
        this.compositeKey = compositeKey;
    }

    public boolean hasCompositeKey() {
        return compositeKey != null;
    }

    public String getCompositeKey() {
        return compositeKey;
    }

    public static Table nullTable() {
        return new Table("");
    }
}
