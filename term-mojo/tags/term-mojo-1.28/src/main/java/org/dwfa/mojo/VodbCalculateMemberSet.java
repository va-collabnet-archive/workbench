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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;

/**
 * Calculates the member set of a particular reference set.
 * 
 * @author Christine Hill
 * 
 */

/**
 *
 * @goal vodb-calculate-member-set
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCalculateMemberSet extends AbstractMojo {

    /**
     * @parameter
     * The concept descriptor for the ref set spec.
     */
    private ConceptDescriptor refSetSpecDescriptor;

    /**
     * @parameter
     * @required
     * The concept descriptor for the member set path.
     */
    private ConceptDescriptor memberSetPathDescriptor;

    /**
     * Location to write list of uuids for included concepts.
     * @parameter
     */
    private File refsetInclusionsOutputFile = new File("refsetInclusions");

    /**
     * Location to write list of uuids for excluded concepts.
     * @parameter
     */
    private File refsetExclusionsOutputFile = new File("refsetExclusions");

    /**
     * @parameter
     * @required
     * The root concept.
     */
    private ConceptDescriptor rootDescriptor;

    public void execute() throws MojoExecutionException, MojoFailureException {

		I_TermFactory termFactory = LocalVersionedTerminology.get();
		List<Thread> threads = new LinkedList<Thread>(); 
		if (refSetSpecDescriptor==null) {
			try {
				I_IntSet status = termFactory.newIntSet();
				status.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());

				I_IntSet is_a = termFactory.newIntSet();
				is_a.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());

				I_GetConceptData refsetRoot = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

				Set<I_GetConceptData> refsetChildren = refsetRoot.getDestRelOrigins(status,is_a, null, false);
				for (I_GetConceptData refsetConcept : refsetChildren) {

					Set<I_GetConceptData> purposeConcepts = new HashSet<I_GetConceptData>();

					List<I_RelVersioned> rels = refsetConcept.getSourceRels();
					for (I_RelVersioned rel: rels) {
						List<I_RelTuple> tuples = rel.getTuples();
						for (I_RelTuple tuple : tuples) {
							if (tuple.getStatusId()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId() && 
									tuple.getRelTypeId()==termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids()).getConceptId()) {

								purposeConcepts.add(termFactory.getConcept(tuple.getC2Id()));
							}
						}
					}

					if (purposeConcepts.size()==1) {

						if (purposeConcepts.iterator().next().getConceptId()==termFactory.getConcept(RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION.getUids()).getConceptId()) {
							getLog().info("Found refset with inclusion specification: " + refsetConcept);
							threads.add(runMojo(refsetConcept));
						} 
					} 
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			} 
			
		} else {		
			try {
				threads.add(runMojo(refSetSpecDescriptor.getVerifiedConcept()));
			} catch (Exception e) {
				throw new MojoExecutionException(e.getMessage());
			}
		}
		
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			if (termFactory.getUncommitted().size()>0) {
				termFactory.commit();
			}
		} catch (Exception e) {
			throw new MojoExecutionException("failed to commit results of member set calculation", e);
		}

	}

    /**
     * Iterates over each concept and calculates the member set.
     */
    public Thread runMojo(I_GetConceptData refSetSpecDescriptor)
            throws MojoExecutionException, MojoFailureException {

        MemberSetCalculator calculator = null;
        try {

            // execute calculate member set plugin
            calculator =
                    new MemberSetCalculator(refSetSpecDescriptor,
                        memberSetPathDescriptor, rootDescriptor,
                        this.refsetInclusionsOutputFile,
                        this.refsetExclusionsOutputFile, getLog());
            // iterate over each concept, starting at the root
            calculator.start();

        } catch (Exception e) {
            e.printStackTrace();
            //throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return calculator;
    }

    public ConceptDescriptor getMemberSetPathDescriptor() {
        return memberSetPathDescriptor;
    }

    public void setMemberSetPathDescriptor(
            ConceptDescriptor memberSetPathDescriptor) {
        this.memberSetPathDescriptor = memberSetPathDescriptor;
    }

    public ConceptDescriptor getRefSetSpecDescriptor() {
        return refSetSpecDescriptor;
    }

    public void setRefSetSpecDescriptor(ConceptDescriptor refSetSpecDescriptor) {
        this.refSetSpecDescriptor = refSetSpecDescriptor;
    }

    public ConceptDescriptor getRootDescriptor() {
        return rootDescriptor;
    }

    public void setRootDescriptor(ConceptDescriptor rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }

    public String getFsnFromConceptId(int conceptId) throws Exception {

		I_GetConceptData concept = LocalVersionedTerminology.get().getConcept(conceptId);

		List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
		int fsnId = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.
				getUids().iterator().next());
		for (I_DescriptionVersioned description : descriptions) {
			List<I_DescriptionPart> parts = description.getVersions();
			for (I_DescriptionPart part : parts) {
				if (fsnId == part.getTypeId()) {
					return part.getText();
				}
			}
		}

		return "unknown";
	}
}
