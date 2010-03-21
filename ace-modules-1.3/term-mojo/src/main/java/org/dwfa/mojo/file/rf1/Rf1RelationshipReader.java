package org.dwfa.mojo.file.rf1;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf1RelationshipReader extends IterableFileReader<Rf1RelationshipRow> {

    public Rf1RelationshipReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf1RelationshipRow processLine(String line) {
        Rf1RelationshipRow rf1RelationshipRow = null;
        try {
            rf1RelationshipRow = new Rf1RelationshipRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf1RelationshipRow;
    }
}
