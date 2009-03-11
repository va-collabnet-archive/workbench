package org.dwfa.maven.derby;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class LogFileCreatorImpl implements LogFileCreator {

    public File createLog(final File parentDir, final String logFileName, final String version) {
        try {
            File logFile = new File(parentDir, logFileName);
            logFile.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(logFile);
            fw.append("Created by DWFA derby plugin version: " + version + "\n");
            fw.close();
            return logFile;
        } catch (IOException e) {
            throw new LogFileCreatorException(e);
        }        
    }
}
