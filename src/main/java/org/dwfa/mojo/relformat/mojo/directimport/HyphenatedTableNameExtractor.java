package org.dwfa.mojo.relformat.mojo.directimport;

public final class HyphenatedTableNameExtractor implements TableNameExtractor {

    public String extract(final String fileName) {
        return fileName.substring(0, fileName.indexOf("-")).toUpperCase();
    }
}
