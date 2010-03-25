package org.dwfa.mojo.file.rf1;

import java.io.File;

import org.dwfa.ace.file.IterableFileReader;

public class Rf1DescriptionReader extends IterableFileReader<Rf1DescriptionRow> {

    public Rf1DescriptionReader(File conceptFile) {
        setSourceFile(conceptFile);
    }

    @Override
    protected Rf1DescriptionRow processLine(String line) {
        Rf1DescriptionRow rf1DescriptionRow = null;
        try {
            rf1DescriptionRow = new Rf1DescriptionRow(line);
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Invalid file format" + ex);
        } catch (Exception e) {
            throw new RuntimeException("Cannot process line:" + e);
        }

        return rf1DescriptionRow;
    }
}
