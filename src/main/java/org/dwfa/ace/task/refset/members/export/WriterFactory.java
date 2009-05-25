package org.dwfa.ace.task.refset.members.export;

public interface WriterFactory {

    DescriptionWriter createDescriptionFile(String refsetName) throws Exception;

    NoDescriptionWriter createNoDescriptionFile() throws Exception;

    void closeFiles() throws Exception;
}
