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
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * @goal vodb-commit
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCommit extends AbstractMojo {

    /**
     * The execution information for this commit operation.
     * 
     * @parameter expression="${mojoExecution}"
     */
    MojoExecution execution;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + execution.getExecutionId(),
                this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        getLog().info("commiting (id: " + execution.getExecutionId() + "): " + termFactory);
        if (termFactory != null) {
            if (termFactory.getUncommitted() != null && termFactory.getUncommitted().size() > 0) {
                try {

                    termFactory.commit();
                } catch (Exception e) {
                    throw new MojoExecutionException(e.getLocalizedMessage(), e);
                }
            } else {
                getLog().info("termfactory.getUncommitted().size() = " + termFactory.getUncommitted().size());
            }
        } else {
            Exception ex = new Exception("Attempting commit with null term factory (id: " + execution.getExecutionId()
                + "): " + termFactory);
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }

    }
}
