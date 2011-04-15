package org.dwfa.mojo.file.rf2;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf2ConceptReader extends IterableFileReader<Rf2ConceptRow> {

    public Rf2ConceptReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf2ConceptRow processLine(String line) {
        Rf2ConceptRow rf2ConceptRow = null;
        try {
            rf2ConceptRow = new Rf2ConceptRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf2ConceptRow;
    }

}
