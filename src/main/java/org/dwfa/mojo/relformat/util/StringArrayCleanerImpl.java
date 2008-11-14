package org.dwfa.mojo.relformat.util;

import java.util.ArrayList;
import java.util.List;

public final class StringArrayCleanerImpl implements StringArrayCleaner {

    public String[] clean(final String[] array) {
        List<String> cleanedValues = new ArrayList<String>();

        for (String value : array) {
            if (value.trim().length() != 0) {
                cleanedValues.add(value.trim());
            }
        }

        return cleanedValues.toArray(new String[cleanedValues.size()]);

    }
}
