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
package org.dwfa.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.util.io.FileIO;

/**
 * Goal which copies a directory to the specified location (including hidden
 * files).
 * 
 * @goal copy-dir
 * @requiresDependencyResolution compile
 * 
 */

public class CopyDirectory extends AbstractMojo {
    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private File inputDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter
     * @required
     */
    private Boolean copyInvisibles;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Log l = getLog();
            l.info("Now executing CopyDirectory from: " + inputDirectory + " to: " + outputDirectory + " invisibles: "
                + copyInvisibles);

            // calculate the SHA-1 hashcode for this mojo based on input
            if (MojoUtil.alreadyRun(l, inputDirectory.getAbsolutePath() + outputDirectory.getAbsolutePath(),
                this.getClass(), targetDirectory)) {
                return;
            }
            FileIO.recursiveCopy(inputDirectory, outputDirectory, copyInvisibles);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}
