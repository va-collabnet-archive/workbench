package org.dwfa.mojo.relformat.mojo.sql.io.util;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

public interface FileUtil {

    void createDirectoriesIfNeeded(File directory);

    void createDirectoriesIfNeeded(Directory directory);

    String createPath(final String directory, final String fileName);

    void closeSilently(final Reader reader);

    void closeSilently(final Writer writer);

    void createDirectoriesIfNeeded(String fileName);

    String changeExtension(String fileName, String extension);
}
