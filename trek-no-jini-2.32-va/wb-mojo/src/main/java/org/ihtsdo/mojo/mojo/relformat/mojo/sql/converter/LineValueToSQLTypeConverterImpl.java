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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter;

import java.util.Map;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.Table;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.parser.TableColumn;

public final class LineValueToSQLTypeConverterImpl implements LineValueToSQLTypeConverter {

    private final Map<String, SQLTypeConverter> converterMap;

    public LineValueToSQLTypeConverterImpl(final Map<String, SQLTypeConverter> converterMap) {
        this.converterMap = converterMap;
    }

    public String[] convert(final Table table, final String[] values) {
        String[] convertedValues = new String[values.length];

        for (int index = 0; index < values.length; index++) {
            String value = values[index];
            convertedValues[index] = (value.length() == 0) ? NULL : getConverter(table, index).convert(value);
        }

        return convertedValues;
    }

    private SQLTypeConverter getConverter(final Table table, final int index) {
        TableColumn column = table.getColumns().get(index);
        if (converterMap.containsKey(column.getType())) {
            return converterMap.get(column.getType());
        }

        return converterMap.get(DEFAULT_CONVERTER);
    }
}
