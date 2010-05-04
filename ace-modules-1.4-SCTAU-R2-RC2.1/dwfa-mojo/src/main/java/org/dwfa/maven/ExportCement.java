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
import java.text.DateFormat;
import java.util.Date;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.AceDateFormat;

/**
 * Export the standard CEMeNT (Common Enumerations and Metadata to Normalize
 * Terminology) taxonomies in
 * a standard SNOMED format (no branch ids or effective date for concepts or
 * relationships).
 * 
 * @goal export-cement
 * @phase process-resources
 */
public class ExportCement extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter
     */
    private String output;

    /**
     * The maven session
     * 
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    /**
     * Only execute this mojo for one of the allowed goals.
     * This will prevent unexpected execution of plugins when
     * other goals are executed such as eclipse:eclipse or
     * site:site.
     * 
     * There may be better ways to do this... If you find one,
     * please let us know :-)
     */

    private String[] allowedGoals = new String[] { "install" };

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
            getLog().info("Exporting cement");
            DateFormat dateFormat = AceDateFormat.getRf2DateFormat();
            try {
                String prefix;
                if (output == null) {
                    prefix = outputDirectory.getCanonicalPath() + File.separatorChar + "generated-resources"
                        + File.separatorChar + "concrete" + File.separatorChar + dateFormat.format(new Date())
                        + File.separatorChar;
                } else {
                    output = output.replace('/', File.separatorChar);
                    prefix = output + File.separator + dateFormat.format(new Date()) + File.separatorChar;
                }
                ArchitectonicAuxiliary.main(new String[] { prefix });
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
