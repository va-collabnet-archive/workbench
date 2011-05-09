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
package org.dwfa.mojo.relformat.mojo.directimport;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.relformat.mojo.db.DerbyFileRunner;
import org.dwfa.mojo.relformat.mojo.db.DerbyFileRunnerImpl;
import org.dwfa.mojo.relformat.mojo.sql.filter.FileMatcherImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.FileLister;
import org.dwfa.mojo.relformat.mojo.sql.io.FileListerImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This plugin directly imports derby-compatible tokenized files into a derby database.
 *
 * @goal derby-direct-import
 * @phase process-resources
 */

public final class DerbyDirectImportMojo extends AbstractMojo {

    /**
     * This is the location from which derby-compatible input files are read.
     *
     * @parameter expression="${project.build.directory}/sql"
     */
    private File locationOfDerbyFiles;

    /**
     * Specifies the name of the derby database to import data into.
     *
     *  @parameter default-value="${project.build.directory}/releaseformats"
     */
    private String databaseName;


    /**
     * The tokeniser used to separate column data.
     *
     * @parameter default-value="\t"
     */
    private String tokeniser;


    /**
     * @parameter default-value="false"
     */
    private boolean verbose;


    private final FileLister fileLister = new FileListerImpl(new FileMatcherImpl());

    private final TableNameExtractor extractor = new HyphenatedTableNameExtractor();


    public void execute() throws MojoExecutionException, MojoFailureException {
        DerbyFileRunner runner = new DerbyFileRunnerImpl(getLog(), verbose);
        runner.connect(databaseName);        

        List<File> derbyFiles = getDerbyFiles();
        record("processing files: " + derbyFiles.toString());

        for (File derbyFile : derbyFiles) {
            try {
                String fileName = derbyFile.getPath();
                String tableName = extractor.extract(derbyFile.getName());
                String importData = "CALL SYSCS_UTIL.SYSCS_IMPORT_DATA (NULL, '" + tableName + "', NULL, NULL, '" +
                                        fileName +"', " + "'"+ tokeniser + "'" +", NULL, 'UTF-8', 0)";

                record(importData);
                runner.run(importData);
            } catch (Exception e) {
                //log errors and go to the next file.
                getLog().error(e);
            }
        }

        runner.disconnect();
    }

    private List<File> getDerbyFiles() {
        return fileLister.list(locationOfDerbyFiles, Arrays.asList("^(.)*\\.derb$"),
                Arrays.<String>asList());
    }

    private void record(final String message) {
        if (verbose) {
            getLog().info(message);
        }
    }
}
