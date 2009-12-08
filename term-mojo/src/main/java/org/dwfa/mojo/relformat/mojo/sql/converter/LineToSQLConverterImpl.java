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
package org.dwfa.mojo.relformat.mojo.sql.converter;

import org.dwfa.mojo.relformat.mojo.sql.SQLCreator;
import org.dwfa.mojo.relformat.mojo.sql.extractor.LineToValuesExtractor;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public final class LineToSQLConverterImpl implements LineToSQLConverter {

    private final LineToValuesExtractor lineToValuesExtractor;
    private final LineValueToSQLTypeConverter lineValueToSQLTypeConverter;
    private final SQLCreator sqlCreator;

    public LineToSQLConverterImpl(final LineToValuesExtractor lineToValuesExtractor,
            final LineValueToSQLTypeConverter lineValueToSQLTypeConverter, final SQLCreator sqlCreator) {
        this.lineToValuesExtractor = lineToValuesExtractor;
        this.lineValueToSQLTypeConverter = lineValueToSQLTypeConverter;
        this.sqlCreator = sqlCreator;
    }

    public String convert(final Table table, final String line) {
        String[] values = lineToValuesExtractor.extract(line);
        String[] convertedValues = lineValueToSQLTypeConverter.convert(table, values);
        return sqlCreator.createSQL(table, convertedValues);
    }
}
