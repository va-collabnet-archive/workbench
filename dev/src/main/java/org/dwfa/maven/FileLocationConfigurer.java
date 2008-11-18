package org.dwfa.maven;

import java.io.File;

public interface FileLocationConfigurer {

    File getSqlSourceDir();

    File getSqlTargetDir();

    File getDbDir();
}
