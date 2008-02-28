package org.dwfa.ace.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * 
 * @author Tore Fjellheim
 *
 */
public class MemberRefsetCalculator extends RefsetUtilities {

	private File outputDirectory;

	private boolean validateOnly = true;

	private I_TermFactory termFactory;

	/**
	 * The ids of the concepts which may be included in the member set (due to lineage).
	 * These may be excluded if they explicitly state a refset exclusion.
	 */
	private Map<Integer,Set<ConceptRefsetInclusionDetails>> newRefsetMembers = new HashMap<Integer,Set<ConceptRefsetInclusionDetails>>();

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	private Map<Integer,Set<ConceptRefsetInclusionDetails>> newRefsetExclusion = new HashMap<Integer,Set<ConceptRefsetInclusionDetails>>();

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	private Map<Integer,Set<ConceptRefsetInclusionDetails>> existingRefsetMembers = new HashMap<Integer,Set<ConceptRefsetInclusionDetails>>();

	/**
	 * The list of specification refsets to be analysed
	 */
	private List<Integer> allowedRefsets = new ArrayList<Integer>();


	private int includeLineage;
	private int includeIndividual;
	private int excludeLineage;
	private int excludeIndividual;

	private File conflictFile = null;
	private BufferedWriter conflictWriter = null;
	private File reportFile = null;
	private BufferedWriter reportWriter = null;

	private Map<Integer,List<Integer>> conceptsWithDirectInclusion = new HashMap<Integer,List<Integer>>();
	private Map<Integer,List<Integer>> conceptsWithDirectExclusion = new HashMap<Integer,List<Integer>>();

	public void run() {
		// TODO Auto-generated method stub
		termFactory = LocalVersionedTerminology.get();

		try {
			Map<Integer,ConceptRefsetInclusionDetails> currentRefsetInclusionType = new HashMap<Integer,ConceptRefsetInclusionDetails>();
			if (allowedRefsets.size()==0) {
				allowedRefsets = findAllowedRefsets();
			}
			setUp();

			/*
			 * Iterate over the concepts in the specification refset
			 * */

			for (Integer i: allowedRefsets) {


				int memberSetId = getMemberSetConcept(i).getConceptId();

				System.out.println("Checking refset: " + termFactory.getConcept(memberSetId));

				List<Integer> conceptsWithInclusion = new ArrayList<Integer>();
				List<Integer> conceptsWithExclusion = new ArrayList<Integer>();
				List<Integer> conceptsWithIncludeIndividual = new ArrayList<Integer>();
				List<Integer> conceptsWithExcludeIndividual = new ArrayList<Integer>();

				List<I_ThinExtByRefVersioned> refsetMembers = termFactory.getRefsetExtensionMembers(i);
				/*
				 * For each inclusion concept, find the type and keep a list of which are each type...reasons for inclusion
				 * 
				 * stop descending if child is in the exclusion lineage set
				 * 
				 * Skip children which are in the exclusion individual set
				 * */
				for (I_ThinExtByRefVersioned member : refsetMembers) {

					List<I_ThinExtByRefTuple> versions = member.getTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, false);					
					if (versions.size()==1) {
						int inclusiontype = getInclusionTypeForRefset(member);
						if (inclusiontype==this.includeLineage) {
							conceptsWithInclusion.add(member.getComponentId());						
						} else if (inclusiontype==this.excludeLineage) {
							conceptsWithExclusion.add(member.getComponentId());
						} else if (inclusiontype==this.includeIndividual) {
							conceptsWithIncludeIndividual.add(member.getComponentId());
						} else if (inclusiontype==this.excludeIndividual) {
							conceptsWithExcludeIndividual.add(member.getComponentId());
						}
					} 
				}
				conceptsWithDirectInclusion.put(memberSetId, conceptsWithInclusion);
				conceptsWithDirectExclusion.put(memberSetId, conceptsWithExclusion);


				for (Integer member : conceptsWithInclusion) {
					IncludeAllChildren(member,memberSetId,member);
				}

				/*
				 * Iterate over exclusions and find which ones are excluded 
				 * 
				 * stop descending if child is in the inclusion lineage set
				 * 
				 * skip children which are in the inclusion individual set
				 * */
				for (Integer member : conceptsWithExclusion) {
					removeFromRefsetInclusion(member,memberSetId);
					ExcludeAllChildren(member,memberSetId,member);
				}

				for (Integer member : conceptsWithIncludeIndividual) {
					IncludeConcept(member,memberSetId,member);
				}

				for (Integer member : conceptsWithExcludeIndividual) {
					ExcludeConcept(member,memberSetId,member);
				}

				List<I_ThinExtByRefVersioned> conceptsInMemberRefset = termFactory.getRefsetExtensionMembers(memberSetId);
				/*
				 * Add all members to the member refset so we know what was already there
				 * */
				for (I_ThinExtByRefVersioned member : conceptsInMemberRefset) {
					addToExistingRefsetMembers(new ConceptRefsetInclusionDetails(member.getComponentId(),includeIndividual,member.getComponentId()),memberSetId);
				}

			}

			setMembers();

			shutDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void IncludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
		addToRefsetMembers(new ConceptRefsetInclusionDetails(componentId,includeLineage,parentId),refsetId);
		removeFromRefsetExclusion(componentId,refsetId);
	}

	public void ExcludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
		addToRefsetExclusion(new ConceptRefsetInclusionDetails(componentId,includeLineage,parentId),refsetId);
		removeFromRefsetInclusion(componentId,refsetId);
	}

	private void ExcludeAllChildren(int componentId, int refsetId, int parentId) throws IOException, Exception {
		/*
		 * Make sure the concept isn't already directly included
		 * */
		if (!conceptsWithDirectInclusion.get(refsetId).contains(componentId)) {

			addToRefsetExclusion(new ConceptRefsetInclusionDetails(componentId,includeLineage,parentId),refsetId);

			List<Integer> children = this.getChildrenOfConcept(componentId);
			for (Integer i : children) {
				ExcludeAllChildren(i,refsetId,parentId);
			}
		} 

	}

	private void removeFromRefsetExclusion(int componentId, int refsetId) {
		Set<ConceptRefsetInclusionDetails> members = newRefsetExclusion.get(refsetId);
		if (members!=null) {
			members.remove(new ConceptRefsetInclusionDetails(componentId,includeLineage,0));			
		}
	}
	private void removeFromRefsetInclusion(int componentId, int refsetId) {
		Set<ConceptRefsetInclusionDetails> members = newRefsetMembers.get(refsetId);
		if (members!=null) {
			members.remove(new ConceptRefsetInclusionDetails(componentId,includeLineage,0));
		}
	}


	private void IncludeAllChildren(int componentId, int refsetId,int parentId) throws IOException, Exception {

		if (!conceptsWithDirectExclusion.get(refsetId).contains(componentId)) {

			addToRefsetMembers(new ConceptRefsetInclusionDetails(componentId,includeLineage,parentId),refsetId);

			List<Integer> children = this.getChildrenOfConcept(componentId);
			for (Integer i : children) {
				IncludeAllChildren(i,refsetId,parentId);
			}
		} 

	}

	public void addToExistingRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(existingRefsetMembers,conceptDetails,refset);
	}

	public void addToRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(newRefsetMembers,conceptDetails,refset);
	}

	public void addToRefsetExclusion(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(newRefsetExclusion,conceptDetails,refset);
	}



	private void shutDown() throws IOException {
		conflictWriter.close();
		reportWriter.close();
	}



	private void setUp() throws TerminologyException, IOException {
		typeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
		includeLineage = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
		includeIndividual = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
		excludeLineage = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
		excludeIndividual = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());

		retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
		currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());

		conflictFile = new File(outputDirectory.getAbsolutePath()+File.separatorChar+"reports","conflicts.txt");	
		reportFile = new File(outputDirectory.getAbsolutePath()+File.separatorChar+"classes","conceptsAddedToRefset.txt");
		reportFile.getParentFile().mkdirs();
		conflictFile.getParentFile().mkdirs();

		conflictWriter = new BufferedWriter(new FileWriter(conflictFile));
		reportWriter = new BufferedWriter(new FileWriter(reportFile));

	}

	private void setMembers() throws Exception {
		for (Integer refset : newRefsetMembers.keySet()) {
			reportWriter.write("\n\nIncluded members of refset " + termFactory.getConcept(refset) + " are: ");
			reportWriter.newLine();
			Set<ConceptRefsetInclusionDetails> newMembers = newRefsetMembers.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers) {
					reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}
		for (Integer refset : newRefsetExclusion.keySet()) {
			reportWriter.write("\n\nExcluded members of refset " + termFactory.getConcept(refset) + " are: ");
			reportWriter.newLine();
			Set<ConceptRefsetInclusionDetails> newMembers = newRefsetExclusion.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers) {
					reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}

		for (Integer refset : existingRefsetMembers.keySet()) {
			reportWriter.write("\n\nPrevious members of refset " + termFactory.getConcept(refset) + " are: ");
			reportWriter.newLine();
			Set<ConceptRefsetInclusionDetails> newMembers = existingRefsetMembers.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i : newMembers) {
					reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}


		for (Integer refset : newRefsetMembers.keySet()) {
			Set<ConceptRefsetInclusionDetails> exclusions = new HashSet<ConceptRefsetInclusionDetails>();

			boolean conflicts = false;
			conflictWriter.write("\n\nConflicts in refset " + termFactory.getConcept(refset) + " are: ");
			conflictWriter.newLine();
			Set<ConceptRefsetInclusionDetails> newMembers = newRefsetMembers.get(refset);
			Set<ConceptRefsetInclusionDetails> oldMembers = newRefsetExclusion.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers) {					
					if (oldMembers!=null && oldMembers.contains(i)) {
						for (ConceptRefsetInclusionDetails old: oldMembers) {
							if (old.equals(i)) {
								conflictWriter.write(termFactory.getConcept(i.getConceptId()).toString());
								conflictWriter.write(" because of " + termFactory.getConcept(i.getInclusionReasonId()).toString());
								conflictWriter.write(" conflicts with " +termFactory.getConcept(old.getInclusionReasonId()).toString());
								conflictWriter.newLine();
							}
						}
						conflicts = true;
					}
				}
			}


			reportWriter.write("\n\nNew included members of refset " + termFactory.getConcept(refset) + " are: ");
			reportWriter.newLine();
			newMembers = newRefsetMembers.get(refset);
			oldMembers = existingRefsetMembers.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers) {					
					if (oldMembers==null || !oldMembers.contains(i)) {
						reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
						reportWriter.newLine();
						if (!conflicts && !validateOnly) { 
							addToMemberSet(i.getConceptId(), i.getInclusionTypeId(), refset);
						}
					}
				}


				reportWriter.write("\n\nNew excluded members who used to be members of refset " + termFactory.getConcept(refset) + " are: ");
				reportWriter.newLine();
				newMembers = existingRefsetMembers.get(refset);
				oldMembers = newRefsetMembers.get(refset);
				if (newMembers!=null) {
					for (ConceptRefsetInclusionDetails i: newMembers) {					
						if (oldMembers==null || !oldMembers.contains(i)) {
							exclusions.add(i);
						}
					}
				}

				newMembers = existingRefsetMembers.get(refset);
				oldMembers = newRefsetExclusion.get(refset);
				if (newMembers!=null) {
					for (ConceptRefsetInclusionDetails i: newMembers) {					
						if (oldMembers!=null && oldMembers.contains(i)) {
							exclusions.add(i);
						}
					}
				}
				for (ConceptRefsetInclusionDetails i : exclusions) {
					reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
					if (!conflicts  && !validateOnly) { 
						I_ThinExtByRefVersioned ext = getExtensionForComponent(i.getConceptId(),refset);
						if (ext!=null) {
							retireLatestExtension(ext);
						} else {
							System.out.println("No extension exists with this refset id for this component");
						}
					}
				}
			}else {
				System.out.println("\nConflicts exist in refset " + termFactory.getConcept(refset));
				System.out.println("No changes made\n");
			}
		} 
	}


	private I_ThinExtByRefVersioned getExtensionForComponent(int conceptId,
			Integer refset) throws IOException {
		
		List<I_ThinExtByRefVersioned> exts = termFactory.getAllExtensionsForComponent(conceptId);
		for (I_ThinExtByRefVersioned ext : exts) {
			if (ext.getRefsetId()==refset.intValue()) {
				return ext;
			}
		}
		return null;
	}


	public File getOutputDirectory() {
		return outputDirectory;
	}


	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}


	public boolean isValidateOnly() {
		return validateOnly;
	}


	public void setValidateOnly(boolean validateOnly) {
		this.validateOnly = validateOnly;
	}


	public List<Integer> getAllowedRefsets() {
		return allowedRefsets;
	}


	public void setAllowedRefsets(List<Integer> allowedRefsets) {
		this.allowedRefsets = allowedRefsets;
	}


}
