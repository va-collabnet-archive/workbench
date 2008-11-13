package org.dwfa.mojo.relformat.mojo.sql;

import java.io.File;

public interface FileNameExtractor {

    String extractFileName(final File file);
}
