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
package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public abstract class RefsetSpecStatement extends RefsetSpecComponent {

	protected enum QUERY_TOKENS {
		CONCEPT_IS(RefsetAuxiliary.Concept.CONCEPT_IS), CONCEPT_IS_CHILD_OF(
				RefsetAuxiliary.Concept.CONCEPT_IS_CHILD_OF), CONCEPT_IS_KIND_OF(
				RefsetAuxiliary.Concept.CONCEPT_IS_KIND_OF), CONCEPT_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.CONCEPT_IS_DESCENDENT_OF), CONCEPT_IS_MEMBER_OF(
				RefsetAuxiliary.Concept.CONCEPT_IS_MEMBER_OF), CONCEPT_STATUS_IS(
				RefsetAuxiliary.Concept.CONCEPT_STATUS_IS), CONCEPT_STATUS_IS_CHILD_OF(
				RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_CHILD_OF), CONCEPT_STATUS_IS_KIND_OF(
				RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_KIND_OF), CONCEPT_STATUS_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_DESCENDENT_OF),

		DESC_IS(RefsetAuxiliary.Concept.DESC_IS), DESC_IS_MEMBER_OF(
				RefsetAuxiliary.Concept.DESC_IS_MEMBER_OF), DESC_STATUS_IS(
				RefsetAuxiliary.Concept.DESC_STATUS_IS), DESC_STATUS_IS_CHILD_OF(
				RefsetAuxiliary.Concept.DESC_STATUS_IS_CHILD_OF), DESC_STATUS_IS_KIND_OF(
				RefsetAuxiliary.Concept.DESC_STATUS_IS_KIND_OF), DESC_STATUS_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.DESC_STATUS_IS_DESCENDENT_OF), DESC_TYPE_IS(
				RefsetAuxiliary.Concept.DESC_TYPE_IS), DESC_TYPE_IS_CHILD_OF(
				RefsetAuxiliary.Concept.DESC_TYPE_IS_CHILD_OF), DESC_TYPE_IS_KIND_OF(
				RefsetAuxiliary.Concept.DESC_TYPE_IS_KIND_OF), DESC_TYPE_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.DESC_TYPE_IS_DESCENDENT_OF), DESC_REGEX_MATCH(
				RefsetAuxiliary.Concept.DESC_REGEX_MATCH), DESC_LUCENE_MATCH(
				RefsetAuxiliary.Concept.DESC_LUCENE_MATCH),

		REL_IS(RefsetAuxiliary.Concept.REL_IS), REL_RESTRICTION_IS(
				RefsetAuxiliary.Concept.REL_IS_MEMBER_OF), REL_IS_MEMBER_OF(
				RefsetAuxiliary.Concept.REL_IS_MEMBER_OF), REL_STATUS_IS(
				RefsetAuxiliary.Concept.REL_STATUS_IS), REL_STATUS_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_STATUS_IS_KIND_OF), REL_STATUS_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_STATUS_IS_CHILD_OF), REL_STATUS_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_STATUS_IS_DESCENDENT_OF), REL_TYPE_IS(
				RefsetAuxiliary.Concept.REL_TYPE_IS), REL_TYPE_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_TYPE_IS_KIND_OF), REL_TYPE_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_TYPE_IS_CHILD_OF), REL_TYPE_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_TYPE_IS_DESCENDENT_OF), REL_LOGICAL_QUANTIFIER_IS(
				RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS), REL_LOGICAL_QUANTIFIER_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS_KIND_OF), REL_LOGICAL_QUANTIFIER_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS_CHILD_OF), REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF), REL_CHARACTERISTIC_IS(
				RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS), REL_CHARACTERISTIC_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS_KIND_OF), REL_CHARACTERISTIC_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS_CHILD_OF), REL_CHARACTERISTIC_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_CHARACTERISTIC_IS_DESCENDENT_OF), REL_REFINABILITY_IS(
				RefsetAuxiliary.Concept.REL_REFINABILITY_IS), REL_REFINABILITY_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_REFINABILITY_IS_KIND_OF), REL_REFINABILITY_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_REFINABILITY_IS_CHILD_OF), REL_REFINABILITY_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_REFINABILITY_IS_DESCENDENT_OF), REL_DESTINATION_IS(
				RefsetAuxiliary.Concept.REL_DESTINATION_IS), REL_DESTINATION_IS_KIND_OF(
				RefsetAuxiliary.Concept.REL_DESTINATION_IS_KIND_OF), REL_DESTINATION_IS_CHILD_OF(
				RefsetAuxiliary.Concept.REL_DESTINATION_IS_CHILD_OF), REL_DESTINATION_IS_DESCENDENT_OF(
				RefsetAuxiliary.Concept.REL_DESTINATION_IS_DESCENDENT_OF),

		V1_IS(RefsetAuxiliary.Concept.DIFFERENCE_V1_IS), V2_IS(
				RefsetAuxiliary.Concept.DIFFERENCE_V2_IS), ADDED_CONCEPT(
				RefsetAuxiliary.Concept.ADDED_CONCEPT), ADDED_DESCRIPTION(
				RefsetAuxiliary.Concept.ADDED_DESCRIPTION), ADDED_RELATIONSHIP(
				RefsetAuxiliary.Concept.ADDED_RELATIONSHIP), CHANGED_CONCEPT_STATUS(
				RefsetAuxiliary.Concept.CHANGED_CONCEPT_STATUS), CHANGED_CONCEPT_DEFINED(
				RefsetAuxiliary.Concept.CHANGED_DEFINED), CHANGED_DESCRIPTION_CASE(
				RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_CASE), CHANGED_DESCRIPTION_LANGUAGE(
				RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_LANGUAGE), CHANGED_DESCRIPTION_STATUS(
				RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_STATUS), CHANGED_DESCRIPTION_TERM(
				RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TERM), CHANGED_DESCRIPTION_TYPE(
				RefsetAuxiliary.Concept.CHANGED_DESCRIPTION_TYPE), CHANGED_RELATIONSHIP_CHARACTERISTIC(
				RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC), CHANGED_RELATIONSHIP_GROUP(
				RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_GROUP), CHANGED_RELATIONSHIP_REFINABILITY(
				RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_REFINABILITY), CHANGED_RELATIONSHIP_STATUS(
				RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_STATUS), CHANGED_RELATIONSHIP_TYPE(
				RefsetAuxiliary.Concept.CHANGED_RELATIONSHIP_TYPE);

		protected int nid;

		private QUERY_TOKENS(I_ConceptualizeUniversally concept) {
			try {
				this.nid = concept.localize().getNid();
			} catch (TerminologyException e) {
				throw new RuntimeException(this.toString(), e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	};

	protected QUERY_TOKENS tokenEnum = null;

	/**
	 * Whether to use the NOT qualifier.
	 */
	protected boolean useNotQualifier;

	/**
	 * The type of query - e.g. "Concept is", "Concept is member of" etc.
	 */
	protected I_GetConceptData queryToken;

	/**
	 * The component to which the query type is applied. e.g. if query type is
	 * "concept is" and query destination is "paracetamol", then the statement
	 * would be "concept is":"paracetamol".
	 */
	protected Object queryConstraint;

	protected I_TermFactory termFactory;

	/**
	 * Constructor for refset spec statement.
	 * 
	 * @param useNotQualifier
	 *            Whether to use the NOT qualifier.
	 * @param queryToken
	 *            The query type to use (e.g. "concept is")
	 * @param queryConstraint
	 *            The destination concept (e.g. "paracetamol")
	 */
	public RefsetSpecStatement(boolean useNotQualifier,
			I_GetConceptData groupingToken, I_AmTermComponent constraint,
			int refsetSpecNid) {
		super(refsetSpecNid);

		this.useNotQualifier = useNotQualifier;
		this.queryToken = groupingToken;
		this.queryConstraint = constraint;
		termFactory = Terms.get();
	}

	/**
	 * Constructor for refset spec statement.
	 * 
	 * @param useNotQualifier
	 *            Whether to use the NOT qualifier.
	 * @param queryToken
	 *            The query type to use (e.g. "concept is")
	 * @param queryConstraint
	 *            The string value for regex or lucene search.
	 */
	public RefsetSpecStatement(boolean useNotQualifier,
			I_GetConceptData groupingToken, String constraint, int refsetSpecNid) {
		super(refsetSpecNid);
		this.useNotQualifier = useNotQualifier;
		this.queryToken = groupingToken;
		this.queryConstraint = constraint;
		termFactory = Terms.get();
	}

	/**
	 * Creates an IntSet containing all the is-a relationship IDs present in the
	 * current database. Some databases use the Terminology Auxiliary Is-a only.
	 * Others use the Snomed Is-a as well.
	 */
	public I_IntSet getIsAIds() throws TerminologyException, IOException {
		I_IntSet ids = termFactory.getActiveAceFrameConfig().getDestRelTypes();
		return ids;
	}

	public boolean isNegated() {
		return useNotQualifier;
	}

	public boolean execute(I_AmTermComponent component,
			I_ConfigAceFrame config, GROUPING_TYPE version, I_Position v1_is,
			I_Position v2_is) throws IOException, TerminologyException {

		boolean statementResult = getStatementResult(component, config,
				version, v1_is, v2_is);

		if (useNotQualifier) {
			// if the statement has a negation associated with it then we need
			// to negate the results
			return !statementResult;
		} else {
			return statementResult;
		}
	}

	public abstract boolean getStatementResult(I_AmTermComponent component,
			I_ConfigAceFrame config, GROUPING_TYPE version, I_Position v1Is,
			I_Position v2Is) throws IOException, TerminologyException;

	protected boolean isComponentStatus(I_GetConceptData requiredStatus,
			List<I_AmTuple> tuples) {

		// get latest tuple
		I_AmTuple latestTuple = null;
		int latestTupleVersion = Integer.MIN_VALUE;
		for (I_AmTuple tuple : tuples) {
			if (tuple.getVersion() > latestTupleVersion) {
				latestTupleVersion = tuple.getVersion();
				latestTuple = tuple;
			}
		}

		if (latestTuple != null
				&& latestTuple.getStatusId() == requiredStatus.getConceptId()) {
			return true;
		}

		return false;
	}

	protected boolean componentStatusIs(I_AmTuple tuple) {
		List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
		tuples.add(tuple);

		return isComponentStatus((I_GetConceptData) queryConstraint, tuples);
	}

	protected boolean componentStatusIs(I_GetConceptData requiredStatus,
			I_AmTuple tuple) {
		List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
		tuples.add(tuple);

		return isComponentStatus(requiredStatus, tuples);
	}

	protected boolean componentStatusIsKindOf(I_AmTuple tuple)
			throws TerminologyException, IOException {

		try {
			List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
			tuples.add(tuple);

			if (isComponentStatus((I_GetConceptData) queryConstraint, tuples)) {
				return true;
			}
			// TODO replace with passed in config...
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

			I_IntSet allowedTypes = getIsAIds();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(
					Terms.get().getActiveAceFrameConfig());
			I_IntSet currentStatuses = helper.getCurrentStatusIntSet();

			// get list of all children of input concept
			Set<? extends I_GetConceptData> childStatuses = ((I_GetConceptData) queryConstraint)
					.getDestRelOrigins(currentStatuses, allowedTypes,
							termFactory.getActiveAceFrameConfig()
									.getViewPositionSetReadOnly(), config
									.getPrecedence(), config
									.getConflictResolutionStrategy());

			// call conceptStatusIs on each
			for (I_GetConceptData childStatus : childStatuses) {
				if (isComponentStatus(childStatus, tuples)) {
					return true;
				}
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	protected boolean componentIsMemberOf(int componentId) throws IOException,
			TerminologyException {
		// get all extensions for this concept
		List<? extends I_ExtendByRef> extensions = termFactory
				.getAllExtensionsForComponent(componentId);

		for (I_ExtendByRef ext : extensions) {
			if (ext.getRefsetId() == ((I_GetConceptData) queryConstraint)
					.getConceptId()) { // check

				List<? extends I_ExtendByRefPart> parts = ext.getMutableParts();

				I_ExtendByRefPart latestPart = null;
				int latestPartVersion = Integer.MIN_VALUE;

				// get latest part & check that it is current
				for (I_ExtendByRefPart part : parts) {
					if (part.getVersion() > latestPartVersion) {
						latestPartVersion = part.getVersion();
						latestPart = part;
					}
				}

				for (Integer currentStatusId : getCurrentStatusIds()) {
					if (latestPart.getStatusId() == currentStatusId) {
						return true;
					}
				}
			}
		}

		return false;

	}

	/**
	 * Negates the statement by inverting the current associated negation.
	 */
	public void negateStatement() {
		useNotQualifier = !useNotQualifier;
	}

	public QUERY_TOKENS getTokenEnum() {
		return tokenEnum;
	}

	public void setTokenEnum(QUERY_TOKENS tokenEnum) {
		this.tokenEnum = tokenEnum;
	}

	public String toHtmlFragment() {
		return toString();
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(!useNotQualifier);
		buff.append(" ");
		buff.append(getTokenEnum());
		buff.append(" ");
		buff.append(queryConstraint);
		return buff.toString();
	}

	protected ArrayList<Integer> getChildren(int concept_id, I_Position pos)
			throws TerminologyException, IOException {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		I_TermFactory tf = Terms.get();
		I_GetConceptData c = tf.getConcept(concept_id);
		for (I_RelVersioned d : c.getDestRels()) {
			I_RelPart dm = null;
			for (I_RelPart dd : d.getMutableParts()) {
				if (dd.getPathId() != pos.getPath().getConceptId())
					continue;
				if (!getIsAIds().contains(dd.getTypeId()))
					continue;
				// Find the greatest version <= the one of interest
				if (dd.getVersion() <= pos.getVersion()
						&& (dm == null || dm.getVersion() < dd.getVersion()))
					dm = dd;
			}
			if (dm != null
					&& dm.getStatusId() == tf.getConcept(
							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
							.getConceptId())
				ret.add(d.getC1Id());
		}
		return ret;
	}

	protected boolean conceptIsChildOf(I_GetConceptData c1,
			I_GetConceptData c2, I_Position pos) throws TerminologyException,
			IOException {
		for (Integer child : getChildren(c2.getConceptId(), pos)) {
			if (c1.getConceptId() == child.intValue())
				return true;
		}
		return false;
	}
	
	protected boolean conceptIsDescendantOf(I_GetConceptData c1,
			I_GetConceptData c2, I_Position pos) throws TerminologyException,
			IOException {
		for (Integer child : getChildren(c2.getConceptId(), pos)) {
			if (c1.getConceptId() == child.intValue())
				return true;
			if (conceptIsDescendantOf(c1, Terms.get().getConcept(child), pos))
				return true;
		}
		return false;
	}
	
	protected I_Position getVersion(GROUPING_TYPE version, I_Position v1_is,
			I_Position v2_is) throws TerminologyException {
		if (version == GROUPING_TYPE.V1)
			return v1_is;
		if (version == GROUPING_TYPE.V2)
			return v2_is;
		throw new TerminologyException("Version error:" + version);
	}

}
