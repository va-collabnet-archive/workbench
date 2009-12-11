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

import java.io.File;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.mojo.ConceptDescriptor;

/**
 *
 * @author Tore Fjellheim
 *
 * @goal createMemberRefset
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class CreateMemberRefset extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter
     * @required
     * The concept descriptor for the member set path.
     */
    private ConceptDescriptor memberSetPathDescriptor;

    /**
     * @parameter
     * The number of items to add to the uncommitted list before committing
     */
    private int commitSize = 1000;

    /**
     * @parameter
     * Use the direct non-transaction ACE API (defaults to false)
     */
    private boolean useNonTxInterface = false;

    /**
     * @parameter
     * The UUID for an alternate "Is a" relationship (defaults to null)
     */
    private String altIsA = null;

    /**
     * Output location for the change sets
     *
     * @parameter
     * @required
     */
    private File changeSetOutputDirectory;

    /**
     * @parameter
     * Generate additional log output (defaults to false)
     */
    private boolean additionalLogging = false;

    protected I_TermFactory termFactory;

    /**
     * Specification refsets to be processed.
     * @parameter
     */
    ConceptDescriptor[] specRefsets = null;

    public void execute() throws MojoExecutionException, MojoFailureException {
		try {                 
            I_GetConceptData altIsAConcept = null;
            if (altIsA != null) {
                termFactory = LocalVersionedTerminology.get();
                altIsAConcept = termFactory.getConcept(new UUID[] { UUID.fromString(this.altIsA) });
            }

			MemberRefsetCalculator calc = new MemberRefsetCalculator();

			calc.setOutputDirectory(outputDirectory);
			calc.setChangeSetOutputDirectory(changeSetOutputDirectory);
			calc.setPathConcept(memberSetPathDescriptor.getVerifiedConcept());
			calc.setValidateOnly(false);
			calc.setCommitSize(commitSize);
			calc.setUseNonTxInterface(useNonTxInterface);
            calc.setAltIsA(altIsAConcept);
            calc.setAdditionalLogging(additionalLogging);

			if (specRefsets != null) {
		        List<Integer> allowedRefsets = new ArrayList<Integer>();
		        for (ConceptDescriptor conceptDesc : specRefsets) {
			        allowedRefsets.add(conceptDesc.getVerifiedConcept().getConceptId());
		        }
		        calc.setAllowedRefsets(allowedRefsets);
			}

			calc.run();

		} catch (Exception e) {
			throw new MojoExecutionException("member refset calculation failed with exception", e);
		}

	}
}
