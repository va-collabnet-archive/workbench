package org.dwfa.mojo.relformat.mojo.converter;

import java.util.ArrayList;
import java.util.List;

public final class DerbyLineBuilder {

    private List<String> values;

    public DerbyLineBuilder() {
        values = new ArrayList<String>();
    }

    public DerbyLineBuilder addValue(final String value) {
        values.add(value);
        return this;
    }

    public DerbyLineBuilder addBlank() {
        values.add("NULL");
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();

        for (int index=0; index < values.size(); index++) {
            builder.append(values.get(index));

            if (index < (values.size() -1)) {
                builder.append('\t');
            }
        }

        return builder.toString();
    }

    public String defaults() {
        return new DerbyLineBuilder().
                addValue("100").
                addBlank().
                addValue("Testing").
                addValue("3").
                addValue("A").
                addValue("2008-11-06 15:55:33").
                addValue("2008-11-11").
                addValue("10:35:46").
                build();
    }
}
