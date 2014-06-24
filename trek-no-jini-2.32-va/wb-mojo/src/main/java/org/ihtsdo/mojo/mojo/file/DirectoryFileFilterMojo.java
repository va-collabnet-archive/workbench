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
/**
 * 
 */
package org.ihtsdo.mojo.mojo.file;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Simple Mojo that ssentially greps all files in a directory and writes the
 * result to another directory.
 * Given a regular expression the Mojo can be configured to copy all matching
 * or non-matchine lines from the input file to the output file
 * 
 * @author Dion McMurtrie
 * 
 * @goal filter-files-in-directory
 */
public class DirectoryFileFilterMojo extends AbstractMojo {

    /**
     * Directory to be read and filtered using the supplied expression
     * 
     * @parameter
     * @required
     */
    File inputDirectory;

    /**
     * Directory to write the filtering result to - NB if any files file exists
     * they will
     * be overwritten
     * 
     * @parameter
     * @required
     */
    File outputDirectory;

    /**
     * Regular expression used to match lines from the input file as a basis for
     * filtering the file.
     * 
     * @parameter
     * @required
     */
    String expression;

    /**
     * Indicates if matching lines from the input file should be copied to the
     * output file.
     * If true, only lines matching the expression will be copied to the output
     * file, if
     * false only lines not matching the expression will be copied to the output
     * file.
     * Default value is true.
     * 
     * @parameter
     */
    boolean matchingLinesPreserved = true;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!inputDirectory.exists() || !inputDirectory.isDirectory() || !inputDirectory.canRead()) {
            throw new MojoFailureException("The input directory " + inputDirectory
                + " must exist, be a dirextory and be readable");
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        FileFilterMojo fileFilterMojo = new FileFilterMojo();
        fileFilterMojo.setExpression(expression);
        fileFilterMojo.setMatchingLinesPreserved(matchingLinesPreserved);

        for (File file : inputDirectory.listFiles()) {
            fileFilterMojo.setInputFile(file);
            fileFilterMojo.setOutputFile(new File(outputDirectory, file.getName()));
            fileFilterMojo.execute();
        }
    }
}
