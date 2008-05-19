package org.dwfa.ace.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
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

	private int commitSize = 1000;
	
	private boolean useNonTxInterface = false;

	private File outputDirectory;

	private boolean validateOnly = true;
	private boolean markParents = true;

	protected I_TermFactory termFactory;

	/**
	 * The ids of the concepts which may be included in the member set (due to lineage).
	 * These may be excluded if they explicitly state a refset exclusion.
	 */
	protected Map<Integer,ClosestDistanceHashSet> newRefsetMembers = new HashMap<Integer,ClosestDistanceHashSet>();

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	protected Map<Integer,ClosestDistanceHashSet> newRefsetExclusion = new HashMap<Integer,ClosestDistanceHashSet>();

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	private Map<Integer,ClosestDistanceHashSet> existingRefsetMembers = new HashMap<Integer,ClosestDistanceHashSet>();

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	private Map<Integer,ClosestDistanceHashSet> existingParentMembers = new HashMap<Integer,ClosestDistanceHashSet>();

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

	private MemberRefsetChangesetWriter nonTxWriter;
	
	public void run() {
		// TODO Auto-generated method stub
		termFactory = LocalVersionedTerminology.get();
		
		try {
			
			if (useNonTxInterface) {
				nonTxWriter = new MemberRefsetChangesetWriter(outputDirectory.getAbsolutePath(), termFactory, 
						ConceptConstants.AU_CT_EDIT_PATH.localize().getUids().iterator().next());
			}			
			
			if (allowedRefsets.size()==0) {
				allowedRefsets = getSpecificationRefsets();
			}
			setUp();

			/*
			 * Iterate over the concepts in the specification refset
			 * */

			for (Integer i: allowedRefsets) {

				int memberSetId = getMemberSetConcept(i).getConceptId();

				I_GetConceptData memberSet = getConcept(memberSetId);
				System.out.println("Checking refset: " + memberSet);

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

					I_GetConceptData concept = termFactory.getConcept(member.getComponentId());
					System.out.println("getting versions for " + concept);
					List<I_ThinExtByRefTuple> versions = member.getTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, false);
					System.out.println("done getting versions for " + concept + " they were " + versions);
					
					if (versions.size()>=1) {
						int inclusiontype = getInclusionTypeForRefset(member);
						if (inclusiontype==this.includeLineage) {
							System.out.println(memberSet + " include lineage: " + concept);
							conceptsWithInclusion.add(member.getComponentId());
						} else if (inclusiontype==this.excludeLineage) {
							System.out.println(memberSet + " exclude lineage: " + concept);
							conceptsWithExclusion.add(member.getComponentId());
						} else if (inclusiontype==this.includeIndividual) {
							System.out.println(memberSet + " include ind: " + concept);
							conceptsWithIncludeIndividual.add(member.getComponentId());
						} else if (inclusiontype==this.excludeIndividual) {
							System.out.println(memberSet + " exclude ind: " + concept);
							conceptsWithExcludeIndividual.add(member.getComponentId());
						}
					} 
				}
				System.out.println("Done calcuating for refset " + memberSet + " - commencing update");
				
				conceptsWithDirectInclusion.put(memberSetId, conceptsWithInclusion);
				conceptsWithDirectExclusion.put(memberSetId, conceptsWithExclusion);
				
				for (Integer member : conceptsWithInclusion) {
					System.out.println("getting children for lineage include for concept " + termFactory.getConcept(member) + " for member set " + memberSet);
					IncludeAllChildren(member,memberSetId,member,0);
				}

				/*
				 * Iterate over exclusions and find which ones are excluded 
				 * 
				 * stop descending if child is in the inclusion lineage set
				 * 
				 * skip children which are in the inclusion individual set
				 * */
				for (Integer member : conceptsWithExclusion) {
					System.out.println("getting children for lineage exclude for concept " + termFactory.getConcept(member) + " for member set " + memberSet);
					removeFromRefsetInclusion(member,memberSetId);
					ExcludeAllChildren(member,memberSetId,member,0);
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
				int parent_marker_nid = ConceptConstants.PARENT_MARKER.localize().getNid();

				System.out.println("collecting existing refset members for comparison");
				int counter = 0;
				for (I_ThinExtByRefVersioned member : conceptsInMemberRefset) {
					counter++;
					if (counter % 1000 == 0) {
						System.out.println("processed " + counter + " of " + conceptsInMemberRefset.size() + " for refset " + memberSet);
					}
					
					I_ThinExtByRefPart latest = getLatestVersion(member);
					if (latest!=null && latest.getStatus()== currentStatusId) {
						I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) latest;
						if (part.getConceptId()!=parent_marker_nid) {
							addToExistingRefsetMembers(new ConceptRefsetInclusionDetails(member.getComponentId(),includeIndividual,member.getComponentId(),0),memberSetId);
						}  else {
							addToExistingParentMembers(new ConceptRefsetInclusionDetails(member.getComponentId(),includeIndividual,member.getComponentId(),0),memberSetId);
						}
					}
				}

			}

			setMembers();

			shutDown();
		} catch (Exception e) {
			throw new RuntimeException("Member refset generation failed with exception", e);
		}
	}


	public void IncludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
		addToRefsetMembers(new ConceptRefsetInclusionDetails(componentId,includeIndividual,parentId,0),refsetId);
		removeFromRefsetExclusion(componentId,refsetId);
	}

	public void ExcludeConcept(int componentId, int refsetId, int parentId) throws TerminologyException, IOException {
		addToRefsetExclusion(new ConceptRefsetInclusionDetails(componentId,excludeIndividual,parentId,0),refsetId);
		removeFromRefsetInclusion(componentId,refsetId);
	}

	private void ExcludeAllChildren(int componentId, int refsetId, int parentId, int distance) throws IOException, Exception {
		/*
		 * Make sure the concept isn't already directly included
		 * */
		if (!conceptsWithDirectInclusion.get(refsetId).contains(componentId)) {

			addToRefsetExclusion(new ConceptRefsetInclusionDetails(componentId,excludeLineage,parentId,distance),refsetId);

			List<Integer> children = this.getChildrenOfConcept(componentId);
			distance++;

			for (Integer i : children) {
				ExcludeAllChildren(i,refsetId,parentId,distance);
			}
		} 

	}

	private void removeFromRefsetExclusion(int componentId, int refsetId) {
		ClosestDistanceHashSet members = newRefsetExclusion.get(refsetId);
		if (members!=null) {
			members.remove(new ConceptRefsetInclusionDetails(componentId,includeLineage,0,0));			
		}
	}
	private void removeFromRefsetInclusion(int componentId, int refsetId) {
		ClosestDistanceHashSet members = newRefsetMembers.get(refsetId);
		if (members!=null) {
			members.remove(new ConceptRefsetInclusionDetails(componentId,includeLineage,0,0));
		}
	}


	private void IncludeAllChildren(int componentId, int refsetId,int parentId, int distance) throws IOException, Exception {
		if (!conceptsWithDirectExclusion.get(refsetId).contains(componentId)) {

			addToRefsetMembers(new ConceptRefsetInclusionDetails(componentId,includeLineage,parentId,distance),refsetId);

			List<Integer> children = this.getChildrenOfConcept(componentId);
			distance++;
			for (Integer i : children) {
				IncludeAllChildren(i,refsetId,parentId, distance);
			}
		} 

	}

	public void addToExistingRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(existingRefsetMembers,conceptDetails,refset);
	}

	public void addToExistingParentMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(existingParentMembers,conceptDetails,refset);
	}

	public void addToRefsetMembers(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(newRefsetMembers,conceptDetails,refset);
	}

	public void addToRefsetExclusion(ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		addToNestedSet(newRefsetExclusion,conceptDetails,refset);
	}



	private void shutDown() throws Exception {
		conflictWriter.close();
		reportWriter.close();
		if (useNonTxInterface) {
			nonTxWriter.close();
		}
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

	protected void setMembers() throws Exception {

		System.out.println("Starting reporting " + new Date());
		for (Integer refset : newRefsetMembers.keySet()) {
			reportWriter.write("\n\nIncluded members of refset " + getConcept(refset) + " are: ");
			reportWriter.newLine();
			ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers.values()) {
					reportWriter.write(getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}
		for (Integer refset : newRefsetExclusion.keySet()) {
			reportWriter.write("\n\nExcluded members of refset " + getConcept(refset) + " are: ");
			reportWriter.newLine();
			ClosestDistanceHashSet newMembers = newRefsetExclusion.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i: newMembers.values()) {
					reportWriter.write(getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}

		for (Integer refset : existingRefsetMembers.keySet()) {
			reportWriter.write("\n\nPrevious members of refset " + getConcept(refset) + " are: ");
			reportWriter.newLine();
			ClosestDistanceHashSet newMembers = existingRefsetMembers.get(refset);
			if (newMembers!=null) {
				for (ConceptRefsetInclusionDetails i : newMembers.values()) {
					reportWriter.write(getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
				}
			}
		}


		for (Integer refset : newRefsetMembers.keySet()) {
			ClosestDistanceHashSet exclusions = new ClosestDistanceHashSet();


			ClosestDistanceHashSet oldparents = existingParentMembers.get(refset);
			ClosestDistanceHashSet parents = findParentsToBeMarked(newRefsetMembers.get(refset));

			conflictWriter.write("\n\nConflicts in refset " + getConcept(refset) + " are: ");
			conflictWriter.newLine();
			ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
			ClosestDistanceHashSet oldMembers = newRefsetExclusion.get(refset);
			ClosestDistanceHashSet newMembersToBeRemoved = new ClosestDistanceHashSet();
			ClosestDistanceHashSet oldMembersToBeRemoved = new ClosestDistanceHashSet();
			if (newMembers!=null && oldMembers != null) {
				Set<Integer> keySet = new HashSet<Integer>();
				keySet.addAll(newMembers.keySet());
				keySet.retainAll(oldMembers.keySet());
				for (Integer key : keySet) {
					ConceptRefsetInclusionDetails newMember = newMembers.get(key);
					ConceptRefsetInclusionDetails old = oldMembers.get(key);
					if (newMember.getDistance() > old.getDistance()) {
						conflictWriter.write("Resolving to exclusion: ");
						newMembersToBeRemoved.add(newMember);
					} else if (newMember.getDistance() < old.getDistance()) {
						conflictWriter.write("Resolving to inclusion: ");
						oldMembersToBeRemoved.add(old);
					} else if (newMember.getDistance() == old.getDistance()) {
						//this is a concept conflicting with itself!
						conflictWriter.write("Distance to instructions is equal at " + newMember.getDistance() + ", resolving to exclude: ");
						newMembersToBeRemoved.add(newMember);
					}
					conflictWriter.write(getConcept(newMember.getConceptId()).toString());
					conflictWriter.write(" because of " + getConcept(newMember.getInclusionReasonId()).toString());
					conflictWriter.write(" conflicts with " +getConcept(old.getInclusionReasonId()).toString());
					conflictWriter.newLine();
				}
			}

			if (newMembers!=null && newMembersToBeRemoved!=null) {
				newMembers.removeAll(newMembersToBeRemoved);
			}
			if (oldMembers!=null && oldMembersToBeRemoved!=null) {
				oldMembers.removeAll(oldMembersToBeRemoved);
			}

			reportWriter.write("\n\nNew included members of refset " + getConcept(refset) + " are: ");
			reportWriter.newLine();
			newMembers = newRefsetMembers.get(refset);
			oldMembers = existingRefsetMembers.get(refset);
			if (newMembers!=null) {
				Set<Integer> keySet = new HashSet<Integer>();
				keySet.addAll(newMembers.keySet());
				if (oldMembers != null) {
					keySet.removeAll(oldMembers.keySet());
				}
				
				int count = 0;
				long sysTime = System.currentTimeMillis();
				for (Integer conceptId: keySet) {
					count++;
					if (count % commitSize == 0) {
						System.out.println("adding member " + count + " of " + keySet.size() + " (" + (System.currentTimeMillis() - sysTime) + ")");
						sysTime = System.currentTimeMillis();
					}
					if (termFactory.getUncommitted().size() > commitSize) {
						termFactory.commit();
					}
					ConceptRefsetInclusionDetails member = newMembers.get(conceptId);
					reportWriter.write(getConcept(member.getConceptId()).toString());
					reportWriter.newLine();
					if (!validateOnly) {
						if (useNonTxInterface) {
							nonTxWriter.addToRefset(member.getConceptId(), getMembershipType(member.getInclusionTypeId()), 
									refset, currentStatusId);
						} else {
							addToMemberSet(member.getConceptId(), member.getInclusionTypeId(), refset);
						}
					}
				}

				newMembers = existingRefsetMembers.get(refset);
				oldMembers = newRefsetMembers.get(refset);

				if (newMembers!=null) {
					keySet = new HashSet<Integer>();
					keySet.addAll(newMembers.keySet());
					if (oldMembers != null) {
						keySet.removeAll(oldMembers.keySet());
					}
					for (Integer conceptId: keySet) {
						exclusions.add(newMembers.get(conceptId));
					}
				}

				newMembers = existingRefsetMembers.get(refset);
				oldMembers = newRefsetExclusion.get(refset);
				if (newMembers!=null) {
					keySet = new HashSet<Integer>();
					keySet.addAll(newMembers.keySet());
					if (oldMembers != null) {
						keySet.retainAll(oldMembers.keySet());
					}
					for (Integer key: keySet) {
						exclusions.add(newMembers.get(key));
					}
				}
				reportWriter.write("\n\nNew excluded members who used to be members of refset " + getConcept(refset) + " are: ");
				reportWriter.newLine();
				for (ConceptRefsetInclusionDetails i : exclusions.values()) {
					reportWriter.write(getConcept(i.getConceptId()).toString());
					reportWriter.newLine();
					if (!validateOnly) { 
						I_ThinExtByRefVersioned ext = getExtensionForComponent(i.getConceptId(),refset);
						if (ext!=null) {
							if (useNonTxInterface) {
								nonTxWriter.addToRefset(i.getConceptId(), ((I_ThinExtByRefPartConcept) ext).getConceptId(), refset, retiredConceptId);
							} else {
								retireLatestExtension(ext);
							}
						} else {
							System.out.println("No extension exists with this refset id for this component");
						}
					}
				}

				if (markParents) {

					if (existingRefsetMembers.get(refset)!=null && oldparents!=null) {
						oldparents.removeAll(existingRefsetMembers.get(refset));
					}
					if (newRefsetMembers.get(refset)!=null && parents!=null) {
						parents.removeAll(newRefsetMembers.get(refset));
					}

					reportWriter.write("\n\nParents that are not marked but will be marked in refset " + getConcept(refset) + " are: ");
					reportWriter.newLine();
					for (ConceptRefsetInclusionDetails parent: parents.values()) {
						if (oldparents==null || (oldparents!=null && !oldparents.containsKey(parent.getConceptId()))) {
							if (!validateOnly) {
								if (useNonTxInterface) {
									nonTxWriter.addToRefset(parent.getConceptId(), 
											ConceptConstants.PARENT_MARKER.localize().getNid(), refset, currentStatusId);
								} else {
									addToMemberSetAsParent(parent.getConceptId(), refset);
								}
								reportWriter.write(getConcept(parent.getConceptId()).toString());
								reportWriter.newLine();						
							}
						} else {
							reportWriter.write(getConcept(parent.getConceptId()).toString() + " ------- is already marked as parent");
							reportWriter.newLine();						
						}
					}
					if (oldparents!=null) {
						oldparents.removeAll(parents);			
						for (ConceptRefsetInclusionDetails existingParent: oldparents.values()) {
							I_ThinExtByRefVersioned ext = getExtensionForComponent(existingParent.getConceptId(),refset);
							if (ext!=null) {
								if (!validateOnly) {
									retireLatestExtension(ext);
								}
							} else {
								System.out.println("No extension exists with this refset id for this component : " + getConcept(existingParent.getConceptId()).toString());
							}
							reportWriter.write(getConcept(existingParent.getConceptId()).toString() + " ------- to be retired");
							reportWriter.newLine();
						}
					}
				}


			}
		} 
	}

	private ClosestDistanceHashSet findParentsToBeMarked(ClosestDistanceHashSet concepts) throws IOException, Exception {
		ClosestDistanceHashSet nonMarkedParents = new ClosestDistanceHashSet();

		if (concepts!=null) {
			int count = 0;
			long sysTime = System.currentTimeMillis();
			for (ConceptRefsetInclusionDetails conceptId: concepts.values()) {
				count++;
				if (count % commitSize == 0) {
					System.out.println("finding parent to be marked " + count + " of " + concepts.size() + " (" + (System.currentTimeMillis() - sysTime) + ")");
					sysTime = System.currentTimeMillis();
				}
				Set<Integer> parents = getAncestorsOfConcept(conceptId.getConceptId(), concepts);
				for (Integer parentId: parents) {
					ConceptRefsetInclusionDetails parent = new ConceptRefsetInclusionDetails(parentId,0,0,0);
					if (!concepts.containsKey(parent.getConceptId())) {
						nonMarkedParents.add(parent);
					}
				}
			}
		}
		return nonMarkedParents;		
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


	public boolean isMarkParents() {
		return markParents;
	}


	public void setMarkParents(boolean markParents) {
		this.markParents = markParents;
	}


	public File getConflictFile() {
		return conflictFile;
	}


	public void setConflictFile(File conflictFile) {
		this.conflictFile = conflictFile;
	}


	public File getReportFile() {
		return reportFile;
	}


	public void setReportFile(File reportFile) {
		this.reportFile = reportFile;
	}


	public int getCommitSize() {
		return commitSize;
	}


	public void setCommitSize(int commitSize) {
		this.commitSize = commitSize;
	}


	public boolean getUseNonTxInterface() {
		return useNonTxInterface;
	}


	public void setUseNonTxInterface(boolean useNonTxInterface) {
		this.useNonTxInterface = useNonTxInterface;
	}


}
