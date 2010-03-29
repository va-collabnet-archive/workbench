package org.dwfa.mojo.file.rf2;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf2IdentifierReader extends IterableFileReader<Rf2IdentifierRow> {

    public Rf2IdentifierReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf2IdentifierRow processLine(String line) {
        Rf2IdentifierRow rf2IdentifierRow = null;
        try {
            rf2IdentifierRow = new Rf2IdentifierRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf2IdentifierRow;
    }

}
