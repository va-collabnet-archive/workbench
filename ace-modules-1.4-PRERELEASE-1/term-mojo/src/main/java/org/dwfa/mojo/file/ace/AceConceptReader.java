package org.dwfa.mojo.file.ace;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class AceConceptReader extends IterableFileReader<AceConceptRow> {

    public AceConceptReader(File conceptFile) {
        setSourceFile(conceptFile);
        setHasHeader(false);
    }

    @Override
    protected AceConceptRow processLine(String line) {
        AceConceptRow aceConceptRow = null;
        try {
            aceConceptRow = new AceConceptRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return aceConceptRow;
    }

}
