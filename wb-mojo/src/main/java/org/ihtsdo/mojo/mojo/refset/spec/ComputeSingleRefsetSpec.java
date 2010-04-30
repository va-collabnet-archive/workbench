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
package org.ihtsdo.mojo.mojo.refset.spec;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.ComputeDescRefsetFromSpecTask;
import org.dwfa.ace.task.refset.spec.compute.ComputeRefsetFromSpecTask;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;

/**
 * Computes the membership of the specified refset spec.
 * 
 * @goal compute-single-refset-membership
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ComputeSingleRefsetSpec extends AbstractMojo {

    /**
     * The refset spec.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor refsetSpecDescriptor;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + refsetSpecDescriptor, this
                .getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {

            I_GetConceptData refsetSpec = refsetSpecDescriptor.getVerifiedConcept();
            boolean showActivityPanel = false;
            RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
            I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();

            if (refsetSpecHelper.isConceptComputeType()) {
                ComputeRefsetFromSpecTask task = new ComputeRefsetFromSpecTask();
                task.computeRefset(LocalVersionedTerminology.get().getActiveAceFrameConfig(), memberRefset,
                    showActivityPanel);
            } else {
                ComputeDescRefsetFromSpecTask task = new ComputeDescRefsetFromSpecTask();
                task.computeRefset(LocalVersionedTerminology.get().getActiveAceFrameConfig(), memberRefset,
                    showActivityPanel);
            }

            Terms.get().commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
