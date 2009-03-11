package org.dwfa.maven.derby;

import java.io.File;

public interface LogFileCreator {

    File createLog(File parentDir, String logFileName, String version);
}
