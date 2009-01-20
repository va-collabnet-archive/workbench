package org.dwfa.mojo.memrefset.mojo;

import java.util.List;

public final class ChangeSetNameComparerImpl implements ChangeSetNameComparer {

    public boolean containsPrefix(final String prefix, final List<String> files) {

        for (String file : files) {
            if (file.startsWith(prefix)) {
                return true;
            }
        }

        return false;

    }
}
