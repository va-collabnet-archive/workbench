package org.dwfa.mojo.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import org.dwfa.ace.file.GenericFileWriter;

/**
 * An instance of an instance of a {@link GenericFileWriter} for writing release files.
 * @author Matthew Edwards
 */
public final class ReleaseFileWriter extends GenericFileWriter<ReleaseFileRow> {

    /** Output Writer. */
    private final transient BufferedWriter outputWriter;
    /** Instance of File to write {@code ReleaseFileRow}'s to. */
    private final transient File outputFile;
    /** {@link ReentrantLock} to limit concurrent access to the writer. */
    private final transient ReentrantLock outputWriterLock;

    /**
     * Creates an instance of a {@link GenericFileWriter} for writing release files.
     * @param outputFile the file to write out to.
     * @throws IOException if there are any failed or interrupted I/O operations.
     */
    public ReleaseFileWriter(final File outputFile) throws IOException {
        super();
        this.outputFile = outputFile;
        outputWriter = new BufferedWriter(new FileWriter(this.outputFile));
        outputWriterLock = new ReentrantLock();
    }

    /**
     * {@inheritDoc }. This implementation protects the internal {@link BufferedWriter} with a {@link ReentrantLock} to
     * ensure Safe Concurrency.
     * @param fileRow the {@link ReleaseFileRow} to write.
     * @throws IOException If there are any failed or interrupted I/O Operations.
     */
    @Override
    public void write(final ReleaseFileRow fileRow) throws IOException {
        this.outputWriterLock.lock();
        try {
            if (!fileRow.getOutputRow().isEmpty()) {
                outputWriter.write(fileRow.getOutputRow());
                outputWriter.newLine();
            }
        } finally {
            this.outputWriterLock.unlock();
        }
    }

    @Override
    public void open(final File outputFile, final boolean append) {
        throw new UnsupportedOperationException("This implementation does not support opening a file directly.");
    }

    @Override
    public String serialize(final ReleaseFileRow object) {
        return object.getOutputRow();
    }

    @Override
    public void write(final List<ReleaseFileRow> objectList) throws IOException {
        for (ReleaseFileRow row : objectList) {
            write(row);
        }
    }

    @Override
    protected void writeHeader(final String header) {
        throw new UnsupportedOperationException("Use the write method with an instance of ReleaseFileRow as a header.");
    }

    @Override
    public void abort() throws IOException {
        close();
        if (!outputFile.delete()) {
            Logger.getLogger(this.getClass().getName()).warning(
                    String.format("Attempting to Delete File '%1$s' after abort failed. File was not removed.",
                    outputFile.getAbsolutePath()));
        }
    }

    @Override
    public void close() throws IOException {
        this.outputWriterLock.lock();
        try {
            outputWriter.close();
        } finally {
            this.outputWriterLock.unlock();
        }
    }
}
