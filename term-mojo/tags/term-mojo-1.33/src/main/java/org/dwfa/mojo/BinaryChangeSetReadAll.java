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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.task.cs.ImportAllChangeSets;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.maven.MojoUtil;

/**
 * Read all binary change set under a specified directory hierarchy, and apply the results of 
 * that change set to the open database.
 * @goal bcs-read-all
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class BinaryChangeSetReadAll extends AbstractMojo {
    /**
     * The change set directory
     *
     * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
     */
    String changeSetDir;

    /**
     * List of validators to use when validating change sets if validate = true
     * 
     * @parameter
     */
    private String[] validators =
            new String[] { ComponentValidator.class.getName() };

    /**
     * Whether to validate the change set first or not. Default value is true; 
     * @parameter
     */
    boolean validate = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass()
                .getCanonicalName()
                + changeSetDir)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        getLog().info("importing change sets in: " + changeSetDir);
        ImportAllChangeSets importAllChangeSetsTask = new ImportAllChangeSets();
        importAllChangeSetsTask.setValidateChangeSets(validate);
        String validatorString = "";
        for (int i = 0; i < validators.length; i++) {
            validatorString += validators[i];
            if (i != validators.length - 1) {
                //if not the last element
                validatorString += ",";
            }
        }

        importAllChangeSetsTask.setValidators(validatorString);
        importAllChangeSetsTask.setRootDirStr(changeSetDir);
        try {
            importAllChangeSetsTask.importAllChangeSets(new LoggerAdaptor(
                getLog()));
        } catch (TaskFailedException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
