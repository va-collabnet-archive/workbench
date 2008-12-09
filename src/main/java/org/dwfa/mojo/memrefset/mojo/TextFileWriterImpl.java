package org.dwfa.mojo.memrefset.mojo;

import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class TextFileWriterImpl implements TextFileWriter {

    private final FileUtil fileUtil;

    public TextFileWriterImpl(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public void write(final String fileName, final String text) {
        Writer writer = openFile(fileName);
        write(writer, text);
        closeFile(writer);
    }

    private Writer openFile(final String fileName) {
        try {
            fileUtil.createDirectoriesIfNeeded(fileName);
            return new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }

    private void write(final Writer writer, final String content) {
        try {
            writer.write(content);
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }

    private void closeFile(final Writer writer) {
        try {
            writer.close();
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }
}
