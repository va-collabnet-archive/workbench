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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessPaths;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;
import org.dwfa.mojo.compare.MonitorComponents;

/**
 * Goal which monitors two branches for changes. Agreed changes are copied
 * to a new branch. Any encountered conflicts are not copied. 
 *
 * @goal vodb-copy-branches
 *
 * @requiresDependencyResolution compile
 * @author Tore Fjellheim
 */
public class VodbCopyBranches extends AbstractMojo implements I_ProcessConcepts, I_ProcessPaths {

	/**
	 * Branch to which the compared branches will be copied to, if they
	 * agree on the value.
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor branchToCopyTo;

	/**
	 * The updated status of any copied concepts.
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor updatedStatus;

	/**
	 * The updated status of any copied concepts.
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor updatedStatusOnNewPath;

	/**
	 * Branches which will be compared.
	 * @parameter
	 */
	private ConceptDescriptor[] branchesToCompare;

	/**
	 * Branches which will be compared.
	 * @parameter
	 */
	private ConceptDescriptor[] branchOrigins;

	/**
	 * The flagged status that will be rejected
	 * @parameter
	 */
	private ConceptDescriptor[] acceptedStatus = null;

	/**
	 * The flagged status that will be rejected
	 * @parameter
	 */
	private ConceptDescriptor[] rejectedStatus = null;

	/**
	 * The html output file location.
	 * @parameter expression="${project.build.directory}/classes"
	 */
	private File outputHtmlDirectory;

	/**
	 * The html output file name.
	 * @parameter
	 */
	private String outputHtmlFileName = "conflict_report.html";

	/**
	 * The text file output location.
	 * @parameter expression="${project.build.directory}/classes"
	 */
	private File outputTextDirectory;

	/**
	 * The text file containing uuids file name.
	 * @parameter
	 */
	private String outputTextFileName = "conflict_uuids.txt";

	/**
	 * Compare Operator used to test if the concepts should be copied to the new path
	 * @parameter
	 * @required
	 */
	private CompareOperator compareOperator;

	/**
	 * If this parameter is set, then the system will only check one position at a time, rather than comparing
	 * paths for differences. This is useful if the only requirement is to check if one path has a particular status
	 * set for a concept
	 * @parameter
	 */
	private boolean checkSinglePath = false;
	
	
	I_Path copyToPath;

	int agreedChanges = 0;
	int conflicts = 0;
	int conceptCount = 0;
	int descriptionCount = 0;
	int relationshipCount = 0;
	int removedDescriptionCount = 0;
	int removedRelationshipCount = 0;

	MonitorComponents componentMonitor = null; 
	I_TermFactory termFactory = null;
	BufferedWriter textWriter = null;
	int updatedStatusId;
	int updatedStatusOnNewPathId;
	List<I_GetConceptData> totalBranches = new LinkedList<I_GetConceptData>();
	List<Integer> rejectedStatusIds = new LinkedList<Integer>();
	List<Integer> acceptedStatusIds = new LinkedList<Integer>();

	public void processConcept(I_GetConceptData concept) throws Exception {
		boolean changed = false;
		List<Match> matches = componentMonitor.checkConcept(concept, acceptedStatusIds);
		// check if the latest tuples are equal (excluding criteria)
		if (compareOperator.compare(matches)) {
			// copy latest attributes to new path/version

			Set<I_ConceptAttributeTuple> matchConceptAttributeTuples = new HashSet<I_ConceptAttributeTuple>();
			Set<I_DescriptionTuple> matchDescriptionTuples = new HashSet<I_DescriptionTuple>();
			Set<I_RelTuple> matchRelationshipTuples = new HashSet<I_RelTuple>();

			Match match = matches.get(0);				
			matchConceptAttributeTuples.addAll(match.matchConceptAttributeTuples);
			matchDescriptionTuples.addAll(match.matchDescriptionTuples);
			matchRelationshipTuples.addAll(match.matchRelationshipTuples);

			for (I_ConceptAttributeTuple tuple: matchConceptAttributeTuples) {
				I_ConceptAttributePart newPartOnNewPath = tuple.duplicatePart();
								
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPartOnNewPath.getConceptStatus())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPartOnNewPath.getConceptStatus())))) {
					changed = true;
					newPartOnNewPath.setVersion(Integer.MAX_VALUE);
					newPartOnNewPath.setPathId(copyToPath.getConceptId());
					newPartOnNewPath.setConceptStatus(updatedStatusOnNewPathId);
					tuple.getConVersioned().addVersion(newPartOnNewPath);
					for (I_GetConceptData cd : totalBranches) {
						I_ConceptAttributePart newPart = tuple.duplicatePart();
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setPathId(cd.getConceptId());
						newPart.setConceptStatus(updatedStatusId);
						tuple.getConVersioned().addVersion(newPart);
					}					
				}
			}
			// copy latest descriptions to new path/version
			for (I_DescriptionTuple tuple: matchDescriptionTuples) {
				I_DescriptionPart newPartOnNewPath = tuple.duplicatePart();
				descriptionCount++;
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPartOnNewPath.getStatusId())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPartOnNewPath.getStatusId())))) {
					changed = true;
					newPartOnNewPath.setVersion(Integer.MAX_VALUE);
					newPartOnNewPath.setPathId(copyToPath.getConceptId());					
					newPartOnNewPath.setStatusId(updatedStatusOnNewPathId);
					newPartOnNewPath.setText(tuple.getText());
					tuple.getDescVersioned().addVersion(newPartOnNewPath);
					for (I_GetConceptData cd : totalBranches) {
						I_DescriptionPart newPart = tuple.duplicatePart();
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setPathId(cd.getConceptId());
						newPart.setStatusId(updatedStatusId);
						tuple.getDescVersioned().addVersion(newPart);
					}
				} else {
					//System.out.println("rejected description: " + tuple.getText());
					removedDescriptionCount++;
				}
			}
			// copy latest relationships to new path/version
			for (I_RelTuple tuple: matchRelationshipTuples) {
				I_RelPart newPartOnNewPath = tuple.duplicatePart();
				relationshipCount++;
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPartOnNewPath.getStatusId())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPartOnNewPath.getStatusId())))) {
					changed = true;
					newPartOnNewPath.setVersion(Integer.MAX_VALUE);
					newPartOnNewPath.setPathId(copyToPath.getConceptId());
					newPartOnNewPath.setStatusId(updatedStatusOnNewPathId);					
					tuple.getRelVersioned().addVersion(newPartOnNewPath);
					for (I_GetConceptData cd : totalBranches) {
						I_RelPart newPart = tuple.duplicatePart();
						newPart.setVersion(Integer.MAX_VALUE);
						newPart.setPathId(cd.getConceptId());
						newPart.setStatusId(updatedStatusId);
						tuple.getRelVersioned().addVersion(newPart);
					}	
				} else {
					removedRelationshipCount++;
				}
			}
			if (changed) {
				agreedChanges++;				
				termFactory.addUncommitted(concept);
			} else {
				conflicts++;
			}

		} else {
			if (textWriter == null) {
				outputTextDirectory.mkdirs();
				textWriter = new BufferedWriter(new BufferedWriter(
						new FileWriter(outputTextDirectory + File.separator
								+ outputTextFileName)));
			}
			textWriter.append(concept.getUids().toString());
			conflicts++;
		}

		conceptCount++;
	}


	public void execute() throws MojoExecutionException, MojoFailureException {
		componentMonitor = new MonitorComponents();
		termFactory = LocalVersionedTerminology.get();
		try {

			CompareComponents.reject = false;
			if (branchOrigins != null) {
				termFactory.iteratePaths(this);
			}

			if (rejectedStatus!=null) {
				for (ConceptDescriptor rejectedstatus : rejectedStatus) {
					rejectedStatusIds.add(termFactory.getConcept(
							rejectedstatus.getVerifiedConcept().getUids()).getConceptId());
				}
			}
			if (acceptedStatus!=null) {
				for (ConceptDescriptor acceptedstatus : acceptedStatus) {
					acceptedStatusIds.add(termFactory.getConcept(
							acceptedstatus.getVerifiedConcept().getUids()).getConceptId());
				}
			}


			updatedStatusId = termFactory.getConcept(
					updatedStatus.getVerifiedConcept().getUids()).getConceptId();

			updatedStatusOnNewPathId = termFactory.getConcept(
					updatedStatusOnNewPath.getVerifiedConcept().getUids()).getConceptId();

			I_Path architectonicPath = termFactory.getPath(
					ArchitectonicAuxiliary.
					Concept.ARCHITECTONIC_BRANCH.getUids());
			I_Position latestOnArchitectonicPath = termFactory.newPosition(
					architectonicPath,
					Integer.MAX_VALUE);
			Set<I_Position> origins = new HashSet<I_Position>();
			origins.add(latestOnArchitectonicPath);

			// get the branch to copy to concept/path
			I_GetConceptData copyToConcept =
				branchToCopyTo.getVerifiedConcept();
			copyToPath = termFactory.getPath(copyToConcept.getUids());

			// get all the positions for the branches to be compared
			List<I_Position> positions = new LinkedList<I_Position>();
			if (branchesToCompare!=null) {
				for (ConceptDescriptor branch : branchesToCompare) {
					I_GetConceptData compareConcept = branch.getVerifiedConcept();
					totalBranches.add(compareConcept);
				}
			}

			for (I_GetConceptData compareConcept : totalBranches) {

				I_Position comparePosition = termFactory.newPosition(
						termFactory.getPath(compareConcept.getUids()),
						Integer.MAX_VALUE);
				positions.add(comparePosition);
			}
			/*
			 * Compare concepts against all  positions 
			 * */
			if (!checkSinglePath) {
				componentMonitor.setPositions(positions);
				termFactory.iterateConcepts(this);

				System.out.println("Finished copying\nFound " + conflicts + " concepts with differences");
				System.out.println("Copied " + agreedChanges + " concepts out of a total of " +conceptCount+ "");
				System.out.println("Copied " + (descriptionCount-removedDescriptionCount) + " descriptions out of a total of " +descriptionCount+ "");
				System.out.println("Copied " + (relationshipCount-removedRelationshipCount) + " relationships out of a total of " +relationshipCount+ "");

			}
			
			/*
			 * do one and one
			 * */
			if (checkSinglePath) {
				for (int i = 0; i < positions.size(); i++) {
					List<I_Position> pos = new LinkedList<I_Position>();
					pos.add(positions.get(i));
					componentMonitor.setPositions(pos);
					conceptCount = 0;
					descriptionCount = 0;
					relationshipCount = 0;
					removedDescriptionCount = 0;
					removedRelationshipCount = 0;
					agreedChanges = 0;
					
					termFactory.iterateConcepts(this);
					System.out.println("For PATH: " + termFactory.getConcept(positions.get(i).getPath().getConceptId()));
					System.out.println("Copied " + agreedChanges + " concepts out of a total of " +conceptCount+ "");
					System.out.println("Copied " + (descriptionCount-removedDescriptionCount) + " descriptions out of a total of " +descriptionCount+ "");
					System.out.println("Copied " + (relationshipCount-removedRelationshipCount) + " relationships out of a total of " +relationshipCount+ "");

				}
			}

			if (conflicts > 0) {
				outputHtmlDirectory.mkdirs();
				BufferedWriter htmlWriter = new BufferedWriter(
						new BufferedWriter(new FileWriter(
								outputHtmlDirectory
								+ File.separator
								+ outputHtmlFileName)));
				htmlWriter.append("Monitored "
						+ conceptCount + " components.");
				htmlWriter.append("<br>");
				htmlWriter.append("Number of agreed changes: "
						+ agreedChanges);
				htmlWriter.append("<br>");
				htmlWriter.append("Number of conflicts: "
						+ conflicts);

				htmlWriter.close();
			}



			if (textWriter != null) {
				textWriter.close();
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	public void processPath(I_Path path) throws Exception {
		Set<I_Position> allOrigins = new HashSet<I_Position>();		
		List<I_Position> origins = new LinkedList<I_Position>();
		origins.addAll(path.getOrigins());
		int size = 0;
		while (origins.size()!=size) {
			I_Position origin = origins.get(size);
			allOrigins.add(origin);
			size++;

			List<I_Position> childOrigins = origin.getPath().getOrigins();
			origins.addAll(childOrigins);
		}

		Iterator<I_Position> it = allOrigins.iterator();

		while (it.hasNext()) {
			I_Position pos = it.next();
			int pathId = pos.getPath().getConceptId();
			for (int i = 0; i < branchOrigins.length; i++) {
				int originId = branchOrigins[i].getVerifiedConcept().getConceptId();
				if (pathId == originId) {
					/*
					 * This path needs to be checked because
					 * it has a origin which is in the list of specified
					 * origins
					 * */
					totalBranches.add(termFactory.getConcept(path.getConceptId()));
				}
			}
		}

	}

}
