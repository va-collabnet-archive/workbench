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

public final class ColumnTypeCleanerImpl implements ColumnTypeCleaner {

    private static final String TRAILING_COLUMN_SEPARATOR = ",";
    private static final String TRAILING_CLOSE_TABLE = ");";

    public String clean(final String column) {
        return removeClosingTable(removeTrailingComma(column));
    }

    private String removeClosingTable(final String type) {
        return type.endsWith(TRAILING_CLOSE_TABLE) ? type.substring(0, type
            .length()
            - TRAILING_CLOSE_TABLE.length()) : type;
    }

    private String removeTrailingComma(final String column) {
        return column.endsWith(TRAILING_COLUMN_SEPARATOR) ? column.substring(0,
            column.length() - TRAILING_COLUMN_SEPARATOR.length()) : column;
    }
}
