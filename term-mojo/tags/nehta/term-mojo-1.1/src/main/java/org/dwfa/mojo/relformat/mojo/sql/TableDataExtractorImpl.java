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

import org.dwfa.mojo.relformat.util.StringArrayCleaner;

public final class TableDataExtractorImpl implements TableDataExtractor {

    private static final String SPACE = "(\\s|\\(|\\))";

    private final StringArrayCleaner cleaner;
    private final ColumnTypeCleaner columnTypeCleaner;

    public TableDataExtractorImpl(final StringArrayCleaner cleaner,
            final ColumnTypeCleaner columnTypeCleaner) {
        this.cleaner = cleaner;
        this.columnTypeCleaner = columnTypeCleaner;
    }

    public String extractName(final String text) {
        try {
            return cleaner.clean(text.split(SPACE))[2];
        } catch (Exception e) {
            throw new TableDataExtractorException(
                "The table name could not be extracted from: [" + text + "]", e);
        }
    }

    public String[] extractColumn(final String text) {
        String[] columnSpec = cleaner.clean(text.split(SPACE));
        columnSpec[1] = columnTypeCleaner.clean(columnSpec[1]);
        return new String[] { columnSpec[0], columnSpec[1] };
    }
}
