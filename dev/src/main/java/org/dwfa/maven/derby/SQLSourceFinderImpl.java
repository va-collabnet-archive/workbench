package org.dwfa.maven.derby;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SQLSourceFinderImpl implements SQLSourceFinder {

    private static final String SQL_EXTENSION = ".sql";

    public File[] find(final File targetDir, final String[] sources, final String[] sqlLocations) {
        if (hasSources(sources)) {
            return findBySources(targetDir, sources);
        }

        if (hasSQLLocations(sqlLocations)) {
            return findBySQLLocations(sqlLocations);
        }

        return lookInTargetDir(targetDir);
    }

    private boolean hasSQLLocations(final String[] sqlLocations) {
        return sqlLocations.length != 0;
    }

    private boolean hasSources(final String[] sources) {
        return sources.length != 0;
    }

    private File[] findBySQLLocations(final String[] sqlLocations) {
        List<File> sqlFiles = new ArrayList<File>();
        for (String sqlLocation : sqlLocations) {
            sqlFiles.addAll(Arrays.asList(findSQLFiles(sqlLocation)));
        }

        return sqlFiles.toArray(new File[sqlFiles.size()]);
    }

    private File[] findSQLFiles(final String sqlLocation) {
        return new File(sqlLocation).listFiles(new SQLFileFilter());
    }

    private File[] lookInTargetDir(final File targetDir) {
        return targetDir.listFiles(new SQLFileFilter());
    }

    private File[] findBySources(final File targetDir, final String[] sources) {
        File[] sqlSources = new File[sources.length];
        for (int i = 0; i < sources.length; i++) {
            sqlSources[i] = new File(targetDir, sources[i]);
        }

        return sqlSources;
    }

    private static class SQLFileFilter implements FileFilter {

        public boolean accept(final File fileName) {
             return fileName.getName().endsWith(SQL_EXTENSION);
        }
    }
}
