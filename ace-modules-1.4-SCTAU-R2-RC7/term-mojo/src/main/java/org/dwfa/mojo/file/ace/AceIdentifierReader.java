package org.dwfa.mojo.file.ace;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class AceIdentifierReader extends IterableFileReader<AceIdentifierRow> {

    public AceIdentifierReader(File conceptFile) {
        setSourceFile(conceptFile);
        setHasHeader(false);
    }

    @Override
    protected AceIdentifierRow processLine(String line) {
        AceIdentifierRow aceIdentifierRow = null;
        try {
            aceIdentifierRow = new AceIdentifierRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return aceIdentifierRow;
    }

}
