package org.dwfa.mojo.file.ace;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class AceDescriptionReader extends IterableFileReader<AceDescriptionRow> {

    public AceDescriptionReader(File conceptFile) {
        setSourceFile(conceptFile);
        setHasHeader(false);
    }

    @Override
    protected AceDescriptionRow processLine(String line) {
        AceDescriptionRow aceDescriptionRow = null;
        try {
            aceDescriptionRow = new AceDescriptionRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return aceDescriptionRow;
    }
}
