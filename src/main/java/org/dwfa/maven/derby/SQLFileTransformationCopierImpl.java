package org.dwfa.maven.derby;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringInputStream;
import org.dwfa.maven.RegexReplace;
import org.dwfa.util.io.FileIO;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;

public final class SQLFileTransformationCopierImpl implements SQLFileTransformationCopier {

    private final Log logger;
    private final File outputDirectory;
    private final boolean replaceForwardSlash;

    public SQLFileTransformationCopierImpl(final Log logger, final File outputDirectory,
                                           final boolean replaceForwardSlash) {
        this.logger = logger;
        this.replaceForwardSlash = replaceForwardSlash;
        this.outputDirectory = outputDirectory;
    }

    public void copySQLFilesToTarget(final File sqlSrcDir, final File sqlTargetDir) {
        try {
            for (File f: getSQLFiles(sqlSrcDir)) {
                RegexReplace replacer = new RegexReplace("\\$\\{project.build.directory\\}",
                        outputDirectory.getCanonicalPath().replace('\\', '/'));
                logger.info("Transforming: " + f.getName());
                Reader is = new FileReader(f);
                String input = FileIO.readerToString(is);
                String sqlScript = replacer.execute(input);

                if (replaceForwardSlash) {
                    sqlScript = sqlScript.replace('/', File.separatorChar);
                }

                FileIO.copyFile(new StringInputStream(sqlScript),
                                new FileOutputStream(new File(sqlTargetDir, f.getName())),
                                true);
            }
        } catch (IOException e) {
            throw new SQLFileTransformationCopierException(e);
        }
    }

    private File[] getSQLFiles(final File sqlSrcDir) {
        return sqlSrcDir.listFiles(new FileFilter() {
            public boolean accept(final File f) {
                return f.getName().endsWith(".sql");
            }
        });
    }
}
