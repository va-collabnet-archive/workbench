package org.dwfa.maven;

import java.io.File;

public final class FileLocationConfigurerImpl implements FileLocationConfigurer {

    private static final String SQL_SUBDIR  = "sql";
    private static final char FORWARD_SLASH = '/';
    
    private File sqlSourceDir;
    private File sqlTargetDir;
    private File dbDir;

    public FileLocationConfigurerImpl(final File sourceDirectory, final File outputDirectory, final String dbName) {
        sqlSourceDir = new File(sourceDirectory.getParentFile(), SQL_SUBDIR);
        sqlTargetDir = new File(outputDirectory, SQL_SUBDIR);
        sqlTargetDir.mkdirs();
        dbDir = new File(outputDirectory, dbName.replace(FORWARD_SLASH, File.separatorChar));
    }

    public File getSqlSourceDir() {
        return sqlSourceDir;
    }

    public File getSqlTargetDir() {
        return sqlTargetDir;
    }

    public File getDbDir() {
        return dbDir;
    }
}
