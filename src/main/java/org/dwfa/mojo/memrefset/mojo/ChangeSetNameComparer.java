package org.dwfa.mojo.memrefset.mojo;

import java.util.List;

public interface ChangeSetNameComparer {

    boolean containsPrefix(String prefix, List<String> files);
}
