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
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.ComputeDescRefsetFromSpecTask;
import org.dwfa.ace.task.refset.spec.compute.ComputeRefsetFromSpecTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;

/**
 * Computes the membership of the specified refset spec.
 * 
 * @goal compute-all-refset-membership
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ComputeAllRefsetSpec extends AbstractMojo {

	/**
	 * List of refset specs to exclude from the computation.
	 * 
	 * @parameter
	 * @optional
	 */
	private ConceptDescriptor[] excludedRefsetSpecDescriptors;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (MojoUtil.alreadyRun(getLog(), this.getClass()
					.getCanonicalName()
					+ excludedRefsetSpecDescriptors, this.getClass(),
					targetDirectory)) {
				return;
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

			Set<Integer> excludedRefsets = new HashSet<Integer>();
			if (excludedRefsetSpecDescriptors != null) {
				for (ConceptDescriptor refsetSpecDescriptor : excludedRefsetSpecDescriptors) {
					RefsetSpec spec = new RefsetSpec(refsetSpecDescriptor
							.getVerifiedConcept());
					excludedRefsets.add(spec.getMemberRefsetConcept()
							.getConceptId());
				}
			}

			// get all children of supporting refsets
			I_GetConceptData supportingRefsetConcept = termFactory
					.getConcept(RefsetAuxiliary.Concept.SUPPORTING_REFSETS
							.getUids());
			I_IntSet isA = termFactory.newIntSet();
			isA.add(termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
					.getConceptId());
			// SpecRefsetHelper helper = new SpecRefsetHelper();
			I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(config);

			I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

			// Set<? extends I_GetConceptData> children =
			// supportingRefsetConcept.getDestRelOrigins(currentStatuses, isA,
			// termFactory
			// .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true,
			// true);
			Set<? extends I_GetConceptData> children = supportingRefsetConcept
					.getDestRelOrigins(currentStatuses, isA, config
							.getViewPositionSetReadOnly(), config
							.getPrecedence(), config
							.getConflictResolutionStrategy());

			Set<Integer> computedRefsets = new HashSet<Integer>();
			for (I_GetConceptData child : children) {
				if (conceptIsRefsetSpec(child)) {

					RefsetSpec refsetSpecHelper = new RefsetSpec(child);
					I_GetConceptData memberRefset = refsetSpecHelper
							.getMemberRefsetConcept();
					boolean showActivityPanel = false;
					if (!computedRefsets.contains(memberRefset.getConceptId())) {

						if (!excludedRefsets.contains(memberRefset
								.getConceptId())) {

							if (refsetSpecHelper.isConceptComputeType()) {
								ComputeRefsetFromSpecTask task = new ComputeRefsetFromSpecTask();
								task.setExcludedRefsets(excludedRefsets);
								task.computeRefset(config, memberRefset,
										showActivityPanel);
								/*
								 * need to keep track of any calculated nested
								 * refsets so we don't compute them twice
								 */
								computedRefsets
										.add(memberRefset.getConceptId());
								for (Integer nestedRefsetId : task
										.getNestedRefsets()) {
									if (!excludedRefsets
											.contains(nestedRefsetId)) {
										computedRefsets.add(nestedRefsetId);
									}
								}
							} else {
								ComputeDescRefsetFromSpecTask task = new ComputeDescRefsetFromSpecTask();
								task.setExcludedRefsets(excludedRefsets);
								task.computeRefset(LocalVersionedTerminology
										.get().getActiveAceFrameConfig(),
										memberRefset, showActivityPanel);

								computedRefsets
										.add(memberRefset.getConceptId());
								for (Integer nestedRefsetId : task
										.getNestedRefsets()) {
									if (!excludedRefsets
											.contains(nestedRefsetId)) {
										computedRefsets.add(nestedRefsetId);
									}
								}
							}

						}
					}
				} else {
					System.out.println("<<<<<< NOT REFSET SPEC: "
							+ child.getInitialText());
				}
			}

			termFactory.commit();

			getLog().info("Computed " + computedRefsets.size() + " refsets:");
			for (Integer computedRefset : computedRefsets) {
				getLog()
						.info(
								termFactory.getConcept(computedRefset)
										.getInitialText());
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	private boolean conceptIsRefsetSpec(I_GetConceptData concept)
			throws Exception {
		// check that it has a "specifies refset" relationship
		I_TermFactory termFactory = Terms.get();
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		I_GetConceptData specifiesRefsetRel = termFactory
				.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
		I_IntSet relType = termFactory.newIntSet();
		relType.add(specifiesRefsetRel.getConceptId());

		I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(config);
		I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

		if ((concept.getSourceRelTargets(currentStatuses, relType, config
				.getViewPositionSetReadOnly(), config.getPrecedence(), config
				.getConflictResolutionStrategy())).size() > 0) {
			return true;
		} else {
			return false;
		}
	}

}
