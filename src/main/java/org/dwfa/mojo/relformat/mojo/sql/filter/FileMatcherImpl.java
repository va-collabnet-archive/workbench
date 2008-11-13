package org.dwfa.mojo.relformat.mojo.sql.filter;

import java.util.List;

public final class FileMatcherImpl implements FileMatcher {

    public boolean match(final String fileName, final List<String> filters) {
        for (String filter : filters) {
            if (fileName.matches(filter)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldExclude(final String fileName, final List<String> excludes) {
        return match(fileName, excludes);
    }
}
