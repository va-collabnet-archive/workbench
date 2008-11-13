package org.dwfa.mojo.relformat.mojo.sql.io;

import java.io.File;
import java.util.List;

public interface FileLister {

    List<File> list(File inputFile, List<String> filters, List<String> excludes);
}
