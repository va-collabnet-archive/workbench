package org.dwfa.mojo;

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
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;

/**
 * Calculates the member set of a particular reference set.
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
     * @required
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
     * The path that the member set will be written on.
     */
    private I_Path memberSetPath;

    /**
     * The id of the member set.
     */
    private int memberSetId;

    /**
     * The ids of the concepts which may be included in the member set (due to lineage).
     * These may be excluded if they explicitly state a refset exclusion.
     */
    private Set<Integer> includedLineage;

    /**
     * The ids of the concepts which may be excluded from the member set (due to lineage).
     * These may be included if they explicitly state a refset inclusion.
     */
    private Set<Integer> excludedLineage;

    /**
     * The ids of the concepts which have already been added to member set.
     */
    private Set<Integer> includedMemberSet;

    /**
     * The ids of the concepts which have be explicitly exclude from the member set.
     */
    private Set<Integer> excludedMemberSet;

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
    
    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

    	getLog().info("Executing VodbCalculateMemberSet mojo");
    	
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            // execute calculate member set plugin
            MemberSetCalculator calculator = new MemberSetCalculator();

            // iterate over each concept, starting at the root
            calculator.processConcept(calculator.getRoot());

            // write list of uuids for concepts that were included
            // in the member set
            refsetInclusionsOutputFile.getParentFile().mkdirs();
            BufferedWriter uuidWriter = new BufferedWriter(
                    new FileWriter(refsetInclusionsOutputFile));
            for (int i : includedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid: uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }
            uuidWriter.close();

            // write list of uuids for concepts that were excluded
            // from member set
            refsetExclusionsOutputFile.getParentFile().mkdirs();
            uuidWriter = new BufferedWriter(
                    new FileWriter(refsetExclusionsOutputFile));
            for (int i : excludedMemberSet) {
                Collection<UUID> uuids = termFactory.getUids(i);
                for (UUID uuid: uuids) {
                    uuidWriter.write(uuid.toString());
                    uuidWriter.append("\t");
                    uuidWriter.append(getFsnFromConceptId(i));
                    uuidWriter.newLine();
                }
            }
            uuidWriter.close();

            String message = "Number of members found in reference set: "
                            + calculator.getMemberSetCount();
            getLog().info(message);
            termFactory.commit();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
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

    private class MemberSetCalculator implements I_ProcessConcepts {

        private I_TermFactory termFactory;
        private int memberSetCount;
        private int referenceSetId;
        private int includeLineageId;
        private int includeIndividualId;
        private int excludeLineageId;
        private int excludeIndividualId;
        private int conceptTypeId;
        private int retiredConceptId;
        private int typeId;
        private int currentStatusId;
        private I_GetConceptData root;

        /**
         * Calculates a member set given a reference set spec.
         * @param referenceSetId The id of the reference set of which we wish to
         * calculate the member set.
         * @throws Exception
         */
        public MemberSetCalculator() throws Exception {
			getLog().info("MemberSetCalculator() - start");

            termFactory = LocalVersionedTerminology.get();

            // verify concepts
            I_GetConceptData refConcept = refSetSpecDescriptor.getVerifiedConcept();
            referenceSetId = refConcept.getConceptId();

            I_GetConceptData memberSetPathConcept = memberSetPathDescriptor.getVerifiedConcept();
            memberSetPath = termFactory.getPath(memberSetPathConcept.getUids());

            I_GetConceptData memberSetSpecConcept = assertExactlyOne(refConcept.getSourceRelTargets(
            		getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), 
            		getIntSet(ConceptConstants.GENERATES_REL), null, false));
                        
            memberSetId = memberSetSpecConcept.getConceptId();

            root = rootDescriptor.getVerifiedConcept();

            // initialise sets
            includedLineage = new HashSet<Integer>();
            excludedLineage = new HashSet<Integer>();
            includedMemberSet = new HashSet<Integer>();
            excludedMemberSet = new HashSet<Integer>();

            memberSetCount = 0;

            includeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_LINEAGE.getUids().iterator().next());
            includeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            excludeLineageId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_LINEAGE.getUids().iterator().next());
            excludeIndividualId = termFactory.uuidToNative(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids().iterator().next());
            conceptTypeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next());
            retiredConceptId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids().iterator().next());
            typeId = termFactory.uuidToNative(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids().iterator().next());
            currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next());

			getLog().info("MemberSetCalculator() - end");
        }

        private <T> T assertExactlyOne(
				Collection<T> collection) {
        	assert collection.size() == 1 :
        		"Exactly one element expected, encountered " + collection;
        	
			return collection.iterator().next();
		}

		/**
         * Processes each concept in the database. Concepts may be included
         * or excluded from the member set based on the reference set extension
         * type. Lineage (children) of the concept may also be included or excluded
         * (recursively).
         */
        public void processConcept(I_GetConceptData concept) throws Exception {
			getLog().info("processConcept(I_GetConceptData) " 
					+ concept == null ? null : concept.getDescriptions().iterator().next() + " - start");
			
            int conceptId = concept.getConceptId();
            
            List<I_GetExtensionData> extensions =
                termFactory.getExtensionsForComponent(conceptId);

            // process each refset associated with this concept and work out
            // if any of them are the refset we are looking for
            int refsetCount = 0;
            I_ThinExtByRefVersioned memberSet = null;
            for (I_GetExtensionData refSetExtension: extensions) {
                if (refSetExtension.getExtension().getRefsetId() == referenceSetId) {
        			getLog().info("processConcept(I_GetConceptData) - found refset spec " + referenceSetId);
                    refsetCount++;
                }
                if (refSetExtension.getExtension().getRefsetId() == memberSetId) {
        			getLog().info("processConcept(I_GetConceptData) - found refset membership " + referenceSetId);
                    memberSet = refSetExtension.getExtension();
                }
            }
            boolean includedInLatestMemberSet = latestMembersetIncludesConcept(memberSet);
            
            if (refsetCount == 0) {
    			getLog().info("processConcept(I_GetConceptData) - no explicit refset instruction");
    			
    			// no refsets have been found so check if there are any inherited
                // conditions
                if (includedLineage.contains(conceptId)) {
        			getLog().info("processConcept(I_GetConceptData) - inherited include " + getFsnFromConceptId(concept.getConceptId()));

        			// this concept has an inherited condition for inclusion
                    if (!includedInLatestMemberSet) {
                        addToMemberSet(conceptId, includeLineageId);
                    }
                } else if (excludedLineage.contains(conceptId)) {
                	getLog().info("processConcept(I_GetConceptData) - inherited exclude " + getFsnFromConceptId(concept.getConceptId()));
                    excludedMemberSet.add(conceptId);
                    if (memberSet != null) {
                    	retireLatestExtension(memberSet);
                    }
                }
            }

            // process each reference set extension
            for (I_GetExtensionData extensionData: extensions) {

                I_ThinExtByRefVersioned part = extensionData.getExtension();
                int extensionTypeId = part.getTypeId();
            	getLog().info("processConcept(I_GetConceptData) - processing extensionTypeId " + extensionTypeId 
            			+ " referenceSetId " + extensionData.getExtension().getRefsetId());


                if (extensionTypeId == conceptTypeId &&
                        extensionData.getExtension().getRefsetId() == referenceSetId) {
                    // only look at the ref set extensions that correspond to
                    // the reference set as specified in maven plugin config
                    int typeId = 0;
                	getLog().info("processConcept(I_GetConceptData) - valid type/refset, processing");

                    List<? extends I_ThinExtByRefPart> versions = part.getVersions();
                    for (I_ThinExtByRefPart version : versions) {
                        I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) version;
                        typeId = temp.getConceptId();
                    	getLog().info("processConcept(I_GetConceptData) - determining type version " 
                    			+ temp.getVersion() + " type now " + typeId);
                    }

                    boolean include = true;
                    if (typeId == includeIndividualId) {
                        if (!includedInLatestMemberSet) {
                        	getLog().info("processConcept(I_GetConceptData) - including individual " + getFsnFromConceptId(concept.getConceptId()));
                            addToMemberSet(conceptId, typeId);
                        } else {
                        	getLog().info("processConcept(I_GetConceptData) - already included in last generation");
                        }
                    } else if (typeId == includeLineageId) {
                        if (!includedInLatestMemberSet) {
                        	getLog().info("processConcept(I_GetConceptData) - including individual for lineage instruction " + getFsnFromConceptId(concept.getConceptId()));
                            addToMemberSet(conceptId, typeId);
                        } else {
                        	getLog().info("processConcept(I_GetConceptData) - already included in last generation");
                        }
                    	getLog().info("processConcept(I_GetConceptData) - including all children");
                        markAllChildren(concept, include);
                    } else if (typeId == excludeIndividualId) {
                        if (includedInLatestMemberSet) {
                        	getLog().info("processConcept(I_GetConceptData) - excluding individual " + getFsnFromConceptId(concept.getConceptId()));
                            retireLatestExtension(memberSet);
                            excludedMemberSet.add(conceptId);
                        } else {
                        	getLog().info("processConcept(I_GetConceptData) - already excluded in last generation");
                        }
                    } else if (typeId == excludeLineageId) {
                        if (includedInLatestMemberSet) {
                        	getLog().info("processConcept(I_GetConceptData) - excluding individual for lineage instruction " + getFsnFromConceptId(concept.getConceptId()));
                            retireLatestExtension(memberSet);
                            excludedMemberSet.add(conceptId);
                        } else {
                        	getLog().info("processConcept(I_GetConceptData) - already excluded in last generation");
                        }
                        getLog().info("processConcept(I_GetConceptData) - including all children");
                        markAllChildren(concept, !include);
                    } else {
                        System.out.println(termFactory.getConcept(typeId));
                        throw new Exception("Unknown extension type: " + typeId);
                    }
                }
            }

            List<I_RelTuple> children = concept.getDestRelTuples(
            		getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
            		getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

            getLog().info("processConcept(I_GetConceptData) - processing " + children.size() + " children");
            
            for (I_RelTuple child : children) {
                int childId = child.getC1Id();
                processConcept(termFactory.getConcept(childId));
            }

            getLog().info("adding uncommitted change to concept " + concept.getUids() + " " + getFsnFromConceptId(concept.getConceptId()));

            termFactory.addUncommitted(concept);


			getLog().info("processConcept(I_GetConceptData) - end");
        }

        /**
         * Calculates if the latest version of the extension includes the current concept.
         * @param extensionPart The extension to check.
         * @return True if the member set includes the concept, false if it doesn't.
         * @throws Exception
         */
        public boolean latestMembersetIncludesConcept(I_ThinExtByRefVersioned extensionPart) throws Exception {
			getLog().info("latestMembersetIncludesConcept(I_ThinExtByRefVersioned=" + extensionPart + ") - start"); //$NON-NLS-1$ //$NON-NLS-2$

            if (extensionPart == null) {
				getLog().info("latestMembersetIncludesConcept(I_ThinExtByRefVersioned) - end - return value=" + false); //$NON-NLS-1$
                return false;
            }
            
            List<I_ThinExtByRefTuple> exensionParts = new ArrayList<I_ThinExtByRefTuple>();
            extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, exensionParts, true);
            
            boolean result = exensionParts.size() > 0;
			getLog().info("latestMembersetIncludesConcept(I_ThinExtByRefVersioned) - end - return value=" + result);
			
			return result;
        }

        /**
         * Retires the latest version of a specified extension.
         * @param extensionPart The extension to check.
         * @throws Exception
         */
        public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {
			getLog().info("retireLatestExtension(I_ThinExtByRefVersioned=" + extensionPart 
					+ ") - start for concept " + getFsnFromConceptId(extensionPart.getComponentId()));

            if (extensionPart != null) {
                                
                List<I_ThinExtByRefTuple> exensionParts = new ArrayList<I_ThinExtByRefTuple>();
                extensionPart.addTuples(getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), null, exensionParts, true);
                
                I_ThinExtByRefPart latestVersion = assertExactlyOne(exensionParts);

                I_ThinExtByRefPart clone = latestVersion.duplicatePart();
                clone.setStatus(retiredConceptId);
                clone.setVersion(Integer.MAX_VALUE);
                extensionPart.addVersion(clone);
	
    			getLog().info("retireLatestExtension(I_ThinExtByRefVersioned) - updated version of extension for " 
    					+ getFsnFromConceptId(extensionPart.getComponentId()));
	    			
    			termFactory.addUncommitted(extensionPart);
            }

			getLog().info("retireLatestExtension(I_ThinExtByRefVersioned) - end"); //$NON-NLS-1$
        }

        /**
         * Adds a particular concept to the member set.
         * @param conceptId the concept id of the concept we wish to add to the member set.
         * @param includeTypeConceptId 
         * @throws Exception
         */
        public void addToMemberSet(int conceptId, int includeTypeConceptId) throws Exception {
			getLog().info("addToMemberSet(int=" + conceptId + ") - start for " + getFsnFromConceptId(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

            if (!includedMemberSet.contains(conceptId)) {
                memberSetCount++;
                includedMemberSet.add(conceptId);

                int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
                        termFactory.getPaths(), Integer.MAX_VALUE);

                I_ThinExtByRefVersioned newExtension =
                    termFactory.newExtension(memberSetId, memberId, conceptId,
                        typeId);

                I_ThinExtByRefPartConcept conceptExtension =
                    termFactory.newConceptExtensionPart();

                conceptExtension.setPathId(memberSetPath.getConceptId());
                conceptExtension.setStatus(currentStatusId);
                conceptExtension.setVersion(Integer.MAX_VALUE);
                conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

                newExtension.addVersion(conceptExtension);
    			getLog().info("addToMemberSet(int=" + conceptId + ") - start added new extension for " + getFsnFromConceptId(conceptId)); //$NON-NLS-1$ //$NON-NLS-2$

            }

			getLog().info("addToMemberSet(int) - end");
        }

        private int getMembershipType(int includeTypeConceptId) throws Exception {
			I_GetConceptData includeConcept = termFactory.getConcept(includeTypeConceptId);
						
			Set<I_GetConceptData> membershipTypes = includeConcept.getSourceRelTargets(
					getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), getIntSet(ConceptConstants.CREATES_MEMBERSHIP_TYPE), null, false);
			
			return assertExactlyOne(membershipTypes).getConceptId();
		}

		/**
         * Finds the children of a particular concept, and includes/excludes based
         * on provided parameter. Occurs recursively so that the entire lineage is
         * calculated.
         * @param concept The concept who's children we wish to process.
         * @param includeChildren Whether children will be included or excluded when processed.
         * @throws Exception
         */
        public void markAllChildren(I_GetConceptData concept, boolean includeChildren)
            throws Exception {
			getLog().info("markAllChildren(I_GetConceptData=" 
					+ concept.getDescriptions().iterator().next() 
					+ ", boolean=" + includeChildren + ") - start");

            List<I_RelTuple> children = concept.getDestRelTuples(
            		getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
            		getIntSet(ConceptConstants.SNOMED_IS_A), null, false);
            
            getLog().info("markAllChildren(I_GetConceptData, boolean) - concept has " + children.size() + " children");

            for (I_RelTuple child : children) {

                int childId = child.getC1Id();

                if (includeChildren) {
                    if (excludedLineage.contains(Integer.valueOf(childId))) {
                        excludedLineage.remove(Integer.valueOf(childId));
                    }
                    includedLineage.add(Integer.valueOf(childId));
                } else {
                    if (includedLineage.contains(Integer.valueOf(childId))) {
                        includedLineage.remove(Integer.valueOf(childId));
                    }
                    excludedLineage.add(Integer.valueOf(childId));
                }
                
                getLog().info("markAllChildren(I_GetConceptData, boolean) - include children's children");

                markAllChildren(termFactory.getConcept(childId), includeChildren);
            }

			getLog().info("markAllChildren(I_GetConceptData, boolean) - end");
        }
		
		private I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
			I_IntSet status = termFactory.newIntSet();
			
			for (ArchitectonicAuxiliary.Concept concept : concepts) {
				status.add(termFactory.getConcept(concept.getUids()).getConceptId());
			}
			
			return status;
		}

		private I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
			I_IntSet status = termFactory.newIntSet();
			
			for (ConceptSpec concept : concepts) {
				status.add(concept.localize().getNid());
			}
			
			return status;
		}
		
        /**
         * Gets the number of members in the specified reference set.
         * @return
         */
        public int getMemberSetCount() {

            return memberSetCount;
        }

        /**
         * Sets the number of members in the specified reference set.
         * @param memberSetCount
         */
        public void setMemberSetCount(int memberSetCount) {

            this.memberSetCount = memberSetCount;
        }

        public I_GetConceptData getRoot() {
            return root;
        }

        public void setRoot(I_GetConceptData root) {
            this.root = root;
        }
    }

    public ConceptDescriptor getMemberSetPathDescriptor() {
        return memberSetPathDescriptor;
    }

    public void setMemberSetPathDescriptor(ConceptDescriptor memberSetPathDescriptor) {
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
}
