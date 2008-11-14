package org.dwfa.mojo.relformat.mojo.converter;

import org.dwfa.mojo.relformat.mojo.sql.SQLCreator;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

public final class DerbyLineCreator implements SQLCreator {

    public String createSQL(final Table table, final String[] values) {
        StringBuilder builder = new StringBuilder();

        for (int index=0; index < values.length; index++) {
            builder.append(values[index]);

            if (index < (values.length - 1)) {
                builder.append('\t');
            }
        }

        return builder.toString();
    }
}
