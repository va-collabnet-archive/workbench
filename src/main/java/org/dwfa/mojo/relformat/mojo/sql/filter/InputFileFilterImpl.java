package org.dwfa.mojo.relformat.mojo.sql.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class InputFileFilterImpl implements InputFileFilter {

    private final FileMatcher fileMatcher;

    public InputFileFilterImpl(final FileMatcher fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public List<File> filter(final File inputDir, final List<String> filters) {
        List<File> matchedFiles = new ArrayList<File>();

        String[] files = inputDir.list();

        for (String file : files) {
            if (fileMatcher.match(file, filters)) {
                matchedFiles.add(new File(file));
            }
        }

        return matchedFiles;
    }
}
