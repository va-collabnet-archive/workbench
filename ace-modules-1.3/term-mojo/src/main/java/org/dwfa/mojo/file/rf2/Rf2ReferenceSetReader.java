package org.dwfa.mojo.file.rf2;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf2ReferenceSetReader extends IterableFileReader<Rf2ReferenceSetRow> {

    public Rf2ReferenceSetReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf2ReferenceSetRow processLine(String line) {
        Rf2ReferenceSetRow rf2ReferenceSetRow = null;
        try {
            rf2ReferenceSetRow = new Rf2ReferenceSetRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf2ReferenceSetRow;
    }
}
