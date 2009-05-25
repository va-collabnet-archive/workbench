package org.dwfa.ace.task.refset.members.export;

public interface ExportWriter {

    void close() throws Exception;

    ExportWriter append(String text) throws Exception;
}
