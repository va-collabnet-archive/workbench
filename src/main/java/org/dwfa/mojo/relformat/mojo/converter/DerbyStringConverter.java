package org.dwfa.mojo.relformat.mojo.converter;

import org.dwfa.mojo.relformat.mojo.sql.converter.SQLTypeConverter;

public final class DerbyStringConverter implements SQLTypeConverter {

    public String convert(final String value) {
        return value.replace("\"", "\"\"");
    }
}
