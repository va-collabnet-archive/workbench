package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.ConceptConstants;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;

import com.sun.tools.javac.util.Pair;

/**
 * 
 * @author Tore Fjellheim
 *
 * @goal createMemberRefsetFromSpec 
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class CreateMemberRefsetFromSpec extends AbstractMojo {


	/**
	 * @parameter
	 * @required
	 * The concept descriptor for the member set path.
	 */

	private ConceptDescriptor memberSetPathDescriptor;
	/**
	 * @parameter
	 * @required
	 * The root concept.
	 */
	private ConceptDescriptor rootDescriptor;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
	

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

	/**
	 * The ids of the concepts which may be excluded from the member set (due to lineage).
	 * These may be included if they explicitly state a refset inclusion.
	 */
	private Map<Integer,List<ConflictPair<ConceptRefsetInclusionDetails>>> conflicts = new HashMap<Integer,List<ConflictPair<ConceptRefsetInclusionDetails>>>();


	private int includeLineage;
	private int includeIndividual;
	private int excludeLineage;
	private int excludeIndividual;
	private int retiredConceptId;
	private int currentStatusId;
	private int typeId;

	private File conflictFile = null;
	private BufferedWriter conflictWriter = null;
	private File reportFile = null;
	private BufferedWriter reportWriter = null;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		termFactory = LocalVersionedTerminology.get();
		try {
			System.out.println(outputDirectory);
			Map<Integer,ConceptRefsetInclusionDetails> currentRefsetInclusionType = new HashMap<Integer,ConceptRefsetInclusionDetails>();
			findAllowedRefsets();
			setUp();
			processConcept(rootDescriptor.getVerifiedConcept(),currentRefsetInclusionType);
			setMembers();
			shutDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage());
		}
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

	public int getInclusionTypeForRefset(I_ThinExtByRefVersioned part) {
		int typeId = 0;
		List<? extends I_ThinExtByRefPart> versions = part.getVersions();
		for (I_ThinExtByRefPart version : versions) {
			I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) version;
			typeId = temp.getConceptId();
			getLog().debug("processConcept(I_GetConceptData) - determining type version " 
					+ temp.getVersion() + " type now " + typeId);
		}

		return typeId;
	}

	public void addToNestedSet(Map<Integer,Set<ConceptRefsetInclusionDetails>> nestedList, ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		Set<ConceptRefsetInclusionDetails> conceptsInRefset = nestedList.get(refset);
		if (conceptsInRefset==null) {
			conceptsInRefset = new HashSet<ConceptRefsetInclusionDetails>();
			nestedList.put(refset, conceptsInRefset);
		}
		conceptsInRefset.add(conceptDetails);
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

	public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
		I_IntSet currentIntSet = getIntSet(ArchitectonicAuxiliary.Concept.CURRENT);
		I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

		I_GetConceptData memberSetSpecConcept = assertOneOrNone(termFactory.getConcept(refsetId).getSourceRelTargets(
				currentIntSet, 
				generatesRelIntSet, null, false));
		return memberSetSpecConcept;
	}

	int processedConcepts = 0;
	private void processConcept(I_GetConceptData concept, Map<Integer,ConceptRefsetInclusionDetails> currentRefsetInclusionType) throws Exception {
		boolean conflict = false;

		processedConcepts++;
		if (processedConcepts % 10000 ==0) {
			getLog().info("Processed " + processedConcepts + " concepts");
		}
		
		List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());

		Set<I_ThinExtByRefVersioned> directSpecifications = new HashSet<I_ThinExtByRefVersioned>();
		Set<Integer> indirectSpecifications = new HashSet<Integer>();

		for (I_ThinExtByRefVersioned refset : extensions) {
			if (allowedRefsets.contains(refset.getRefsetId())) {
				directSpecifications.add(refset);
			} else {
				indirectSpecifications.add(refset.getComponentId());				
			}
		}
		boolean found = false;
		for (Integer i : allowedRefsets) {
			for (I_ThinExtByRefVersioned refset : directSpecifications) {
				if (refset.getRefsetId()==i) {
					found = true;
				}
			}
			if (!found) {
				indirectSpecifications.add(i);
			}
			found = false;
		}


		for (I_ThinExtByRefVersioned refset : directSpecifications) {
			/*
			 * Direct Specification of refset defined because we have
			 * found a refset specified in the concept which is one of
			 * the allowed refsets					
			 **/
			I_GetConceptData memberSet = getMemberSetConcept(refset.getRefsetId());

			/*
			 * Check inclusion type
			 * and update current refsetInclusionType if this affects the children
			 * */

			Integer inclusionType = getInclusionTypeForRefset(refset);
			if (inclusionType!=null) {
				if (inclusionType == includeLineage) {
					getLog().debug(concept + " has direct inclusion" + " " + termFactory.getConcept(refset.getRefsetId()) + " " + termFactory.getConcept(inclusionType));
					addToRefsetMembers(new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()),memberSet.getConceptId());
					currentRefsetInclusionType.put(refset.getRefsetId(), new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()));

				} else if (inclusionType == excludeLineage) {
					getLog().debug(concept + " has direct exclusion" + " " + termFactory.getConcept(refset.getRefsetId()) + " " + termFactory.getConcept(inclusionType));
					addToRefsetExclusion(new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()),memberSet.getConceptId());
					currentRefsetInclusionType.put(refset.getRefsetId(), new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()));
				} else if (inclusionType ==  includeIndividual) {
					getLog().debug(concept + " has direct inclusion" + " " + termFactory.getConcept(refset.getRefsetId()) + " " + termFactory.getConcept(inclusionType));
					addToRefsetMembers(new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()),memberSet.getConceptId());

				} else if (inclusionType == excludeIndividual) {
					getLog().debug(concept + " has direct exclusion" + " " + termFactory.getConcept(refset.getRefsetId()) + " " + termFactory.getConcept(inclusionType));
					addToRefsetExclusion(new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType,concept.getConceptId()),memberSet.getConceptId());
				}
			}
			checkForExistingMembership(concept,refset.getRefsetId());
		}

		for (Integer refsetId : indirectSpecifications) {

			I_GetConceptData memberSet = getMemberSetConcept(refsetId);
			if (memberSet!=null) {
				/*
				 * No direct Specification of refset defined
				 * */
				ConceptRefsetInclusionDetails inclusionType = currentRefsetInclusionType.get(refsetId);
				if (inclusionType!=null) {


					if (inclusionType.getInclusionTypeId() == includeLineage) {
						getLog().debug("\n\n"+concept + " has indirect inclusion" + " " + termFactory.getConcept(refsetId) + " " + termFactory.getConcept(inclusionType.getInclusionTypeId()));
						getLog().debug("because of: " + termFactory.getConcept(inclusionType.getConceptId()));
						/*
						 * Check for conflicts
						 * */
						Set<ConceptRefsetInclusionDetails> excludedConcepts = newRefsetExclusion.get(memberSet.getConceptId());
						ConceptRefsetInclusionDetails crid = new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType.getInclusionTypeId(),inclusionType.getInclusionReasonId());
						if (excludedConcepts==null || !excludedConcepts.contains(crid)) {
							addToRefsetMembers(crid,memberSet.getConceptId());																							
						} else {
							/*
							 * Conflict detected, do not proceed with children
							 * */

							ConceptRefsetInclusionDetails equals = null;
							for (ConceptRefsetInclusionDetails i : excludedConcepts) {
								if (i.equals(crid)) {
									equals=i;
								}
							}

							conflict = true;
							String conflictMsg = "Conflict in refset: " + termFactory.getConcept(refsetId) + "  with concept: " + concept + " because of " + termFactory.getConcept(inclusionType.getConceptId()) + " and " + termFactory.getConcept(equals.getInclusionReasonId());
							getLog().info(conflictMsg);
							conflictWriter.write(conflictMsg);
							conflictWriter.newLine();
							
							List<ConflictPair<ConceptRefsetInclusionDetails>> pairs = conflicts.get(refsetId);
							if (pairs==null) {
								pairs=new ArrayList<ConflictPair<ConceptRefsetInclusionDetails>>();								
								conflicts.put(memberSet.getConceptId(),pairs);
							}							
							pairs.add(new ConflictPair(equals,crid));							
						}						
					} else if (inclusionType.getInclusionTypeId() == excludeLineage) {
						getLog().debug("\n\n"+concept + " has indirect exclusion" + " " + termFactory.getConcept(refsetId) + " " + termFactory.getConcept(inclusionType.getInclusionTypeId()));
						getLog().debug("because of: " + termFactory.getConcept(inclusionType.getConceptId()));

						Set<ConceptRefsetInclusionDetails> includedConcepts = newRefsetMembers.get(memberSet.getConceptId());
						ConceptRefsetInclusionDetails crid = new ConceptRefsetInclusionDetails(concept.getConceptId(),inclusionType.getInclusionTypeId(),inclusionType.getInclusionReasonId());
						if (includedConcepts==null || !includedConcepts.contains(crid)) {
							addToRefsetExclusion(crid,memberSet.getConceptId());																							
						} else {

							ConceptRefsetInclusionDetails equals = null;
							for (ConceptRefsetInclusionDetails i : includedConcepts) {
								if (i.equals(crid)) {
									equals=i;
								}
							}

							/*
							 * Conflict detected, do not proceed with children
							 * */							
							conflict = true;
							String conflictMsg = "Conflict in refset: " + termFactory.getConcept(refsetId) + "  with concept: " + concept + " because of " + termFactory.getConcept(inclusionType.getConceptId()) + " and " + termFactory.getConcept(equals.getInclusionReasonId());
							getLog().info(conflictMsg);
							conflictWriter.write(conflictMsg);
							conflictWriter.newLine();
							List<ConflictPair<ConceptRefsetInclusionDetails>> pairs = conflicts.get(refsetId);
							if (pairs==null) {
								pairs=new ArrayList<ConflictPair<ConceptRefsetInclusionDetails>>();								
								conflicts.put(memberSet.getConceptId(),pairs);
							}							
							pairs.add(new ConflictPair(equals,crid));							
						}						
					}
				}
			} 
			checkForExistingMembership(concept,refsetId);

		}

		if (!conflict) {
			/*
			 * Find all children
			 **/
			List<I_RelTuple> children = concept.getDestRelTuples(
					getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
					getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

			getLog().debug("processConcept(I_GetConceptData) - processing " + children.size() + " children");
			/*
			 * Iterate over children
			 **/
			for (I_RelTuple child : children) {

				Map<Integer,ConceptRefsetInclusionDetails> localCurrentRefsetInclusionType = new HashMap<Integer,ConceptRefsetInclusionDetails>();
				localCurrentRefsetInclusionType.putAll(currentRefsetInclusionType);

				processConcept(termFactory.getConcept(child.getC1Id()),localCurrentRefsetInclusionType);
			}
		}


	}

	private void checkForExistingMembership(I_GetConceptData concept, int refsetId) throws Exception {
		/*
		 * Check if it is a CURRENT member of the member-refset
		 * And if it is, add it to list of existing members
		 * */

		I_GetConceptData memberSet = getMemberSetConcept(refsetId);
		if (memberSet!=null) {
			List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());
			for (I_ThinExtByRefVersioned ext : extensions) {
				if (ext.getRefsetId()==memberSet.getConceptId()) {
					/*
					 * This is the correct memberSet
					 * -> check the version
					 * */
					List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
					I_ThinExtByRefPart latest = null;
					for (I_ThinExtByRefPart part : versions) {				
						if (latest==null || latest.getVersion()<part.getVersion()) {
							latest = part;
						} 
					}
					getLog().debug("concept " + concept + " has latest version " + latest.getVersion() + " " + latest.getStatus());
					if (latest.getStatus()== this.currentStatusId) {
						addToExistingRefsetMembers(new ConceptRefsetInclusionDetails(concept.getConceptId(),0,concept.getConceptId()), memberSet.getConceptId());
					}
				}
			}
		}
	}

	/*
	 * Utility method
	 * */
	private I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ArchitectonicAuxiliary.Concept concept : concepts) {
			status.add(termFactory.getConcept(concept.getUids()).getConceptId());
		}
		assert status.getSetValues().length > 0: "getIntSet returns an empty set";
		return status;
	}	

	private I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ConceptSpec concept : concepts) {
			status.add(concept.localize().getNid());
		}

		return status;
	}

	private <T> T assertOneOrNone(
			Collection<T> collection) {
		assert collection.size() <= 1 :
			"Exactly one element expected, encountered " + collection;

		if (collection.size()==1) {		
			return collection.iterator().next();
		} else {
			return null;
		}
	}

	private <T> T assertExactlyOne(
			Collection<T> collection) {
		assert collection.size() == 1 :
			"Exactly one element expected, encountered " + collection;

		return collection.iterator().next();
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

		Set<ConceptRefsetInclusionDetails> exclusions = new HashSet<ConceptRefsetInclusionDetails>();

		for (Integer refset : newRefsetMembers.keySet()) {
			if (conflicts.get(refset)==null) { 

				reportWriter.write("\n\nNew included members of refset " + termFactory.getConcept(refset) + " are: ");
				reportWriter.newLine();
				Set<ConceptRefsetInclusionDetails> newMembers = newRefsetMembers.get(refset);
				Set<ConceptRefsetInclusionDetails> oldMembers = existingRefsetMembers.get(refset);
				if (newMembers!=null) {
					for (ConceptRefsetInclusionDetails i: newMembers) {					
						if (oldMembers==null || !oldMembers.contains(i)) {
							reportWriter.write(termFactory.getConcept(i.getConceptId()).toString());
							reportWriter.newLine();
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
					retireLatestExtension(getExtensionForComponent(i.getConceptId(),refset));
				}
			}else {
				getLog().info("\nConflicts exist in refset " + termFactory.getConcept(refset));
				getLog().info("No changes made\n");
			}
		} 
	}

	public I_ThinExtByRefVersioned getExtensionForComponent(int conceptId, int refsetId) throws IOException {
		for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(conceptId)) {
			if (extension.getRefsetId()==refsetId) {
				return extension;
			}
		}
		return null;
	}

	/**
	 * Retires the latest version of a specified extension.
	 * @param extensionPart The extension to check.
	 * @throws Exception
	 */
	private void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {
		getLog().debug("retireLatestExtension(I_ThinExtByRefVersioned=" + extensionPart 
				+ ") - start for concept " + termFactory.getConcept(extensionPart.getComponentId()));

		if (extensionPart != null) {

			List<I_ThinExtByRefTuple> extensionParts = new ArrayList<I_ThinExtByRefTuple>();
			extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, extensionParts, true);

			if (extensionParts.size() > 0) {
				I_ThinExtByRefPart latestVersion = assertExactlyOne(extensionParts);

				I_ThinExtByRefPart clone = latestVersion.duplicatePart();
				clone.setStatus(retiredConceptId);
				clone.setVersion(Integer.MAX_VALUE);
				extensionPart.addVersion(clone);

				getLog().debug("retireLatestExtension(I_ThinExtByRefVersioned) - updated version of extension for " 
						+ termFactory.getConcept(extensionPart.getComponentId()));

				termFactory.addUncommitted(extensionPart);
			}
		}

		getLog().debug("retireLatestExtension(I_ThinExtByRefVersioned) - end"); //$NON-NLS-1$
	}

	/**
	 * Adds a particular concept to the member set.
	 * @param conceptId the concept id of the concept we wish to add to the member set.
	 * @param includeTypeConceptId 
	 * @throws Exception
	 */
	private void addToMemberSet(int conceptId, int includeTypeConceptId, int memberSetId) throws Exception {
		getLog().debug("addToMemberSet(int=" + conceptId + ") - start for " + termFactory.getConcept(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

		int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
				termFactory.getPaths(), Integer.MAX_VALUE);

		I_ThinExtByRefVersioned newExtension =
			termFactory.newExtension(memberSetId, memberId, conceptId,typeId);

		I_ThinExtByRefPartConcept conceptExtension =
			termFactory.newConceptExtensionPart();


		conceptExtension.setPathId(this.memberSetPathDescriptor.getVerifiedConcept().getConceptId());
		conceptExtension.setStatus(currentStatusId);
		conceptExtension.setVersion(Integer.MAX_VALUE);
		conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

		newExtension.addVersion(conceptExtension);
		getLog().debug("addToMemberSet(int=" + conceptId + ") - start added new extension for " + termFactory.getConcept(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

		termFactory.addUncommitted(newExtension);    			

		getLog().debug("addToMemberSet(int) - end");
	}

	private int getMembershipType(int includeTypeConceptId) throws Exception {
		I_GetConceptData includeConcept = termFactory.getConcept(includeTypeConceptId);

		Set<I_GetConceptData> membershipTypes = includeConcept.getSourceRelTargets(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), getIntSet(ConceptConstants.CREATES_MEMBERSHIP_TYPE), null, false);

		return assertExactlyOne(membershipTypes).getConceptId();
	}


	/**
	 * 
	 * @throws TerminologyException
	 * @throws IOException
	 */
	private void findAllowedRefsets() throws TerminologyException, IOException {
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
					allowedRefsets.add(refsetConcept.getConceptId());
				} 
			} 
		}
	}

}
