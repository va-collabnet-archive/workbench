package org.dwfa.mojo.relformat.mojo.sql.extractor;

public final class LineToValuesExtractorImpl implements LineToValuesExtractor {


    public String[] extract(final String line) {
        return line.split("\\t");
    }
}
