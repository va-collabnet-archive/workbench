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
package org.dwfa.mojo.refset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.file.IterableFileReader;

/**
 * Imports the contents of refset files from a directory
 *
 * @see https
 *      ://mgr.cubit.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/dataimport
 *      .html
 * @goal load-refset-files-from-directory
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class ImportRefsetFromDirectory extends AbstractMojo {

    /**
     * Ignore errors
     *
     * @parameter
     */
    boolean ignoreErrors = false;

    /**
     * Directory the files are to read from
     *
     * @parameter
     * @required
     */
    File refsetDirectory;

    /**
     * Indicates if the transactional ACE interface should be used - defaults to
     * false
     *
     * @parameter
     */
    boolean transactional;

    /**
     * Indicates if the files contain a header row or not. If true the first
     * line of the file
     * will be skipped. Default value is true.
     *
     * @parameter
     */
    boolean hasHeader = true;

    /**
     * List of filename expressions to exclude
     *
     * @parameter
     */
    List<String> exclusions = new ArrayList<String>();

    private FilenameFilter filenameFilter;

    /*
     * Mojo execution method.
     *
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            List<File> files = recursivelyGetFiles(refsetDirectory);
            if (files.isEmpty()) {
                getLog().warn("No files found to import at specified directory " + refsetDirectory);
                return;
            }

            for (File file : files) {
                if (notExcluded(file.getName())) {
                    getLog().info("Processing refset file " + file);

                    try {
                        IterableFileReader<I_ThinExtByRefPart> handler = RefsetType.getHandlerForFile(file);
                        handler.setTransactional(transactional);
                        handler.setSourceFile(file);
                        handler.setHasHeader(hasHeader);

                        int i = 0;
                        Iterator<I_ThinExtByRefPart> memebrIterator = handler.iterator();
                        while (memebrIterator.hasNext()) {
                            try {
                                memebrIterator.next();
                                if (++i % 1000 == 0) {
                                    getLog().info("Imported " + i + " extensions from file " + file);
                                }
                            } catch (Exception ignor) {
                                getLog().error("Error on line  " + i + " : " + ignor.getMessage());
                                if (!ignoreErrors) {
                                    throw ignor;
                                }
                            }
                        }
                        getLog().info("Completed importing " + i + " extensions from file " + file);
                    } catch (Exception e) {
                        getLog().warn("Failed to import file " + file + " due to exception. Continuing to next file.",
                            e);
                    }
                } else {
                    getLog().info("File " + file + " excluded from import");
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException("failed importing files from " + refsetDirectory, e);
        }
    }

    private boolean notExcluded(String name) {
        for (String exclusion : exclusions) {
            if (name.matches(exclusion)) {
                return false;
            }
        }
        return true;
    }

    private List<File> recursivelyGetFiles(File directory) {
        List<File> allFiles = new ArrayList<File>();

        if (!directory.isDirectory()) {
            allFiles.add(directory);
        } else {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    allFiles.addAll(recursivelyGetFiles(file));
                } else {
                    allFiles.add(file);
                }
            }
        }

        return allFiles;
    }

}
