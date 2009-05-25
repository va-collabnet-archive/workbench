package org.dwfa.ace.task.refset.members.export;

public interface RefsetWriterParameterObject {

    ProgressLogger getProgressLogger();

    RefsetTextWriter getRefsetTextWriter();

    WriterFactory getWriterFactory();
}
