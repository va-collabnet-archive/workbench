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
package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;

public abstract class RefsetUtilities {

	protected I_GetConceptData pathConcept;

	private I_TermFactory termFactory;

	protected int retiredConceptId;
	protected int currentStatusId;
	protected int typeId;

	Map<Integer,I_GetConceptData> conceptCache = new HashMap<Integer,I_GetConceptData>();

	public int getInclusionTypeForRefset(I_ThinExtByRefVersioned part) {
		System.out.println("getInclusionTypeForRefset " + part);
		int typeId = 0;
		I_ThinExtByRefPart latest = null;
		List<? extends I_ThinExtByRefPart> versions = part.getVersions();
		for (I_ThinExtByRefPart version : versions) {

			if (latest == null) {
				latest = version;
			} else {
				if (latest.getVersion()<version.getVersion()) {
					latest = version;
				}				
			}			
		}

		I_ThinExtByRefPartConcept temp = (I_ThinExtByRefPartConcept) latest;
		typeId = temp.getConceptId();
		

		System.out.println("getInclusionTypeForRefset resul " + temp);

		return typeId;
	}

	public Set<Integer> getParentsOfConcept(int conceptId) throws IOException, Exception {

		Set<Integer> parents = new HashSet<Integer>();

		I_GetConceptData concept = getConcept(conceptId);
		List<I_RelTuple> parenttuples = concept.getSourceRelTuples(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
				getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

		/*
		 * Iterate over children
		 **/
		for (I_RelTuple parent : parenttuples) {

			List<I_ConceptAttributeTuple> atts = getConcept(parent.getC2Id()).getConceptAttributeTuples(null, null);
			I_ConceptAttributeTuple att = getLatestAttribute(atts);
			if (att.getConceptStatus()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId() ||
				att.getConceptStatus()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId()) {
				parents.add(parent.getC2Id());				
			}
		}

		return parents;
	}

	public Set<Integer> getAncestorsOfConcept(int conceptId, ClosestDistanceHashSet concepts) throws IOException, Exception {

		Set<Integer> allParents = new HashSet<Integer>();

		Set<Integer> parents = getParentsOfConcept(conceptId);
		for (Integer parent: parents) {
			if (!concepts.keySet().contains(parent)) {
				allParents.add(parent);
				allParents.addAll(getAncestorsOfConcept(parent, concepts));
			}
		}
		return allParents;
	}


	public List<Integer> getChildrenOfConcept(int conceptId) throws IOException, Exception {

		List<Integer> children = new ArrayList<Integer>();

		I_GetConceptData concept = getConcept(conceptId);
		/*
		 * Find all children
		 **/
		List<I_RelTuple> childrentuples = concept.getDestRelTuples(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT, ArchitectonicAuxiliary.Concept.PENDING_MOVE), 
				getIntSet(ConceptConstants.SNOMED_IS_A), null, false);

		/*
		 * Iterate over children
		 **/
		for (I_RelTuple child : childrentuples) {

			List<I_ConceptAttributeTuple> atts = getConcept(child.getC1Id()).getConceptAttributeTuples(null, null);
			I_ConceptAttributeTuple att = getLatestAttribute(atts);
			if (att.getConceptStatus()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId() ||
				att.getConceptStatus()==termFactory.getConcept(ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()).getConceptId()) {
				children.add(child.getC1Id());				
			}
		} 
		return children;
	}

	public List<Integer> getSpecificationRefsets() throws Exception {

		List<Integer> allowedRefsets = new ArrayList<Integer>();

		termFactory = LocalVersionedTerminology.get();

		I_IntSet status = termFactory.newIntSet();
		status.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId());

		I_IntSet is_a = termFactory.newIntSet();
		is_a.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());
		is_a.add(termFactory.getConcept(ConceptConstants.SNOMED_IS_A.localize().getUids()).getConceptId());

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

						purposeConcepts.add(getConcept(tuple.getC2Id()));
					}
				}
			}

			if (purposeConcepts.size()==1) {

				if (purposeConcepts.iterator().next().getConceptId()==termFactory.getConcept(RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION.getUids()).getConceptId()) {
					if (getMemberSetConcept(refsetConcept.getConceptId()) == null) {
						System.out.println("ERROR: inclusion specification concept does not have a defined 'generates' relationship. Skipping generation of refset " + refsetConcept);
					} else {
						allowedRefsets.add(refsetConcept.getConceptId());
					}
				} 
			} 
		}
		return allowedRefsets;
	}
	protected I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ArchitectonicAuxiliary.Concept concept : concepts) {
			status.add(termFactory.getConcept(concept.getUids()).getConceptId());
		}
		assert status.getSetValues().length > 0: "getIntSet returns an empty set";
		return status;
	}

	protected I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ConceptSpec concept : concepts) {
			status.add(concept.localize().getNid());
		}

		return status;
	}

	protected<T> T assertExactlyOne(
			Collection<T> collection) {
		assert collection.size() == 1 :
			"Exactly one element expected, encountered " + collection;

		return collection.iterator().next();
	}

	public void addToNestedSet(Map<Integer,ClosestDistanceHashSet> nestedList, ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		ClosestDistanceHashSet conceptsInRefset = nestedList.get(refset);
		if (conceptsInRefset==null) {
			conceptsInRefset = new ClosestDistanceHashSet();
			nestedList.put(refset, conceptsInRefset);
		}
		conceptsInRefset.add(conceptDetails);
	}

	public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
		I_IntSet currentIntSet = getIntSet(ArchitectonicAuxiliary.Concept.CURRENT);
		I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

		I_GetConceptData memberSetSpecConcept = assertOneOrNone(getConcept(refsetId).getSourceRelTargets(
				currentIntSet, 
				generatesRelIntSet, null, false));
		return memberSetSpecConcept;
	}
	
	protected <T> T assertOneOrNone(
			Collection<T> collection) {
		assert collection.size() <= 1 :
			"Exactly one element expected, encountered " + collection;

		if (collection.size()==1) {		
			return collection.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * Retires the latest version of a specified extension.
	 * @param extensionPart The extension to check.
	 * @throws Exception
	 */
	public void retireLatestExtension(I_ThinExtByRefVersioned extensionPart) throws Exception {

		if (extensionPart != null) {
			I_ThinExtByRefPart latestVersion = getLatestVersion(extensionPart);

			I_ThinExtByRefPart clone = latestVersion.duplicatePart();
			clone.setStatus(retiredConceptId);
			clone.setVersion(Integer.MAX_VALUE);
			extensionPart.addVersion(clone);

			termFactory.addUncommitted(extensionPart);
		}

	}

	public I_ConceptAttributeTuple getLatestAttribute(List<I_ConceptAttributeTuple> atts) {
		I_ConceptAttributeTuple latest = null;
		for (I_ConceptAttributeTuple att: atts) {
			if (latest == null) {
				latest = att;
			} else {
				if (latest.getVersion()<att.getVersion()) {
					latest = att;
				}				
			}		
		}
		return latest;
	}

	public I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned ext) {
		I_ThinExtByRefPart latest = null;
		List<? extends I_ThinExtByRefPart> versions = ext.getVersions();
		for (I_ThinExtByRefPart version : versions) {

			if (latest == null) {
				latest = version;
			} else {
				if (latest.getVersion()<version.getVersion()) {
					latest = version;
				}				
			}			
		}
		return latest;
	}

	/**
	 * Adds a particular concept to the member set.
	 * @param conceptId the concept id of the concept we wish to add to the member set.
	 * @param includeTypeConceptId 
	 * @throws Exception
	 */
	public void addToMemberSet(int conceptId, int includeTypeConceptId, int memberSetId) throws Exception {
		I_ThinExtByRefVersioned ext = getExtensionForComponent(conceptId,memberSetId);
		if (ext != null) {

			I_ThinExtByRefPart clone = getLatestVersion(ext).duplicatePart();
			I_ThinExtByRefPartConcept conceptClone = (I_ThinExtByRefPartConcept) clone;
			conceptClone.setPathId(pathConcept.getConceptId());
			conceptClone.setConceptId(getMembershipType(includeTypeConceptId));
			conceptClone.setStatus(currentStatusId);
			conceptClone.setVersion(Integer.MAX_VALUE);
			ext.addVersion(conceptClone);
			termFactory.addUncommitted(ext);

		} else {
			int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
					termFactory.getPaths(), Integer.MAX_VALUE);

			I_ThinExtByRefVersioned newExtension =
				termFactory.newExtension(memberSetId, memberId, conceptId,typeId);

			I_ThinExtByRefPartConcept conceptExtension =
				termFactory.newConceptExtensionPart();


			conceptExtension.setPathId(pathConcept.getConceptId());
			conceptExtension.setStatus(currentStatusId);
			conceptExtension.setVersion(Integer.MAX_VALUE);
			conceptExtension.setConceptId(getMembershipType(includeTypeConceptId));

			newExtension.addVersion(conceptExtension);

			termFactory.addUncommitted(newExtension);    			
		}
	}

	/**
	 * Adds a particular concept to the member set.
	 * @param conceptId the concept id of the concept we wish to add to the member set.
	 * @param includeTypeConceptId 
	 * @throws Exception
	 */
	public void addToMemberSetAsParent(int conceptId, int memberSetId) throws Exception {

		I_ThinExtByRefVersioned ext = getExtensionForComponent(conceptId,memberSetId);
		if (ext != null) {

			I_ThinExtByRefPart clone = getLatestVersion(ext).duplicatePart();
			I_ThinExtByRefPartConcept conceptClone = (I_ThinExtByRefPartConcept) clone;
			conceptClone.setPathId(pathConcept.getConceptId());
			conceptClone.setConceptId(ConceptConstants.PARENT_MARKER.localize().getNid());
			conceptClone.setStatus(currentStatusId);
			conceptClone.setVersion(Integer.MAX_VALUE);
			ext.addVersion(conceptClone);
			termFactory.addUncommitted(ext);

		} else {

			int memberId = termFactory.uuidToNativeWithGeneration(UUID.randomUUID(),
					ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
					termFactory.getPaths(), Integer.MAX_VALUE);

			I_ThinExtByRefVersioned newExtension =
				termFactory.newExtension(memberSetId, memberId, conceptId,typeId);

			I_ThinExtByRefPartConcept conceptExtension =
				termFactory.newConceptExtensionPart();

			conceptExtension.setPathId(pathConcept.getConceptId());
			conceptExtension.setStatus(currentStatusId);
			conceptExtension.setVersion(Integer.MAX_VALUE);
			conceptExtension.setConceptId(ConceptConstants.PARENT_MARKER.localize().getNid());

			newExtension.addVersion(conceptExtension);
			termFactory.addUncommitted(newExtension);    			

		}

	}

	protected I_ThinExtByRefVersioned getExtensionForComponent(int conceptId,
			Integer refset) throws IOException {

		List<I_ThinExtByRefVersioned> exts = termFactory.getAllExtensionsForComponent(conceptId);
		for (I_ThinExtByRefVersioned ext : exts) {
			if (ext.getRefsetId()==refset.intValue()) {
				return ext;
			}
		}
		return null;
	}

	public int getMembershipType(int includeTypeConceptId) throws Exception {
		I_GetConceptData includeConcept = getConcept(includeTypeConceptId);

		Set<I_GetConceptData> membershipTypes = includeConcept.getSourceRelTargets(
				getIntSet(ArchitectonicAuxiliary.Concept.CURRENT), getIntSet(ConceptConstants.CREATES_MEMBERSHIP_TYPE), null, false);

		return assertExactlyOne(membershipTypes).getConceptId();
	}


	public I_GetConceptData getPathConcept() {
		return pathConcept;
	}


	public void setPathConcept(I_GetConceptData pathConcept) {
		this.pathConcept = pathConcept;
	}

	public I_GetConceptData getConcept(int id) throws TerminologyException, IOException {
		I_GetConceptData concept = conceptCache.get(id);
		if (concept==null) {
			concept = termFactory.getConcept(id);
			conceptCache.put(id, concept);
		}
		return concept;
	}
	
}
