package org.dwfa.mojo.file.rf2;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf2DescriptionReader extends IterableFileReader<Rf2DescriptionRow> {

    public Rf2DescriptionReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf2DescriptionRow processLine(String line) {
        Rf2DescriptionRow rf2DescriptionRow = null;
        try {
            rf2DescriptionRow = new Rf2DescriptionRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf2DescriptionRow;
    }
}
