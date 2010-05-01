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
package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.task.cmrscs.ImportCmrscs;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.maven.MojoUtil;

/**
 * Read all binary change set under a specified directory hierarchy, and apply
 * the results of
 * that change set to the open database.
 * 
 * @goal member-cs-read-all
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class MemberRefsetChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir, this.getClass(),
                targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        ImportCmrscs importCmrscs = new ImportCmrscs();

        importCmrscs.setRootDirStr(changeSetDir);
        try {
            importCmrscs.importAllChangeSets(new LoggerAdaptor(getLog()));
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
