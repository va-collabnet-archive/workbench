package org.dwfa.mojo.file.ace;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class AceRelationshipReader extends IterableFileReader<AceRelationshipRow> {

    public AceRelationshipReader(File conceptFile) {
        setHasHeader(false);
        setSourceFile(conceptFile);
    }

    @Override
    protected AceRelationshipRow processLine(String line) {
        AceRelationshipRow aceRelationshipRow = null;
        try {
            aceRelationshipRow = new AceRelationshipRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return aceRelationshipRow;
    }
}
