package org.dwfa.mojo.relformat.mojo.sql.io.util;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

public final class FileUtilImpl implements FileUtil {

    public void createDirectoriesIfNeeded(final File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    public void createDirectoriesIfNeeded(final Directory directory) {
        directory.mkdirs();
    }

    public String createPath(final String directory, final String fileName) {
        return directory + File.separator + fileName;
    }

    public void closeSilently(final Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
             //be silent
        }
    }

    public void closeSilently(final Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception e) {
            //be silent
        }
    }
}
