package org.dwfa.mojo.relformat.mojo.sql.filter;

import java.util.List;

public interface FileMatcher {

    boolean match(String fileName, List<String> filters);
    
    boolean shouldExclude(String fileName, List<String> excludes);
}
