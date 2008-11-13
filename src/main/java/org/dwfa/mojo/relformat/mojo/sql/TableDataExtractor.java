package org.dwfa.mojo.relformat.mojo.sql;

public interface TableDataExtractor {

    String extractName(final String text);

    String[] extractColumn(final String text);
}
