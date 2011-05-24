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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.RefsetAuxiliary.Concept;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PositionSetBI;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public abstract class RefsetSpecStatement extends RefsetSpecComponent {

    protected enum QUERY_TOKENS {

        CONCEPT_IS(Concept.CONCEPT_IS), 
        CONCEPT_IS_CHILD_OF(Concept.CONCEPT_IS_CHILD_OF), 
        CONCEPT_IS_KIND_OF(Concept.CONCEPT_IS_KIND_OF), 
        CONCEPT_IS_DESCENDENT_OF(Concept.CONCEPT_IS_DESCENDENT_OF), 
        CONCEPT_IS_MEMBER_OF(Concept.CONCEPT_IS_MEMBER_OF), 
        CONCEPT_STATUS_IS(Concept.CONCEPT_STATUS_IS), 
        CONCEPT_STATUS_IS_CHILD_OF(Concept.CONCEPT_STATUS_IS_CHILD_OF), 
        CONCEPT_STATUS_IS_KIND_OF(Concept.CONCEPT_STATUS_IS_KIND_OF), 
        CONCEPT_STATUS_IS_DESCENDENT_OF(Concept.CONCEPT_STATUS_IS_DESCENDENT_OF),
        DESC_IS(Concept.DESC_IS), 
        DESC_IS_MEMBER_OF(Concept.DESC_IS_MEMBER_OF), 
        DESC_STATUS_IS(Concept.DESC_STATUS_IS), 
        DESC_STATUS_IS_CHILD_OF(Concept.DESC_STATUS_IS_CHILD_OF), 
        DESC_STATUS_IS_KIND_OF(Concept.DESC_STATUS_IS_KIND_OF), 
        DESC_STATUS_IS_DESCENDENT_OF(Concept.DESC_STATUS_IS_DESCENDENT_OF), 
        DESC_TYPE_IS(Concept.DESC_TYPE_IS), 
        DESC_TYPE_IS_CHILD_OF(Concept.DESC_TYPE_IS_CHILD_OF), 
        DESC_TYPE_IS_KIND_OF(Concept.DESC_TYPE_IS_KIND_OF), 
        DESC_TYPE_IS_DESCENDENT_OF(Concept.DESC_TYPE_IS_DESCENDENT_OF), 
        DESC_REGEX_MATCH(Concept.DESC_REGEX_MATCH), 
        DESC_LUCENE_MATCH(Concept.DESC_LUCENE_MATCH),
        REL_IS(Concept.REL_IS), 
        REL_RESTRICTION_IS(Concept.REL_IS_MEMBER_OF), 
        REL_IS_MEMBER_OF(Concept.REL_IS_MEMBER_OF), 
        REL_STATUS_IS(Concept.REL_STATUS_IS), 
        REL_STATUS_IS_KIND_OF(Concept.REL_STATUS_IS_KIND_OF), 
        REL_STATUS_IS_CHILD_OF(Concept.REL_STATUS_IS_CHILD_OF), 
        REL_STATUS_IS_DESCENDENT_OF(Concept.REL_STATUS_IS_DESCENDENT_OF), 
        REL_TYPE_IS(Concept.REL_TYPE_IS), 
        REL_TYPE_IS_KIND_OF(Concept.REL_TYPE_IS_KIND_OF), 
        REL_TYPE_IS_CHILD_OF(Concept.REL_TYPE_IS_CHILD_OF), 
        REL_TYPE_IS_DESCENDENT_OF(Concept.REL_TYPE_IS_DESCENDENT_OF), 
        REL_LOGICAL_QUANTIFIER_IS(Concept.REL_LOGICAL_QUANTIFIER_IS), 
        REL_LOGICAL_QUANTIFIER_IS_KIND_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_KIND_OF), 
        REL_LOGICAL_QUANTIFIER_IS_CHILD_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_CHILD_OF), 
        REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF(Concept.REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF), 
        REL_CHARACTERISTIC_IS(Concept.REL_CHARACTERISTIC_IS), 
        REL_CHARACTERISTIC_IS_KIND_OF(Concept.REL_CHARACTERISTIC_IS_KIND_OF), 
        REL_CHARACTERISTIC_IS_CHILD_OF(Concept.REL_CHARACTERISTIC_IS_CHILD_OF), 
        REL_CHARACTERISTIC_IS_DESCENDENT_OF(Concept.REL_CHARACTERISTIC_IS_DESCENDENT_OF), 
        REL_REFINABILITY_IS(Concept.REL_REFINABILITY_IS), 
        REL_REFINABILITY_IS_KIND_OF(Concept.REL_REFINABILITY_IS_KIND_OF), 
        REL_REFINABILITY_IS_CHILD_OF(Concept.REL_REFINABILITY_IS_CHILD_OF), 
        REL_REFINABILITY_IS_DESCENDENT_OF(Concept.REL_REFINABILITY_IS_DESCENDENT_OF), 
        REL_DESTINATION_IS(Concept.REL_DESTINATION_IS), 
        REL_DESTINATION_IS_KIND_OF(Concept.REL_DESTINATION_IS_KIND_OF), 
        REL_DESTINATION_IS_CHILD_OF(Concept.REL_DESTINATION_IS_CHILD_OF), 
        REL_DESTINATION_IS_DESCENDENT_OF(Concept.REL_DESTINATION_IS_DESCENDENT_OF),
        V1_IS(Concept.DIFFERENCE_V1_IS), 
        V2_IS(Concept.DIFFERENCE_V2_IS), 
        ADDED_CONCEPT(Concept.ADDED_CONCEPT), 
        ADDED_DESCRIPTION(Concept.ADDED_DESCRIPTION), 
        ADDED_RELATIONSHIP(Concept.ADDED_RELATIONSHIP), 
        CHANGED_CONCEPT_STATUS(Concept.CHANGED_CONCEPT_STATUS), 
        CHANGED_CONCEPT_DEFINED(Concept.CHANGED_DEFINED), 
        CHANGED_DESCRIPTION_CASE(Concept.CHANGED_DESCRIPTION_CASE), 
        CHANGED_DESCRIPTION_LANGUAGE(Concept.CHANGED_DESCRIPTION_LANGUAGE), 
        CHANGED_DESCRIPTION_STATUS(Concept.CHANGED_DESCRIPTION_STATUS), 
        CHANGED_DESCRIPTION_TERM(Concept.CHANGED_DESCRIPTION_TERM), 
        CHANGED_DESCRIPTION_TYPE(Concept.CHANGED_DESCRIPTION_TYPE), 
        CHANGED_RELATIONSHIP_CHARACTERISTIC(Concept.CHANGED_RELATIONSHIP_CHARACTERISTIC), 
        CHANGED_RELATIONSHIP_GROUP(Concept.CHANGED_RELATIONSHIP_GROUP), 
        CHANGED_RELATIONSHIP_REFINABILITY(Concept.CHANGED_RELATIONSHIP_REFINABILITY), 
        CHANGED_RELATIONSHIP_STATUS(Concept.CHANGED_RELATIONSHIP_STATUS), 
        CHANGED_RELATIONSHIP_TYPE(Concept.CHANGED_RELATIONSHIP_TYPE);
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
    protected I_IntSet allowedTypes;
    protected I_IntSet currentStatuses;

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    public RefsetSpecStatement(boolean useNotQualifier, I_GetConceptData groupingToken, I_AmTermComponent constraint,
            int refsetSpecNid, I_ConfigAceFrame config) throws TerminologyException, IOException, Exception {
        super(refsetSpecNid, config);
        this.useNotQualifier = useNotQualifier;
        this.queryToken = groupingToken;
        this.queryConstraint = constraint;
        termFactory = Terms.get();
        this.allowedTypes = getIsAIds();
        this.currentStatuses = helper.getCurrentStatusIntSet();

    }

    /**
     * Constructor for refset spec statement.
     * 
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The string value for regex or lucene search.
     * @throws Exception
     * @throws IOException
     * @throws TerminologyException
     */
    public RefsetSpecStatement(boolean useNotQualifier, I_GetConceptData groupingToken, String constraint,
            int refsetSpecNid, I_ConfigAceFrame config) throws TerminologyException, IOException, Exception {
        super(refsetSpecNid, config);
        this.config = config;
        this.allowedTypes = getIsAIds();
        this.currentStatuses = helper.getCurrentStatusIntSet();
        this.useNotQualifier = useNotQualifier;
        this.queryToken = groupingToken;
        this.queryConstraint = constraint;
        termFactory = Terms.get();
    }

    /**
     * Creates an IntSet containing all the is-a relationship IDs present in the current database. Some databases use
     * the Terminology Auxiliary Is-a only. Others use the Snomed Is-a as well.
     */
    public I_IntSet getIsAIds() throws TerminologyException, IOException {
        I_IntSet ids = config.getDestRelTypes();
        return ids;
    }

    public boolean isNegated() {
        return useNotQualifier;
    }

    @Override
    public boolean execute(I_AmTermComponent component,
            GROUPING_TYPE version, PositionSetBI v1_is,
            PositionSetBI v2_is, Collection<I_ShowActivity> activities) throws IOException, TerminologyException {

        boolean statementResult = getStatementResult(component,
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
            GROUPING_TYPE version, PositionSetBI v1Is,
            PositionSetBI v2Is) throws IOException, TerminologyException;

    protected boolean isComponentStatus(I_GetConceptData requiredStatus, List<I_AmTuple> tuples) {

        // get latest tuple
        I_AmTuple latestTuple = null;
        int latestTupleVersion = Integer.MIN_VALUE;
        for (I_AmTuple tuple : tuples) {
            if (tuple.getVersion() > latestTupleVersion) {
                latestTupleVersion = tuple.getVersion();
                latestTuple = tuple;
            }
        }

        if (latestTuple != null && latestTuple.getStatusId() == requiredStatus.getConceptNid()) {
            return true;
        }

        return false;
    }

    protected boolean componentStatusIs(I_AmTuple tuple) {
        List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
        tuples.add(tuple);

        return isComponentStatus((I_GetConceptData) queryConstraint, tuples);
    }

    protected boolean componentStatusIs(I_GetConceptData requiredStatus, I_AmTuple tuple) {
        List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
        tuples.add(tuple);

        return isComponentStatus(requiredStatus, tuples);
    }

    protected boolean componentStatusIsKindOf(I_AmTuple tuple) throws TerminologyException, IOException {

        try {
            List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
            tuples.add(tuple);

            if (isComponentStatus((I_GetConceptData) queryConstraint, tuples)) {
                return true;
            }

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childStatuses =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            // call conceptStatusIs on each
            for (I_GetConceptData childStatus : childStatuses) {
                if (isComponentStatus(childStatus, tuples)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    protected boolean componentIsMemberOf(int componentId) throws IOException, TerminologyException {
        // get all extensions for this concept
        List<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(componentId);

        for (I_ExtendByRef ext : extensions) {
            if (ext.getRefsetId() == ((I_GetConceptData) queryConstraint).getConceptNid()) { // check

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

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(!useNotQualifier);
        buff.append(" ");
        buff.append(getTokenEnum());
        buff.append(" ");
        buff.append(queryConstraint);
        return buff.toString();
    }

//	private Set<? extends I_GetConceptData> getChildren2(I_GetConceptData c,
//			PositionSetBI pos) throws TerminologyException, IOException {
//		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//		System.out.println("\t" + c.getInitialText());
//		HashSet<I_GetConceptData> ret = new HashSet<I_GetConceptData>();
//		I_TermFactory tf = Terms.get();
//		for (I_RelVersioned<?> d : c.getDestRels()) {
//			I_RelPart dm = null;
//			for (I_RelPart dd : d.getMutableParts()) {
//				System.out.println("\tpos:" + dd.getPathId() + " "
//						+ pos.getPath().getConceptNid());
//				if (dd.getPathId() != pos.getPath().getConceptNid())
//					continue;
//				// if (!getIsAIds().contains(dd.getTypeId()))
//				// continue;
//				System.out.println("\ttyp:"
//						+ dd.getTypeId()
//						+ " "
//						+ tf.getConcept(SNOMED.Concept.IS_A.getUids())
//								.getConceptNid());
//				if (dd.getTypeId() != tf.getConcept(
//						SNOMED.Concept.IS_A.getUids()).getConceptNid())
//					continue;
//				// Find the greatest version <= the one of interest
//				System.out.println("\tver:" + dd.getVersion() + " "
//						+ pos.getVersion());
//				if (dd.getVersion() <= pos.getVersion()
//						&& (dm == null || dm.getVersion() < dd.getVersion()))
//					dm = dd;
//			}
//			if (dm != null)
//				System.out.println("\tsta:"
//						+ dm.getStatusId()
//						+ " "
//						+ tf.getConcept(
//								ArchitectonicAuxiliary.Concept.CURRENT
//										.getUids()).getConceptNid());
//			if (dm != null
//					&& dm.getStatusId() == tf.getConcept(
//							ArchitectonicAuxiliary.Concept.CURRENT.getUids())
//							.getConceptNid())
//				ret.add(tf.getConcept(d.getC1Id()));
//		}
//		return ret;
//	}
    private I_IntSet getCurrentStatuses() throws TerminologyException {
        if (currentStatuses == null) {
            try {
                I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
                I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(
                        config);
                currentStatuses = helper.getCurrentStatusIntSet();
            } catch (Exception e) {
                throw new TerminologyException(e);
            }
        }
        return currentStatuses;
    }

    protected Set<? extends I_GetConceptData> getChildren(
            I_GetConceptData concept, PositionSetBI pos)
            throws TerminologyException, IOException {
        return concept.getDestRelOrigins(getCurrentStatuses(), getIsAIds(),
                new PositionSetReadOnly(pos), config.getPrecedence(),
                config.getConflictResolutionStrategy());
    }

    protected boolean conceptIsChildOf(I_GetConceptData c1,
            I_GetConceptData c2, PositionSetBI pos) throws TerminologyException,
            IOException {
        for (I_GetConceptData child : getChildren(c2, pos)) {
            if (c1.getConceptNid() == child.getConceptNid()) {
                return true;
            }
        }
        return false;
    }
    private HashSet<Integer> descendants = null;

    private void getDescendants(I_GetConceptData c2, PositionSetBI pos)
            throws TerminologyException, IOException {
        if (descendants.contains(new Integer(c2.getConceptNid()))) {
            return;
        }
        for (I_GetConceptData child : getChildren(c2, pos)) {
            getDescendants(child, pos);
            descendants.add(new Integer(child.getConceptNid()));
        }
    }

    protected boolean conceptIsDescendantOf(I_GetConceptData c1,
            I_GetConceptData c2, PositionSetBI pos) throws TerminologyException,
            IOException {
        if (descendants == null) {
            descendants = new HashSet<Integer>();
            getDescendants(c2, pos);
        }
        return descendants.contains(new Integer(c1.getConceptNid()));
    }

    protected PositionSetBI getVersion(GROUPING_TYPE version, PositionSetBI v1_is,
            PositionSetBI v2_is) throws TerminologyException {
        if (version == GROUPING_TYPE.V1) {
            return v1_is;
        }
        if (version == GROUPING_TYPE.V2) {
            return v2_is;
        }
        throw new TerminologyException("Version error:" + version);
    }
//	protected I_AmPart getVersion(List<I_AmPart> parts, PositionSetBI vn_is,
//			boolean trace_p) throws TerminologyException {
//		try {
//			I_AmPart an = null;
//			Set<? extends PositionBI> origins = vn_is.getPath().getInheritedOrigins();
//			for (I_AmPart a : parts) {
//				if (trace_p) {
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//					System.out.println("\t" + a.getPathId() + " "
//							+ a.getVersion() + " "
//							+ vn_is.getPath().getConceptNid() + " "
//							+ vn_is.getVersion());
//				}
//				// Must be on the path
//				// Find the greatest version <= the one of interest
//				for (PositionBI origin : origins) {
//					if (trace_p) {
//						System.out.println("\t" + a.getPathId() + " "
//								+ origin.getPath().getConceptNid());
//						System.out.println("\t" + a.getVersion() + " "
//								+ origin.getVersion());
//					}
//					if ((a.getPathId() == vn_is.getPath().getConceptNid() || (a
//							.getPathId() == origin.getPath().getConceptNid()
//					// &&
//					// a.getVersion()
//					// >=
//					// origin.getVersion()
//					))
//							&& a.getVersion() <= vn_is.getVersion()
//							&& (an == null || an.getVersion() < a.getVersion()))
//						an = a;
//				}
//			}
//			return an;
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new TerminologyException(e.getMessage());
//		}
//	}
}
