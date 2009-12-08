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
package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ExtensionValidator;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * @goal validate-extension-details
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class ValidateExtensionDetails extends AbstractMojo {

    /**
     * Location to write list of extension validation alerts.
     * 
     * @parameter expression="${project.build.directory}/extensionAlerts.txt"
     * @required
     */
    private File alertsOutputFile;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            ValidateConceptExtensions vce = new ValidateConceptExtensions();

            BufferedWriter writer = new BufferedWriter(new FileWriter(alertsOutputFile));

            for (AlertToDataConstraintFailure alert : vce.getAlertList()) {
                writer.write(alert.getAlertMessage());
                writer.newLine();
            }// End for loop

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }// End method execute

    private class ValidateConceptExtensions implements I_ProcessConcepts {
        private int refsetType = -1;
        private List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

        public void setRefsetType(int refsetType) {
            this.refsetType = refsetType;
        }

        public List<AlertToDataConstraintFailure> getAlertList() {
            return this.alertList;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            ExtensionValidator ev = new ExtensionValidator();
            alertList.addAll(ev.validate(concept.getConceptId(), refsetType, false));
        }// End method processConcept
    }// End nested class ValidateConceptExtensions

}// End class ValidateExtensionDetails
