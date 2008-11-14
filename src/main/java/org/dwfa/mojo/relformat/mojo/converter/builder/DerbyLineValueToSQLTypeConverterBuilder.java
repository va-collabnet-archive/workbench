package org.dwfa.mojo.relformat.mojo.converter.builder;

import org.dwfa.mojo.relformat.mojo.converter.DerbyStringConverter;
import org.dwfa.mojo.relformat.mojo.sql.converter.GenericSQLTypeConverter;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverter;
import static org.dwfa.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverter.DEFAULT_CONVERTER;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineValueToSQLTypeConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.converter.SQLTimeStampConverterImpl;
import org.dwfa.mojo.relformat.mojo.sql.converter.SQLTypeConverter;

import java.util.HashMap;
import java.util.Map;

public final class DerbyLineValueToSQLTypeConverterBuilder {

    private static final String VARCHAR_TYPE        = "VARCHAR";
    private static final String TIMESTAMP_TYPE      = "TIMESTAMP";

    public LineValueToSQLTypeConverter build() {
        Map<String, SQLTypeConverter> converterMap = new HashMap<String, SQLTypeConverter>();
        converterMap.put(VARCHAR_TYPE, new DerbyStringConverter());
        converterMap.put(TIMESTAMP_TYPE, new SQLTimeStampConverterImpl());
        converterMap.put(DEFAULT_CONVERTER, new GenericSQLTypeConverter());
        return new LineValueToSQLTypeConverterImpl(converterMap);
    }    
}
