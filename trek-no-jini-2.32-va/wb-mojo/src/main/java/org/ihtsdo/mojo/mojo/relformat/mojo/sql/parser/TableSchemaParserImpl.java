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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.TableDataExtractor;
import org.ihtsdo.mojo.mojo.relformat.util.StringArrayCleaner;

public final class TableSchemaParserImpl implements TableSchemaParser {

    private static final String NEW_LINE = "(\\r\\n|\\n)";
    private static final String PRIMARY_KEY = "PRIMARY KEY";

    private final TableDataExtractor extractor;
    private final StringArrayCleaner cleaner;

    public TableSchemaParserImpl(final TableDataExtractor extractor, final StringArrayCleaner cleaner) {
        this.extractor = extractor;
        this.cleaner = cleaner;
    }

    public Table parse(final String schema) {
        String[] lines = cleaner.clean(schema.split(NEW_LINE));

        Table table = new Table(extractor.extractName(lines[0]));
        addTableColumns(lines, table);

        return table;
    }

    private void addTableColumns(final String[] lines, final Table table) {
        for (int index = 1; index < lines.length; index++) {
            String aLine = lines[index];

            if (isColumn(aLine)) {
                String[] colSpec = extractor.extractColumn(aLine);
                table.addColumn(colSpec[0], colSpec[1]);
            }
        }
    }

    private boolean isColumn(final String aLine) {
        return !aLine.startsWith(PRIMARY_KEY);
    }
}
