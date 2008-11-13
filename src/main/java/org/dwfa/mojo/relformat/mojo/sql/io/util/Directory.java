package org.dwfa.mojo.relformat.mojo.sql.io.util;

import java.io.File;

/**
 * Created to distinguish between a 'File' and a 'Directory'.
 */
public final class Directory {

    private final File directory;

    public Directory(final String directory) {
        this.directory = new File(directory);
    }

    public void mkdirs() {
        directory.mkdirs();
    }
}
