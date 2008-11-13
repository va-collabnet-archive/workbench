package org.dwfa.mojo.relformat.mojo.sql.filter;

import java.io.File;
import java.util.List;

public interface InputFileFilter {

    List<File> filter(File inputDir, List<String> filters);
}
