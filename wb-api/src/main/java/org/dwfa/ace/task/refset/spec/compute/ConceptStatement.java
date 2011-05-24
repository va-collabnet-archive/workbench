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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill, Keith Campbell
 * 
 */
public class ConceptStatement extends RefsetSpecStatement {

	I_GetConceptData queryConstraintConcept;
	private Collection<I_ShowActivity> activities;
	private StopActionListener stopListener = new StopActionListener();

	private class StopActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			for (I_ShowActivity a : activities) {
				a.cancel();
			}
		}
	}

	/**
	 * Constructor for refset spec statement.
	 * 
	 * @param useNotQualifier Whether to use the NOT qualifier.
	 * @param queryToken The query type to use (e.g. "concept is")
	 * @param queryConstraint The destination concept (e.g. "paracetamol")
	 * @throws Exception
	 */
	public ConceptStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint,
			int refsetSpecNid, I_ConfigAceFrame config) throws Exception {
		super(useNotQualifier, queryToken, queryConstraint, refsetSpecNid, config);

		for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
			if (queryToken.getConceptNid() == token.nid) {
				tokenEnum = token;
				break;
			}
		}
		if (tokenEnum == null) {
			throw new RuntimeException("Unknown query type : " + queryToken);
		}
		queryConstraintConcept = (I_GetConceptData) queryConstraint;
	}

	@Override
	public I_RepresentIdSet getPossibleDescriptions(I_RepresentIdSet parentPossibleConcepts,
			Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
		throw new TerminologyException("Get possible descriptions in concept statement unsupported operation.");
	}

	@Override
	public I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
			Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
		throw new TerminologyException("Get possible relationships in concept statement unsupported operation.");
	}

	@Override
	public I_RepresentIdSet getPossibleConcepts(I_RepresentIdSet parentPossibleConcepts,
			Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
		I_ShowActivity activity = null;
		long startTime = System.currentTimeMillis();
		this.activities = activities;

		queryConstraint = (I_GetConceptData) queryConstraint;
		I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
		if (parentPossibleConcepts == null) {
			parentPossibleConcepts = termFactory.getConceptNidSet();
		}

		switch (tokenEnum) {
		case CONCEPT_IS:
			if (isNegated()) {
				possibleConcepts.or(parentPossibleConcepts);
				possibleConcepts.setNotMember(queryConstraintConcept.getConceptNid());
			} else {
				possibleConcepts.setMember(queryConstraintConcept.getConceptNid());
			}
			break;
		case CONCEPT_IS_CHILD_OF:
			activity = setupActivityPanel(parentPossibleConcepts);
			activities.add(activity);

			if (isNegated()) {
				possibleConcepts.or(parentPossibleConcepts);
			} else {
				I_RepresentIdSet results = queryConstraintConcept.getPossibleChildOfConcepts(config);
				possibleConcepts.or(results);
			}
			break;
		case CONCEPT_IS_DESCENDENT_OF:
		case CONCEPT_IS_KIND_OF:
			activity = setupActivityPanel(parentPossibleConcepts);
			activities.add(activity);
			if (isNegated()) {
				possibleConcepts.or(parentPossibleConcepts);
			} else {
				I_RepresentIdSet results = queryConstraintConcept.getPossibleKindOfConcepts(config, activity);
				possibleConcepts.or(results);
			}
			break;
		case CONCEPT_IS_MEMBER_OF:
			activity = setupActivityPanel(parentPossibleConcepts);
			activities.add(activity);
			Collection<? extends I_ExtendByRef> refsetExtensions =
				termFactory.getRefsetExtensionMembers(queryConstraintConcept.getConceptNid());
			Set<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
			for (I_ExtendByRef ext : refsetExtensions) {
				refsetMembers.add(termFactory.getConcept(ext.getComponentNid()));
			}
			I_RepresentIdSet refsetMemberSet = termFactory.getIdSetfromTermCollection(refsetMembers);
			if (isNegated()) {
				possibleConcepts.or(parentPossibleConcepts);
			} else {
				possibleConcepts.or(refsetMemberSet);
			}
			break;
		case CONCEPT_STATUS_IS:
		case CONCEPT_STATUS_IS_CHILD_OF:
		case CONCEPT_STATUS_IS_DESCENDENT_OF:
		case CONCEPT_STATUS_IS_KIND_OF:
			possibleConcepts.or(parentPossibleConcepts);
			break;
		case V1_IS:
		case V2_IS:
		case ADDED_CONCEPT:
		case CHANGED_CONCEPT_STATUS:
		case CHANGED_CONCEPT_DEFINED:
			// TODO - EKM
			possibleConcepts.or(parentPossibleConcepts);
			break;
		default:
			throw new RuntimeException("Can't handle queryToken: " + queryToken);
		}
		setPossibleConceptsCount(possibleConcepts.cardinality());

		if (activity != null) {
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Incoming count: "
					+ parentPossibleConcepts.cardinality() + "; Outgoing count: " + possibleConcepts.cardinality());
			activity.complete();
		}
		return possibleConcepts;
	}

	private I_ShowActivity setupActivityPanel(I_RepresentIdSet parentPossibleConcepts) {
		I_ShowActivity activity;
		activity = Terms.get().newActivityPanel(true, config, "<html>Possible: <br>" + this.toHtmlFragment(), true);
		activity.setIndeterminate(true);
		activity.setProgressInfoLower("Incoming count: " + parentPossibleConcepts.cardinality());
		activity.addStopActionListener(stopListener);
		return activity;
	}

	@Override
	public boolean getStatementResult(I_AmTermComponent component, GROUPING_TYPE version, PositionSetBI v1_is,
			PositionSetBI v2_is) throws TerminologyException, IOException {
		I_GetConceptData concept = (I_GetConceptData) component;

		if (version != null || v1_is != null || v2_is != null) {
			if (version == null)
				throw new TerminologyException("Not in scope of V1 or V2: "
						+ tokenEnum + " " + concept.getInitialText());
			if (v1_is == null)
				throw new TerminologyException("Need to set V1 IS: "
						+ tokenEnum + " " + concept.getInitialText());
			if (v2_is == null)
				throw new TerminologyException("Need to set V2 IS: "
						+ tokenEnum + " " + concept.getInitialText());
		}

		switch (tokenEnum) {
		case CONCEPT_IS:
			if (version == null) {
				return conceptIs(concept);
			} else {
				return conceptIs(concept, getVersion(version, v1_is, v2_is));
			}
		case CONCEPT_IS_CHILD_OF:
			if (version == null) {
				return conceptIsChildOf(concept);
			} else {
				return conceptIsChildOf(concept, getVersion(version, v1_is,
						v2_is));
			}
		case CONCEPT_IS_DESCENDENT_OF:
			if (version == null) {
				return conceptIsDescendantOf(concept);
			} else {
				return conceptIsDescendantOf(concept, getVersion(version,
						v1_is, v2_is));
			}
		case CONCEPT_IS_KIND_OF:
			if (version == null) {
				return conceptIsKindOf(concept);
			} else {
				return conceptIsKindOf(concept, getVersion(version, v1_is,
						v2_is));
			}
		case CONCEPT_IS_MEMBER_OF:
			if (version == null) {
				return conceptIsMemberOf(concept);
			} else {
				throw new TerminologyException(tokenEnum
						+ ": Unsupported operation for version scope.");
			}
		case CONCEPT_STATUS_IS:
			if (version == null) {
				return conceptStatusIs(concept);
			} else {
				return conceptStatusIs(concept, getVersion(version, v1_is,
						v2_is));
			}
		case CONCEPT_STATUS_IS_CHILD_OF:
			if (version == null) {
				return conceptStatusIsChildOf(concept);
			} else {
				return conceptStatusIsChildOf(concept, getVersion(version,
						v1_is, v2_is));
			}
		case CONCEPT_STATUS_IS_DESCENDENT_OF:
			if (version == null) {
				return conceptStatusIsDescendantOf(concept);
			} else {
				return conceptStatusIsDescendantOf(concept, getVersion(version,
						v1_is, v2_is));
			}
		case CONCEPT_STATUS_IS_KIND_OF:
			if (version == null) {
				return conceptStatusIsKindOf(concept);
			} else {
				return conceptStatusIsKindOf(concept, getVersion(version,
						v1_is, v2_is));
			}
		case ADDED_CONCEPT:
			return addedConcept(concept, version, v1_is, v2_is);
		case CHANGED_CONCEPT_STATUS:
			return changedConceptStatus(concept, version, v1_is, v2_is);
		case CHANGED_CONCEPT_DEFINED:
			return changedConceptDefined(concept, version, v1_is, v2_is);
		default:
			throw new RuntimeException("Can't handle queryToken: " + queryToken);
		}
	}

	/**
	 * Tests if the concept being tested is an immediate child of the query
	 * constraint.
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	private boolean conceptIsChildOf(I_GetConceptData conceptBeingTested) throws TerminologyException, IOException {
		try {

			Set<? extends I_GetConceptData> children =
				queryConstraintConcept.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
						.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
						.getConflictResolutionStrategy());

			for (I_GetConceptData child : children) {
				if (conceptBeingTested.equals(child)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	/**
	 * Tests of the concept being tested is a member of the specified refset.
	 * 
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptIsMemberOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
		return componentIsMemberOf(conceptBeingTested.getConceptNid());
	}

	/**
	 * Tests of the current concept is the same as the query constraint.
	 * 
	 * @param concept
	 * @return
	 */
	private boolean conceptIs(I_GetConceptData conceptBeingTested) {
		return conceptBeingTested.equals(queryConstraint);
	}

	/**
	 * Tests if the current concept is a child of the query constraint. This
	 * does not return true if they are the same concept. This will check depth
	 * >= 1 to find children.
	 * 
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptIsDescendantOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
		if (conceptBeingTested.getNid() == queryConstraintConcept.getNid()) {
			return false;
		}
		
		if (RefsetSpecQuery.myStaticIsACache == null) {
			//System.out.print("n");
			return queryConstraintConcept.isParentOf(conceptBeingTested, currentStatuses, allowedTypes, termFactory
					.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
					.getConflictResolutionStrategy());
		} else {
			try {
				//System.out.print("c");
				return RefsetSpecQuery.myStaticIsACache.isKindOf(conceptBeingTested.getConceptNid(), 
						queryConstraintConcept.getConceptNid());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
		
	}

	/**
	 * Tests if the current concept is a child of the query constraint. This
	 * will return true if they are the same concept. This will check depth
	 * >= 1 to find children.
	 * 
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptIsKindOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
		return queryConstraintConcept.isParentOfOrEqualTo(conceptBeingTested, currentStatuses, allowedTypes,
				termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
				.getConflictResolutionStrategy());
	}

	private boolean conceptIs(I_GetConceptData concept, PositionSetBI pos)
	throws TerminologyException {
		I_ConceptAttributeTuple<?> a = getVersion(concept, pos);
		return (a != null && concept.getConceptNid() == queryConstraintConcept
				.getConceptNid());
	}

	/**
	 * Tests if the current concept has a status the same as the query
	 * constraint.
	 * 
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIs(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {
		return conceptStatusIs(conceptBeingTested, queryConstraintConcept);
	}

	/**
	 * Tests if the current concept has a status matching the inputted status.
	 * 
	 * @param requiredStatusConcept
	 * @param conceptBeingTested
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIs(I_GetConceptData conceptBeingTested, I_GetConceptData requiredStatusConcept)
	throws IOException, TerminologyException {

		List<? extends I_ConceptAttributeTuple> tuples =
			conceptBeingTested.getConceptAttributeTuples(null, termFactory.getActiveAceFrameConfig()
					.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

		// get latest tuple
		I_ConceptAttributeTuple latestTuple = null;
		int latestTupleVersion = Integer.MIN_VALUE;
		for (I_ConceptAttributeTuple tuple : tuples) {
			if (tuple.getVersion() > latestTupleVersion) {
				latestTupleVersion = tuple.getVersion();
				latestTuple = tuple;
			}
		}

		if (latestTuple != null && latestTuple.getStatusNid() == requiredStatusConcept.getConceptNid()) {
			return true;
		}

		return false;
	}

	private boolean conceptStatusIs(I_GetConceptData concept, PositionSetBI pos)
	throws TerminologyException {
		I_ConceptAttributeTuple<?> a = getVersion(concept, pos);
		return (a != null && a.getStatusNid() == queryConstraintConcept
				.getConceptNid());
	}

	private boolean conceptStatusIsChildOf(I_GetConceptData concept,
			PositionSetBI pos) throws TerminologyException, IOException {
		I_ConceptAttributeTuple<?> a = getVersion(concept, pos);
		if (a == null)
			return false;
		return conceptIsChildOf(Terms.get().getConcept(a.getStatusNid()),
				this.queryConstraintConcept, pos);
	}

	private boolean conceptStatusIsDescendantOf(I_GetConceptData concept,
			PositionSetBI pos) throws TerminologyException, IOException {
		I_ConceptAttributeTuple<?> a = getVersion(concept, pos);
		if (a == null)
			return false;
		return conceptIsDescendantOf(Terms.get().getConcept(a.getStatusNid()),
				this.queryConstraintConcept, pos);
	}

	private boolean conceptStatusIsKindOf(I_GetConceptData concept,
			PositionSetBI pos) throws TerminologyException, IOException {
		return conceptStatusIs(concept, pos)
		|| conceptStatusIsDescendantOf(concept, pos);
	}

	private boolean conceptIsChildOf(I_GetConceptData c1, PositionSetBI pos)
	throws TerminologyException, IOException {
		return conceptIsChildOf(c1, queryConstraintConcept, pos);
	}

	private boolean conceptIsDescendantOf(I_GetConceptData c1, PositionSetBI pos)
	throws TerminologyException, IOException {
		return conceptIsDescendantOf(c1, queryConstraintConcept, pos);
	}

	private boolean conceptIsKindOf(I_GetConceptData concept, PositionSetBI pos)
	throws TerminologyException, IOException {
		return conceptIs(concept, pos) || conceptIsDescendantOf(concept, pos);
	}

	/**
	 * Tests if the current concept has a status matching the query constraint,
	 * or any of its children (depth >=1).
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIsKindOf(I_GetConceptData conceptBeingTested) throws IOException, TerminologyException {

		// check if the concept's status matches the specified status
		if (conceptStatusIs(conceptBeingTested)) {
			return true;
		}

		return conceptStatusIsDescendantOf(conceptBeingTested);
	}

	/**
	 * Tests if the current concept has a status matching the query constraint's
	 * immediate children.
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIsChildOf(I_GetConceptData conceptBeingTested) throws TerminologyException,
	IOException {

		try {

			// get list of all children of input concept
			Set<? extends I_GetConceptData> childStatuses =
				queryConstraintConcept.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
						.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config
						.getConflictResolutionStrategy());

			// call conceptStatusIs on each
			for (I_GetConceptData childStatus : childStatuses) {
				if (conceptStatusIs(conceptBeingTested, childStatus)) {
					return true;
				}

			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	/**
	 * Tests if the current concept has a status matching the query constraint's
	 * children to depth >= 1.
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIsDescendantOf(I_GetConceptData conceptBeingTested) throws IOException,
	TerminologyException {

		return conceptStatusIsDescendantOf(conceptBeingTested, queryConstraintConcept);
	}

	/**
	 * Tests if the current concept has a status matching the specified status'
	 * children to depth >= 1.
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIsDescendantOf(I_GetConceptData conceptBeingTested, I_GetConceptData status)
	throws TerminologyException, IOException {

		try {

			Set<? extends I_GetConceptData> childStatuses =
				status.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
						.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

			for (I_GetConceptData childStatus : childStatuses) {
				if (conceptStatusIs(conceptBeingTested, childStatus)) {
					return true;
				} else if (conceptStatusIsDescendantOf(conceptBeingTested, childStatus)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private I_ConceptAttributeTuple<?> getVersion(
			I_GetConceptData conceptBeingTested, PositionSetBI vn_is)
			throws TerminologyException {
		try {
			// ArrayList<I_AmPart> parts = new ArrayList<I_AmPart>(
			// conceptBeingTested.getConceptAttributes().getMutableParts());
			// I_AmPart part = getVersion(parts, vn_is, false);
			// return (I_ConceptAttributePart) part;
			List<? extends I_ConceptAttributeTuple> a1s = conceptBeingTested
					.getConceptAttributeTuples(null, vn_is, Precedence.PATH,
							config.getConflictResolutionStrategy());
			I_ConceptAttributeTuple<?> a1 = (a1s != null && a1s.size() > 0 ? a1s
					.get(0) : null);
			return a1;
		} catch (Exception e) {
			throw new TerminologyException(e.getMessage());
		}
	}

	private I_ConceptAttributeTuple<?> getVersion(
			I_GetConceptData conceptBeingTested, GROUPING_TYPE version,
			PositionSetBI v1_is, PositionSetBI v2_is) throws TerminologyException {
		return getVersion(conceptBeingTested, getVersion(version, v1_is, v2_is));
	}

	/**
	 * Tests if the concept being tested has been added from v1 to v2
	 * 
	 * @param conceptBeingTested
	 * @return
	 * @throws TerminologyException
	 * @throws IOException
	 */
	private boolean addedConcept(I_GetConceptData conceptBeingTested,
			GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
	throws TerminologyException, IOException {
		try {
			// TODO version must be v2
			I_ConceptAttributeTuple<?> a1 = getVersion(conceptBeingTested, v1_is);
			I_ConceptAttributeTuple<?> a2 = getVersion(conceptBeingTested, v2_is);
			return (a1 == null && a2 != null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedConceptStatus(I_GetConceptData conceptBeingTested,
			GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
			throws TerminologyException, IOException {
		try {
			I_ConceptAttributeTuple<?> a1 = getVersion(conceptBeingTested,
					v1_is);
			I_ConceptAttributeTuple<?> a2 = getVersion(conceptBeingTested,
					v2_is);
			return (a1 != null
					&& a2 != null
					&& !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2
							.getTime()) && a1.getStatusNid() != a2
					.getStatusNid());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

	private boolean changedConceptDefined(I_GetConceptData conceptBeingTested,
			GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
			throws TerminologyException, IOException {
		try {
			I_ConceptAttributeTuple<?> a1 = getVersion(conceptBeingTested,
					v1_is);
			I_ConceptAttributeTuple<?> a2 = getVersion(conceptBeingTested,
					v2_is);
			return (a1 != null
					&& a2 != null
					&& !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2
							.getTime()) && a1.isDefined() != a2.isDefined());
		} catch (Exception e) {
			e.printStackTrace();
			throw new TerminologyException(e.getMessage());
		}
	}

}
