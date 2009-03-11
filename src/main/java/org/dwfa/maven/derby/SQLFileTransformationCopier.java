package org.dwfa.maven.derby;

import java.io.File;

public interface SQLFileTransformationCopier {

    void copySQLFilesToTarget(File sqlSrcDir, File sqlTargetDir);
}
