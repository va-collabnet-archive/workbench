/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.maven.derby;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringInputStream;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.mojo.maven.RegexReplace;

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
            for (File f : getSQLFiles(sqlSrcDir)) {
                RegexReplace replacer = new RegexReplace("\\$\\{project.build.directory\\}",
                    outputDirectory.getCanonicalPath().replace('\\', '/'));
                logger.info("Transforming: " + f.getName());
                Reader is = new FileReader(f);
                String input = FileIO.readerToString(is);
                String sqlScript = replacer.execute(input);

                if (replaceForwardSlash) {
                    sqlScript = sqlScript.replace('/', File.separatorChar);
                }

                FileIO.copyFile(new StringInputStream(sqlScript), new FileOutputStream(new File(sqlTargetDir,
                    f.getName())), true);
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
