package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessPaths;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;
import org.dwfa.mojo.compare.MonitorComponents;

/**
 * Goal which monitors two branches for changes. Agreed changes are copied
 * to a new branch. Any encountered conflicts result in a html summary report
 * and text file containing a list of the conflicting concept identifiers.
 *
 * Optionally can check for flagged concept status (exclude
 * components from being copied if they are flagged).
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
		List<Match> matches = componentMonitor.checkConcept(concept);

		// check if the latest tuples are equal (excluding criteria)
		if (compareOperator.compare(matches)) {
			agreedChanges++;
			// copy latest attributes to new path/version

			Set<I_ConceptAttributeTuple> allConceptAttributeTuples = new HashSet<I_ConceptAttributeTuple>();
			Set<I_DescriptionTuple> allDescriptionTuples = new HashSet<I_DescriptionTuple>();
			Set<I_RelTuple> allRelationshipTuples = new HashSet<I_RelTuple>();

			Set<I_ConceptAttributeTuple> matchConceptAttributeTuples = new HashSet<I_ConceptAttributeTuple>();
			Set<I_DescriptionTuple> matchDescriptionTuples = new HashSet<I_DescriptionTuple>();
			Set<I_RelTuple> matchRelationshipTuples = new HashSet<I_RelTuple>();

			for (Match match: matches) {				
				allConceptAttributeTuples.addAll(match.matchConceptAttributeTuples);
				allDescriptionTuples.addAll(match.matchDescriptionTuples);
				allRelationshipTuples.addAll(match.matchRelationshipTuples);
				if (matchConceptAttributeTuples.size()==0) {
					matchConceptAttributeTuples.addAll(match.matchConceptAttributeTuples);
					matchDescriptionTuples.addAll(match.matchDescriptionTuples);
					matchRelationshipTuples.addAll(match.matchRelationshipTuples);
				}
			}

			for (I_ConceptAttributeTuple tuple: allConceptAttributeTuples) {
				I_ConceptAttributePart newPart = tuple.duplicatePart();
				newPart.setVersion(Integer.MAX_VALUE);
				newPart.setConceptStatus(updatedStatusId);
				tuple.getConVersioned().addVersion(newPart);
			}
			for (I_DescriptionTuple tuple: allDescriptionTuples) {
				I_DescriptionPart newPart = tuple.duplicatePart();
				newPart.setVersion(Integer.MAX_VALUE);
				newPart.setStatusId(updatedStatusId);
				newPart.setText(tuple.getText());
				tuple.getDescVersioned().addVersion(newPart);
			}
			for (I_RelTuple tuple: allRelationshipTuples) {
				I_RelPart newPart = tuple.duplicatePart();
				newPart.setVersion(Integer.MAX_VALUE);
				newPart.setStatusId(updatedStatusId);
				tuple.getRelVersioned().addVersion(newPart);
			}

			for (I_ConceptAttributeTuple tuple: matchConceptAttributeTuples) {
				I_ConceptAttributePart newPart = tuple.duplicatePart();
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPart.getConceptStatus())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPart.getConceptStatus())))) {
					changed = true;
					newPart.setVersion(Integer.MAX_VALUE);
					newPart.setPathId(copyToPath.getConceptId());
					newPart.setConceptStatus(updatedStatusOnNewPathId);
					tuple.getConVersioned().addVersion(newPart);
				}
			}
			// copy latest descriptions to new path/version
			for (I_DescriptionTuple tuple: matchDescriptionTuples) {
				I_DescriptionPart newPart = tuple.duplicatePart();
				descriptionCount++;
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPart.getStatusId())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPart.getStatusId())))) {
					changed = true;
					newPart.setVersion(Integer.MAX_VALUE);
					newPart.setPathId(copyToPath.getConceptId());					
					newPart.setStatusId(updatedStatusOnNewPathId);
					newPart.setText(tuple.getText());
					tuple.getDescVersioned().addVersion(newPart);
				} else {
					System.out.println("rejected description: " + tuple.getText());
					removedDescriptionCount++;
				}
			}
			// copy latest relationships to new path/version
			for (I_RelTuple tuple: matchRelationshipTuples) {
				I_RelPart newPart = tuple.duplicatePart();
				relationshipCount++;
				if ((rejectedStatus==null || (rejectedStatus!=null && !rejectedStatusIds.contains(newPart.getStatusId())))
						&& 
						(acceptedStatus==null || (acceptedStatus!=null && acceptedStatusIds.contains(newPart.getStatusId())))) {
					changed = true;
					newPart.setVersion(Integer.MAX_VALUE);
					newPart.setPathId(copyToPath.getConceptId());
					newPart.setStatusId(updatedStatusOnNewPathId);
					tuple.getRelVersioned().addVersion(newPart);
				} else {
					removedRelationshipCount++;
				}
			}
			if (changed) {
				termFactory.addUncommitted(concept);
			}

		} else {
			conflicts++;
			if (textWriter == null) {
				outputTextDirectory.mkdirs();
				textWriter = new BufferedWriter(new BufferedWriter(
						new FileWriter(outputTextDirectory + File.separator
								+ outputTextFileName)));
			}
			textWriter.append(concept.getUids().toString());
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
			copyToPath = termFactory.newPath(origins, copyToConcept);

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
						termFactory.newPath(origins, compareConcept),
						Integer.MAX_VALUE);
				positions.add(comparePosition);
			}
			componentMonitor.setPositions(positions);
			termFactory.iterateConcepts(this);

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

			System.out.println("Finished copying\nFound " + conflicts + " concepts with differences");
			System.out.println("Copied " + agreedChanges + " concepts out of a total of " +conceptCount+ "");
			System.out.println("Copied " + (descriptionCount-removedDescriptionCount) + " descriptions out of a total of " +descriptionCount+ "");
			System.out.println("Copied " + (relationshipCount-removedRelationshipCount) + " relationships out of a total of " +relationshipCount+ "");
			

			if (textWriter != null) {
				textWriter.close();
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	List<I_Path> originBasedPath = new LinkedList<I_Path>();
	public void processPath(I_Path path) throws Exception {

		Iterator<I_Position> it = path.getOrigins().iterator();
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
					System.out.println("the path is: " + termFactory.getConcept(path.getConceptId()).getDescriptions().iterator().next().getTuples().iterator().next().getText());
					totalBranches.add(termFactory.getConcept(path.getConceptId()));
				}
			}
		}

	}

}
