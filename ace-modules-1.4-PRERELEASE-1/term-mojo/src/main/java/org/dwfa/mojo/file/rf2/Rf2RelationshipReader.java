package org.dwfa.mojo.file.rf2;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf2RelationshipReader extends IterableFileReader<Rf2RelationshipRow> {

    public Rf2RelationshipReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf2RelationshipRow processLine(String line) {
        Rf2RelationshipRow rf2RelationshipRow = null;
        try {
            rf2RelationshipRow = new Rf2RelationshipRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf2RelationshipRow;
    }
}
