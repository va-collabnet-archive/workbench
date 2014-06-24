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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.builder;

import static org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverter.DEFAULT_CONVERTER;

import java.util.HashMap;
import java.util.Map;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.DateSQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.GenericSQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverterImpl;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.SQLTimeStampConverterImpl;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.SQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.StringSQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.TimeSQLTypeConverter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.converter.TimestampSQLTypeConverter;

public final class LineValueToSQLTypeConverterBuilder {

    private static final String VARCHAR_TYPE = "VARCHAR";
    private static final String CHAR_TYPE = "CHAR";
    private static final String TIMESTAMP_TYPE = "TIMESTAMP";
    private static final String TIME_TYPE = "TIME";
    private static final String DATE_TYPE = "DATE";

    public LineValueToSQLTypeConverter build() {
        Map<String, SQLTypeConverter> converterMap = new HashMap<String, SQLTypeConverter>();
        StringSQLTypeConverter stringConverter = new StringSQLTypeConverter();
        converterMap.put(VARCHAR_TYPE, stringConverter);
        converterMap.put(CHAR_TYPE, stringConverter);
        converterMap.put(TIMESTAMP_TYPE, new TimestampSQLTypeConverter(new SQLTimeStampConverterImpl()));
        converterMap.put(TIME_TYPE, new TimeSQLTypeConverter());
        converterMap.put(DATE_TYPE, new DateSQLTypeConverter());
        converterMap.put(DEFAULT_CONVERTER, new GenericSQLTypeConverter());

        return new LineValueToSQLTypeConverterImpl(converterMap);
    }
}
