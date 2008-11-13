package org.dwfa.mojo.relformat.mojo.sql.io;

import org.dwfa.mojo.relformat.mojo.sql.filter.FileMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FileListerImpl implements FileLister {

    private final FileMatcher fileMatcher;

    public FileListerImpl(final FileMatcher fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public List<File> list(final File inputFile, final List<String> filters, final List<String> excludes) {
        File[] files = inputFile.listFiles();
        List<File> resolvedFiles = new ArrayList<File>();

        for (File file : files) {
            if (file.isDirectory()) {
                if (isValid(file, excludes)) {
                    resolvedFiles.addAll(list(file, filters, excludes));
                }                
                continue;
            }

            if (fileMatcher.match(file.getName(), filters)) {            
                resolvedFiles.add(file);
            }
        }

        return resolvedFiles;
    }

    private boolean isValid(final File file, final List<String> excludes) {
        return !fileMatcher.shouldExclude(file.getName(), excludes);
    }
}
