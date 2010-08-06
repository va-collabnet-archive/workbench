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
package org.ihtsdo.db.bdb.computer.refset;

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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.refset.ClosestDistanceHashSet;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.db.bdb.computer.kindof.LineageHelper;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public abstract class RefsetUtilities extends LineageHelper implements
		I_HelpRefsets {

	public I_GetConceptData altIsA = null;

	protected I_GetConceptData pathConcept;

	public I_TermFactory termFactory;

	protected int typeId;

	Map<Integer, I_GetConceptData> conceptCache = new HashMap<Integer, I_GetConceptData>();
	
	protected boolean autocommitActive = true;
	
	
	private static int parentMarker = Integer.MIN_VALUE;


	public RefsetUtilities(I_ConfigAceFrame config) {
		super(config);
		setup();
	}

	public RefsetUtilities(I_ConfigAceFrame config, I_IntSet isARelTypes) {
		super(config, isARelTypes);
		setup();
	}
	
	private void setup() {
		if (parentMarker == Integer.MIN_VALUE) {
			parentMarker = ConceptConstants.PARENT_MARKER.localize().getNid();
		}
	}

	public int getInclusionTypeForRefset(I_ExtendByRef part) {
		System.out.println("getInclusionTypeForRefset " + part);
		int typeId = Integer.MIN_VALUE;
		I_ExtendByRefPart latest = null;
		List<? extends I_ExtendByRefPart> versions = part.getMutableParts();
		for (I_ExtendByRefPart version : versions) {

			if (latest == null) {
				if (version.getStatusId() == ReferenceConcepts.CURRENT.getNid()) {
					latest = version;
				}
			} else {
				if (latest.getVersion() < version.getVersion()) {
					if (version.getStatusId() == ReferenceConcepts.RETIRED
							.getNid()) {
						// member has a later retirement so exclude
						latest = null;
					} else {
						latest = version;
					}
				}
			}
		}

		if (latest != null) {
			I_ExtendByRefPartCid temp = (I_ExtendByRefPartCid) latest;
			typeId = temp.getC1id();
		}

		System.out.println("getInclusionTypeForRefset result " + latest);

		return typeId;
	}

	public Set<Integer> getParentsOfConcept(int conceptId) throws IOException,
			Exception {

		Set<Integer> parents = new HashSet<Integer>();

		I_GetConceptData concept = getConcept(conceptId);
		List<? extends I_RelTuple> parenttuples = concept.getSourceRelTuples(
				getStatuses(),
				(this.altIsA == null ? getIntSet(ConceptConstants.SNOMED_IS_A)
						: getIntSet(this.altIsA)), null,
			            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

		/*
		 * Iterate over children
		 */
		for (I_RelTuple parent : parenttuples) {

			List<? extends I_ConceptAttributeTuple> atts = getConcept(
					parent.getC2Id()).getConceptAttributeTuples(null, null);
			I_ConceptAttributeTuple att = getLatestAttribute(atts);
			if (isValidStatus(att)) {
				parents.add(parent.getC2Id());
			}
		}

		return parents;
	}

	public I_IntSet getStatuses() throws Exception {
		return getIntSet(ArchitectonicAuxiliary.Concept.CURRENT,
				ArchitectonicAuxiliary.Concept.PENDING_MOVE,
				ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED,
				ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE);
	}

	public boolean isValidStatus(I_ConceptAttributeTuple att)
			throws TerminologyException, IOException {
		return att.getStatusId() == termFactory.getConcept(
				ArchitectonicAuxiliary.Concept.CURRENT.getUids())
				.getConceptId()
				|| att.getStatusId() == termFactory.getConcept(
						ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids())
						.getConceptId()
				|| att.getStatusId() == termFactory.getConcept(
						ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED
								.getUids()).getConceptId()
				|| att.getStatusId() == termFactory.getConcept(
						ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE
								.getUids()).getConceptId();
	}

	public Set<Integer> getAncestorsOfConcept(int conceptId,
			ClosestDistanceHashSet concepts) throws IOException, Exception {

		Set<Integer> allParents = new HashSet<Integer>();

		Set<Integer> parents = getParentsOfConcept(conceptId);
		for (Integer parent : parents) {
			if (!concepts.keySet().contains(parent)) {
				allParents.add(parent);
				allParents.addAll(getAncestorsOfConcept(parent, concepts));
			}
		}
		return allParents;
	}

	public List<Integer> getChildrenOfConcept(int conceptId)
			throws IOException, Exception {

		if (!termFactory.hasId(SNOMED.Concept.IS_A.getUids())
				&& this.altIsA == null) {
			if (termFactory.hasId(ArchitectonicAuxiliary.Concept.IS_A_REL
					.getUids())) {
				this.altIsA = termFactory
						.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
								.getUids());
			}
		}
		List<Integer> children = new ArrayList<Integer>();

		I_GetConceptData concept = getConcept(conceptId);
		/*
		 * Find all children
		 */
		List<? extends I_RelTuple> childrentuples = concept.getDestRelTuples(
				getStatuses(),
				(this.altIsA == null ? getIntSet(ConceptConstants.SNOMED_IS_A)
						: getIntSet(this.altIsA)), null,
			            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

		/*
		 * Iterate over children
		 */
		for (I_RelTuple child : childrentuples) {

			List<? extends I_ConceptAttributeTuple> atts = getConcept(
					child.getC1Id()).getConceptAttributeTuples(null, null);
			I_ConceptAttributeTuple att = getLatestAttribute(atts);
			if (isValidStatus(att)) {
				children.add(child.getC1Id());
			}
		}
		return children;
	}

	public List<Integer> getSpecificationRefsets() throws Exception {

		List<Integer> allowedRefsets = new ArrayList<Integer>();

		I_IntSet status = termFactory.newIntSet();
		status.add(termFactory.getConcept(
				ArchitectonicAuxiliary.Concept.CURRENT.getUids())
				.getConceptId());

		I_IntSet is_a = termFactory.newIntSet();
		if (this.altIsA == null) {
			is_a.add(termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.IS_A_REL.getUids())
					.getConceptId());
			is_a.add(termFactory.getConcept(
					ConceptConstants.SNOMED_IS_A.localize().getUids())
					.getConceptId());
		} else {
			is_a.add(this.altIsA.getConceptId());
		}

		I_GetConceptData refsetRoot = termFactory
				.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

		Set<? extends I_GetConceptData> refsetChildren = refsetRoot
				.getDestRelOrigins(status, is_a, null,
		            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());
		for (I_GetConceptData refsetConcept : refsetChildren) {
			Set<I_GetConceptData> purposeConcepts = new HashSet<I_GetConceptData>();

			Collection<? extends I_RelVersioned> rels = refsetConcept.getSourceRels();
			for (I_RelVersioned rel : rels) {
				List<? extends I_RelTuple> tuples = rel.getTuples();
				for (I_RelTuple tuple : tuples) {
					if (tuple.getStatusId() == termFactory.getConcept(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
							.getConceptId()
							&& tuple.getTypeId() == termFactory.getConcept(
									RefsetAuxiliary.Concept.REFSET_PURPOSE_REL
											.getUids()).getConceptId()) {

						purposeConcepts.add(getConcept(tuple.getC2Id()));
					}
				}
			}

			if (purposeConcepts.size() == 1) {

				if (purposeConcepts.iterator().next().getConceptId() == termFactory
						.getConcept(
								RefsetAuxiliary.Concept.INCLUSION_SPECIFICATION
										.getUids()).getConceptId()) {
					if (getMemberSetConcept(refsetConcept.getConceptId()) == null) {
						System.out
								.println("ERROR: inclusion specification concept does not have a defined 'generates' relationship. Skipping generation of refset "
										+ refsetConcept);
					} else {
						allowedRefsets.add(refsetConcept.getConceptId());
					}
				}
			}
		}
		return allowedRefsets;
	}

	public I_IntSet getIntSet(ArchitectonicAuxiliary.Concept... concepts)
			throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ArchitectonicAuxiliary.Concept concept : concepts) {
			status
					.add(termFactory.getConcept(concept.getUids())
							.getConceptId());
		}
		assert status.getSetValues().length > 0 : "getIntSet returns an empty set";
		return status;
	}

	public I_IntSet getIntSet(ConceptSpec... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (ConceptSpec concept : concepts) {
			status.add(concept.localize().getNid());
		}

		return status;
	}

	public I_IntSet getIntSet(I_GetConceptData... concepts) throws Exception {
		I_IntSet status = termFactory.newIntSet();

		for (I_GetConceptData concept : concepts) {
			status.add(concept.getConceptId());
		}
		assert status.getSetValues().length > 0 : "getIntSet returns an empty set";
		return status;
	}

	protected <T> T assertExactlyOne(Collection<T> collection) {
		assert collection.size() == 1 : "Exactly one element expected, encountered "
				+ collection;

		return collection.iterator().next();
	}

	public void addToNestedSet(Map<Integer, ClosestDistanceHashSet> nestedList,
			ConceptRefsetInclusionDetails conceptDetails, Integer refset) {
		ClosestDistanceHashSet conceptsInRefset = nestedList.get(refset);
		if (conceptsInRefset == null) {
			conceptsInRefset = new ClosestDistanceHashSet();
			nestedList.put(refset, conceptsInRefset);
		}
		conceptsInRefset.add(conceptDetails);
	}

	public I_GetConceptData getMemberSetConcept(int refsetId) throws Exception {
		I_IntSet currentIntSet = getIntSet(ArchitectonicAuxiliary.Concept.CURRENT);
		I_IntSet generatesRelIntSet = getIntSet(ConceptConstants.GENERATES_REL);

		I_GetConceptData memberSetSpecConcept = assertOneOrNone(getConcept(
				refsetId).getSourceRelTargets(currentIntSet,
				generatesRelIntSet, null,
	            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy()));
		return memberSetSpecConcept;
	}

	public <T> T assertOneOrNone(Collection<T> collection) {
		assert collection.size() <= 1 : "Exactly one element expected, encountered "
				+ collection;

		if (collection.size() == 1) {
			return collection.iterator().next();
		} else {
			return null;
		}
	}

	/**
	 * Retires the latest version of a specified extension.
	 * 
	 * @param extensionPart
	 *            The extension to check.
	 * @throws Exception
	 */
	public void retireLatestExtension(I_ExtendByRef extensionPart)
			throws Exception {

		if (extensionPart != null) {
			I_ExtendByRefPart latestVersion = getLatestVersion(extensionPart);

			I_ExtendByRefPart clone = (I_ExtendByRefPart) latestVersion
					.makeAnalog(ReferenceConcepts.RETIRED.getNid(),
							latestVersion.getPathId(), Long.MAX_VALUE);
			extensionPart.addVersion(clone);

	    	if (isAutocommitActive()) {
	    		termFactory.addUncommitted(extensionPart);
	    	}
		}

	}

	public I_ConceptAttributeTuple getLatestAttribute(
			List<? extends I_ConceptAttributeTuple> atts) {
		I_ConceptAttributeTuple latest = null;
		for (I_ConceptAttributeTuple att : atts) {
			if (latest == null) {
				latest = att;
			} else {
				if (latest.getVersion() < att.getVersion()) {
					latest = att;
				}
			}
		}
		return latest;
	}

	public I_ExtendByRefPart getLatestVersion(I_ExtendByRef ext) {
		I_ExtendByRefPart latest = null;
		List<? extends I_ExtendByRefPart> versions = ext.getMutableParts();
		for (I_ExtendByRefPart version : versions) {

			if (latest == null) {
				latest = version;
			} else {
				if (latest.getVersion() < version.getVersion()) {
					latest = version;
				}
			}
		}
		return latest;
	}

	/**
	 * Adds a particular concept to the member set.
	 * 
	 * @param conceptNid
	 *            the concept id of the concept we wish to add to the member
	 *            set.
	 * @param includeTypeConceptId
	 * @throws Exception
	 */
	public void addToMemberSet(int componentId, int includeTypeConceptId,
			int refsetId) throws Exception {
		I_ExtendByRef ext = getExtensionForComponent(componentId,
				refsetId);
		if (ext != null) {
			I_ExtendByRefPart clone = (I_ExtendByRefPart) getLatestVersion(
					ext).makeAnalog(ReferenceConcepts.CURRENT.getNid(),
					pathConcept.getConceptId(), Long.MAX_VALUE);
			I_ExtendByRefPartCid conceptClone = (I_ExtendByRefPartCid) clone;
			conceptClone.setC1id(getMembershipType(includeTypeConceptId));
			ext.addVersion(conceptClone);
		} else {
			RefsetPropertyMap refsetMap = new RefsetPropertyMap(
					REFSET_TYPES.CID);
			refsetMap.put(REFSET_PROPERTY.CID_ONE,
					getMembershipType(includeTypeConceptId));
			ext = getOrCreateRefsetExtension(refsetId, componentId,
					REFSET_TYPES.CID_CID, refsetMap, UUID.randomUUID());
		}
    	if (isAutocommitActive()) {
    		Terms.get().addUncommittedNoChecks(ext);
    	}
	}

	/**
	 * Adds a particular concept to the member set.
	 * 
	 * @param conceptId
	 *            the concept id of the concept we wish to add to the member
	 *            set.
	 * @param includeTypeConceptId
	 * @throws Exception
	 */
	public void addToMemberSetAsParent(int conceptId, int memberSetId)
			throws Exception {

		I_ExtendByRef ext = getExtensionForComponent(conceptId,
				memberSetId);
		if (ext != null) {
			I_ExtendByRefPart clone = (I_ExtendByRefPart) getLatestVersion(
					ext).makeAnalog(ReferenceConcepts.CURRENT.getNid(),
					pathConcept.getConceptId(), Long.MAX_VALUE);
			I_ExtendByRefPartCid conceptClone = (I_ExtendByRefPartCid) clone;
			conceptClone.setC1id(parentMarker);
			ext.addVersion(conceptClone);
		} else {
			RefsetPropertyMap refsetMap = new RefsetPropertyMap(
					REFSET_TYPES.CID);
			refsetMap.put(REFSET_PROPERTY.CID_ONE,
					getMembershipType(parentMarker));
			ext = getOrCreateRefsetExtension(memberSetId, conceptId,
					REFSET_TYPES.CID_CID, refsetMap, UUID.randomUUID());
		}
    	if (isAutocommitActive()) {
    		termFactory.addUncommitted(ext);
    	}

	}
	
	protected I_ExtendByRef getExtensionForComponent(int conceptId,
			Integer refset) throws IOException {

		List<? extends I_ExtendByRef> exts = termFactory
				.getAllExtensionsForComponent(conceptId);
		for (I_ExtendByRef ext : exts) {
			if (ext.getRefsetId() == refset.intValue()) {
				return ext;
			}
		}
		return null;
	}

	/**
	 * Get the target of a particular type of source relation on a concept. The
	 * source relationship must be current and there must be only one of that
	 * type present.
	 */
	public int getRelTypeTarget(int conceptId, ConceptSpec relType)
			throws Exception {
		I_GetConceptData concept = getConcept(conceptId);

		Set<? extends I_GetConceptData> membershipTypes = concept
				.getSourceRelTargets(
						getIntSet(ArchitectonicAuxiliary.Concept.CURRENT),
						getIntSet(relType), null,
			            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy());

		if (membershipTypes.size() == 0) {
			throw new TerminologyException("A source relationship of type '"
					+ relType.getDescription() + "' was not found for concept "
					+ concept.getIdentifier().getUUIDs().iterator().next());
		}

		if (membershipTypes.size() > 1) {
			throw new TerminologyException(
					"More than one source relationship of type '"
							+ relType.getDescription()
							+ "' was found for concept "
							+ concept.getIdentifier().getUUIDs().iterator()
									.next());
		}

		return membershipTypes.iterator().next().getConceptId();
	}

	public int getMembershipType(int includeTypeConceptId) throws Exception {
		return getRelTypeTarget(includeTypeConceptId,
				ConceptConstants.CREATES_MEMBERSHIP_TYPE);
	}

	public int getExcludeMembersRefset(int specRefsetConceptId) {
		try {
			return getRelTypeTarget(specRefsetConceptId,
					ConceptConstants.EXCLUDE_MEMBERS_REL_TYPE);
		} catch (Exception ex) {
			return Integer.MIN_VALUE;
		}
	}

	public I_GetConceptData getPathConcept() {
		return pathConcept;
	}

	public void setPathConcept(I_GetConceptData pathConcept) {
		this.pathConcept = pathConcept;
	}

	public I_GetConceptData getConcept(int id) throws TerminologyException,
			IOException {
		I_GetConceptData concept = conceptCache.get(id);
		if (concept == null) {
			concept = termFactory.getConcept(id);
			conceptCache.put(id, concept);
		}
		return concept;
	}

	public void setAltIsA(I_GetConceptData altIsA) {
		this.altIsA = altIsA;
	}

	public String getConceptName(int id) throws TerminologyException,
			IOException {
		StringBuffer name = new StringBuffer();
		I_GetConceptData conceptData = getConcept(id);
		name.append("Concept[").append(conceptData.getUids().iterator().next());
		name.append(",\"").append(conceptData).append("\"]");
		return name.toString();
	}

	@Override
	public boolean isAutocommitActive() {
		return autocommitActive;
	}

	@Override
	public void setAutocommitActive(boolean autocommitActive) {
		this.autocommitActive = autocommitActive;
	}
}
