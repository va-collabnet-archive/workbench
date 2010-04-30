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
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.ihtsdo.mojo.maven.MojoUtil;

/**
 * 
 * @goal vodb-import-jcs
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbImportBinaryChangeSetsInDir extends AbstractMojo {

    /**
     * changeSetDirStr
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDirStr;

    /**
     * changeSetSuffix
     * 
     * @parameter default-value=".jcs"
     */
    String changeSetSuffix;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDirStr + changeSetSuffix,
                this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology.get();
        try {
            File changeSetDir = new File(changeSetDirStr);
            File[] changeSets = changeSetDir.listFiles(new FileFilter() {

                public boolean accept(File f) {
                    return f.getName().endsWith(changeSetSuffix);
                }

            });
            if (changeSets != null) {
                for (File csf : changeSets) {
                    getLog().info("Importing: " + csf.getName());
                    I_ReadChangeSet reader = termFactoryImpl.newBinaryChangeSetReader(csf);
                    reader.read();
                }
            } else {
                getLog().info("No change sets found.");
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
