package org.dwfa.mojo.file.rf1;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf1ConceptReader extends IterableFileReader<Rf1ConceptRow> {

    public Rf1ConceptReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf1ConceptRow processLine(String line) {
        Rf1ConceptRow rf1ConceptRow = null;
        try {
            rf1ConceptRow = new Rf1ConceptRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf1ConceptRow;
    }

}
