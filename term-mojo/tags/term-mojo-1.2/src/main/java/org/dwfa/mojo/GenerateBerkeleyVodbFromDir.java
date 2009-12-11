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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal berkley-vodb-dir
 * 
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */

public class GenerateBerkeleyVodbFromDir extends AbstractMojo {

    /**
     * Location of the data directory.
     * 
     * @parameter expression="${project.build.directory}/generated-resources/ace/"
     * @required
     */
    File dataDirectory;

    private String[] allowedGoals = new String[] { "install" };

    /**
     * The maven session
     * 
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
            try {
                if (MojoUtil.alreadyRun(getLog(), dataDirectory
                    .getCanonicalPath())) {
                    return;
                }
                LocalVersionedTerminology.get()
                    .loadFromDirectory(dataDirectory);
            } catch (Exception ex) {
                throw new MojoExecutionException(
                    "Error processing dependency. Reason: " + ex.getMessage(),
                    ex);
            }
        }
    }

}
