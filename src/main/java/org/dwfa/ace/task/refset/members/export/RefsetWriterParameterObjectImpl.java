package org.dwfa.ace.task.refset.members.export;

public class RefsetWriterParameterObjectImpl implements RefsetWriterParameterObject {
    private final ProgressLogger progressLogger;
    private final RefsetTextWriter refsetTextWriter;
    private final WriterFactory writerFactory;

    public RefsetWriterParameterObjectImpl(final ProgressLogger progressLogger, final RefsetTextWriter refsetTextWriter, final WriterFactory writerFactory) {
        this.progressLogger = progressLogger;
        this.refsetTextWriter = refsetTextWriter;
        this.writerFactory = writerFactory;
    }

    public ProgressLogger getProgressLogger() {
        return progressLogger;
    }

    public RefsetTextWriter getRefsetTextWriter() {
        return refsetTextWriter;
    }

    public WriterFactory getWriterFactory() {
        return writerFactory;
    }
}
