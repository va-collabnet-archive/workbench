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
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * Compress database log files so their utilization is the value provided. 
 * Call this goal prior to closing the database. Corresponds to je.cleaner.minUtilization.
 * 
 * @goal vodb-compress
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCompress extends AbstractMojo {

    /**
     * Ensure the total disk space utilization percentage is above this value. 
     * The default is set to 90 percent. The maximum is 90 percent. The environmental
     * default is 50 percent.
     * @parameter
     */
    private int minUtilization = 90;

    /**
     * @parameter expression="${mojoExecution}"
     */
    private org.apache.maven.plugin.MojoExecution execution;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), execution.getExecutionId(), this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e1) {
            throw new MojoExecutionException(e1.getLocalizedMessage(), e1);
        } catch (IOException e1) {
            throw new MojoExecutionException(e1.getLocalizedMessage(), e1);
        }
        I_ImplementTermFactory termFactoryImpl =
                (I_ImplementTermFactory) LocalVersionedTerminology.get();
        try {
            termFactoryImpl.compress(minUtilization);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
