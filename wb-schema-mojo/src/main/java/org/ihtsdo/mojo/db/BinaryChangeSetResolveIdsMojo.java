/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.db;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.tapi.TerminologyException;

/**
 * Read all binary change set under a specified directory hierarchy create a map of when the SCT IDs
 * were used with respective enclosing concepts keep only the latest use of SCT IDs which were used
 * for more than one enclosing concept write out a change set file with
 *
 * @goal bcs-resolve-ids
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class BinaryChangeSetResolveIdsMojo extends AbstractMojo {

    /**
     * The change set directory
     *
     * @parameter default-value= "${project.build.directory}/changesets/"
     */
    String changeSetDir;
    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}/generated-artifact"
     * @required
     */
    private String targetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("resolving change set ids in: " + changeSetDir);
        try {
            BinaryChangeSetResolveIds rcsi = new BinaryChangeSetResolveIds(changeSetDir, targetDirectory);
            rcsi.processFiles();
        } catch (IOException | TerminologyException ex) {
            throw new MojoExecutionException("BinaryChangeSetResolveIdsMojo \n", ex);
        }
    }

    public String getChangeSetDir() {
        return changeSetDir;
    }

    public void setChangeSetDir(String changeSetDir) {
        this.changeSetDir = changeSetDir;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
