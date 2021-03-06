/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ComponentValidator;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Read a binary change set, and apply the results of that change set to the open database.
 *
 * @goal bcs-read
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class BinaryChangeSetRead extends AbstractMojo {

    /**
     * The change set directory
     *
     * @parameter default-value= "${project.build.directory}/generated-resources/changesets/"
     */
    File changeSetDir;
    /**
     * The change set file name
     *
     * @parameter
     * @required
     */
    String changeSetFileName;
    /**
     * Whether to validate the change set first or not.
     *
     * @parameter
     */
    boolean validate = false;
    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir.getCanonicalPath()
                    + changeSetFileName, this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        I_TermFactory termFactory = Terms.get();
        try {
            I_ReadChangeSet reader = termFactory.newBinaryChangeSetReader(new File(changeSetDir, changeSetFileName));
            if (validate) {
                getLog().info("******* Validating changeset before importing. ********");
                reader.getValidators().add(new ComponentValidator());
            }
            HashSet<ConceptChronicleBI> indexedAnnotations = new HashSet<>();
            reader.read(indexedAnnotations);
            if (WorkflowHelper.isWorkflowCapabilityAvailable()) {
                I_ReadChangeSet wfReader = termFactory.newWfHxLuceneChangeSetReader(new File(changeSetDir, changeSetFileName));
                wfReader.read(indexedAnnotations);
            }

            for (ConceptChronicleBI concept : indexedAnnotations) {
                Ts.get().addUncommittedNoChecks(concept);
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
