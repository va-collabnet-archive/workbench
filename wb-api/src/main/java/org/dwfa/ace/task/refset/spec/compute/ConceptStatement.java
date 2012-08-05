/**
 * Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.dwfa.ace.task.refset.spec.compute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery.GROUPING_TYPE;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ConceptFetcherSimple;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 * Represents partial information contained in a refset spec. An example of a statement is : "NOT: Concept is
 * : Paracetamol"
 *
 * @author Chrissy Hill, Keith Campbell
 *
 */
public class ConceptStatement extends RefsetSpecStatement {

    I_GetConceptData queryConstraintConcept;
    private Collection<I_ShowActivity> activities;
    private StopActionListener stopListener = new StopActionListener();
    private TerminologyStoreDI ts;

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
        ts = Ts.get();
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
            Collection<I_ShowActivity> activities) throws IOException {
        throw new IOException("Get possible descriptions in concept statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws IOException {
        throw new IOException("Get possible relationships in concept statement unsupported operation.");
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws IOException, ComputationCanceled, ContradictionException {
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
                    I_RepresentIdSet results = (I_RepresentIdSet) queryConstraintConcept.getPossibleKindOfConcepts(config);
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
                    try {
                        refsetMembers.add(termFactory.getConcept(ext.getComponentNid()));
                    } catch (TerminologyException ex) {
                        throw new IOException(ex);
                    }
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
    public boolean getStatementResult(int componentNid, Object component, GROUPING_TYPE version, PositionSetBI v1_is,
            PositionSetBI v2_is) throws IOException, ContradictionException {
        ConceptFetcherBI fetcher;
        if (component instanceof ConceptFetcherBI) {
            fetcher = (ConceptFetcherBI) component;
        } else {
            fetcher = new ConceptFetcherSimple((ConceptChronicleBI) component);
        }

        if (version != null || v1_is != null || v2_is != null) {
            if (version == null) {
                try {
                    throw new IOException("Not in scope of V1 or V2: "
                            + tokenEnum + " " + fetcher.fetch().toString());
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            if (v1_is == null) {
                try {
                    throw new IOException("Need to set V1 IS: "
                            + tokenEnum + " " + fetcher.fetch().toString());
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            if (v2_is == null) {
                try {
                    throw new IOException("Need to set V2 IS: "
                            + tokenEnum + " " + fetcher.fetch().toString());
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
        }

        switch (tokenEnum) {
            case CONCEPT_IS:
                if (version == null) {
                    return conceptIs(componentNid, fetcher);
                } else {
                    return conceptIs(componentNid, fetcher, getVersion(version, v1_is, v2_is));
                }
            case CONCEPT_IS_CHILD_OF:
                if (version == null) {
                    return conceptIsChildOf(componentNid, fetcher);
                } else {
                    return conceptIsChildOf(componentNid, fetcher, getVersion(version, v1_is,
                            v2_is));
                }
            case CONCEPT_IS_DESCENDENT_OF:
                if (version == null) {
                    return conceptIsDescendantOf(componentNid, fetcher);
                } else {
                    return conceptIsDescendantOf(componentNid, fetcher, getVersion(version,
                            v1_is, v2_is));
                }
            case CONCEPT_IS_KIND_OF:
                if (version == null) {
                    return conceptIsKindOf(componentNid, fetcher);
                } else {
                    return conceptIsKindOf(componentNid, fetcher, getVersion(version, v1_is,
                            v2_is));
                }
            case CONCEPT_IS_MEMBER_OF:
                if (version == null) {
                    return conceptIsMemberOf(componentNid, fetcher);
                } else {
                    throw new IOException(tokenEnum
                            + ": Unsupported operation for version scope.");
                }
            case CONCEPT_STATUS_IS:
                if (version == null) {
                    return conceptStatusIs(componentNid, fetcher);
                } else {
                    return conceptStatusIs(componentNid, fetcher, getVersion(version, v1_is,
                            v2_is));
                }
            case CONCEPT_STATUS_IS_CHILD_OF:
                if (version == null) {
                    return conceptStatusIsChildOf(componentNid, fetcher);
                } else {
                    return conceptStatusIsChildOf(componentNid, fetcher, getVersion(version,
                            v1_is, v2_is));
                }
            case CONCEPT_STATUS_IS_DESCENDENT_OF:
                if (version == null) {
                    return conceptStatusIsDescendantOf(componentNid, fetcher);
                } else {
                    return conceptStatusIsDescendantOf(componentNid, fetcher, getVersion(version,
                            v1_is, v2_is));
                }
            case CONCEPT_STATUS_IS_KIND_OF:
                if (version == null) {
                    return conceptStatusIsKindOf(componentNid, fetcher);
                } else {
                    return conceptStatusIsKindOf(componentNid, fetcher, getVersion(version,
                            v1_is, v2_is));
                }
            case ADDED_CONCEPT:
                return addedConcept(componentNid, fetcher, version, v1_is, v2_is);
            case CHANGED_CONCEPT_STATUS:
                return changedConceptStatus(componentNid, fetcher, version, v1_is, v2_is);
            case CHANGED_CONCEPT_DEFINED:
                return changedConceptDefined(componentNid, fetcher, version, v1_is, v2_is);
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    /**
     * Tests if the concept being tested is an immediate child of the query constraint.
     *
     * @param conceptBeingTested
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    private boolean conceptIsChildOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException {
        try {

            return Ts.get().isChildOf(conceptNid, queryConstraintConcept.getConceptNid(), config.getViewCoordinate());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
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
    private boolean conceptIsMemberOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException {
        return componentIsMemberOf(conceptNid);
    }

    /**
     * Tests of the current concept is the same as the query constraint.
     *
     * @param concept
     * @return
     */
    private boolean conceptIs(int conceptNid, ConceptFetcherBI fetcher) {
        return conceptNid == queryConstraintConcept.getConceptNid();
    }

    /**
     * Tests if the current concept is a child of the query constraint. This does not return true if they are
     * the same concept. This will check depth >= 1 to find children.
     *
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsDescendantOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException, ContradictionException {
        int parentNid = queryConstraintConcept.getNid();
        if (conceptNid == parentNid) {
            return false;
        }

        return ts.isKindOf(conceptNid, parentNid, viewCoordinate);
    }

    /**
     * Tests if the current concept is a child of the query constraint. This will return true if they are the
     * same concept. This will check depth >= 1 to find children.
     *
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptIsKindOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException, ContradictionException {
        return Ts.get().isKindOf(conceptNid, queryConstraintConcept.getNid(), config.getViewCoordinate());
    }

    private boolean conceptIs(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI pos)
            throws IOException {
        return conceptNid == queryConstraintConcept.getConceptNid();
    }

    /**
     * Tests if the current concept has a status the same as the query constraint.
     *
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(int conceptNid, ConceptFetcherBI fetcher) throws IOException {
        try {
            return conceptStatusIs((I_GetConceptData) fetcher.fetch(), queryConstraintConcept);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
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
            throws IOException {
        try {
            List<? extends I_ConceptAttributeTuple> tuples =
                    conceptBeingTested.getConceptAttributeTuples(null, termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

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
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
    }

    private boolean conceptStatusIs(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI pos)
            throws IOException {
        try {
            I_ConceptAttributeTuple<?> a = getVersion(conceptNid, fetcher, pos);
            return (a != null && a.getStatusNid() == queryConstraintConcept.getConceptNid());
        } catch (Exception ex) {
            throw new IOException(ex);
        }

    }

    private boolean conceptStatusIsChildOf(int conceptNid, ConceptFetcherBI fetcher,
            PositionSetBI pos) throws IOException {
        try {
            I_ConceptAttributeTuple<?> a = getVersion(conceptNid, fetcher, pos);
            if (a == null) {
                return false;
            }
            return conceptIsChildOf(Terms.get().getConcept(a.getStatusNid()),
                    this.queryConstraintConcept, pos);
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
    }

    private boolean conceptStatusIsDescendantOf(int conceptNid, ConceptFetcherBI fetcher,
            PositionSetBI pos) throws IOException {
        I_ConceptAttributeTuple<?> a = getVersion(conceptNid, fetcher, pos);
        if (a == null) {
            return false;
        }
        try {
            return conceptIsDescendantOf(Terms.get().getConcept(a.getStatusNid()),
                    this.queryConstraintConcept, pos);
        } catch (TerminologyException ex) {
            throw new IOException(ex);
        }
    }

    private boolean conceptStatusIsKindOf(int conceptNid, ConceptFetcherBI fetcher,
            PositionSetBI pos) throws IOException {
        return conceptStatusIs(conceptNid, fetcher, pos)
                || conceptStatusIsDescendantOf(conceptNid, fetcher, pos);
    }

    private boolean conceptIsChildOf(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI pos)
            throws IOException {
        try {
            return Ts.get().isChildOf(conceptNid, queryConstraintConcept.getConceptNid(), config.getViewCoordinate());
        } catch (Exception ex) {
           throw new IOException(ex);
        }
    }

    private boolean conceptIsDescendantOf(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI pos)
            throws IOException {
        if (conceptNid == queryConstraintConcept.getConceptNid()) {
            return false;
        }
        try {
            return Ts.get().isKindOf(conceptNid, queryConstraintConcept.getConceptNid(), config.getViewCoordinate());
        } catch (Exception ex) {
           throw new IOException(ex);
         }
    }

    private boolean conceptIsKindOf(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI pos)
            throws IOException, ContradictionException {
        return Ts.get().isKindOf(conceptNid, queryConstraintConcept.getConceptNid(), config.getViewCoordinate());
    }

    /**
     * Tests if the current concept has a status matching the query constraint, or any of its children (depth
     * >=1).
     *
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsKindOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException {

        // check if the concept's status matches the specified status
        if (conceptStatusIs(conceptNid, fetcher)) {
            return true;
        }

        return conceptStatusIsDescendantOf(conceptNid, fetcher);
    }

    /**
     * Tests if the current concept has a status matching the query constraint's immediate children.
     *
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsChildOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException {

        try {

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childStatuses =
                    queryConstraintConcept.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            // call conceptStatusIs on each
            for (I_GetConceptData childStatus : childStatuses) {
                if (conceptStatusIs((I_GetConceptData) fetcher.fetch(), childStatus)) {
                    return true;
                }

            }

            return false;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Tests if the current concept has a status matching the query constraint's children to depth >= 1.
     *
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsDescendantOf(int conceptNid, ConceptFetcherBI fetcher) throws IOException {

        return conceptStatusIsDescendantOf(conceptNid, fetcher, queryConstraintConcept);
    }

    /**
     * Tests if the current concept has a status matching the specified status' children to depth >= 1.
     *
     * @param conceptBeingTested
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIsDescendantOf(int conceptNid, ConceptFetcherBI fetcher, I_GetConceptData status)
            throws IOException {

        try {

            Set<? extends I_GetConceptData> childStatuses =
                    status.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData childStatus : childStatuses) {
                if (conceptStatusIs((I_GetConceptData) fetcher.fetch(), childStatus)) {
                    return true;
                } else if (conceptStatusIsDescendantOf(conceptNid, fetcher, childStatus)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private I_ConceptAttributeTuple<?> getVersion(int conceptNid, ConceptFetcherBI fetcher, PositionSetBI vn_is)
            throws IOException {
        try {
            // ArrayList<I_AmPart> parts = new ArrayList<I_AmPart>(
            // conceptBeingTested.getConceptAttributes().getMutableParts());
            // I_AmPart part = getVersion(parts, vn_is, false);
            // return (I_ConceptAttributePart) part;
            List<? extends I_ConceptAttributeTuple> a1s = ((I_GetConceptData) fetcher.fetch()).getConceptAttributeTuples(null, vn_is, Precedence.PATH,
                    config.getConflictResolutionStrategy());
            I_ConceptAttributeTuple<?> a1 = (a1s != null && a1s.size() > 0 ? a1s.get(0) : null);
            return a1;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private I_ConceptAttributeTuple<?> getVersion(int conceptNid, ConceptFetcherBI fetcher, GROUPING_TYPE version,
            PositionSetBI v1_is, PositionSetBI v2_is) throws IOException {
        return getVersion(conceptNid, fetcher, getVersion(version, v1_is, v2_is));
    }

    /**
     * Tests if the concept being tested has been added from v1 to v2
     *
     * @param conceptBeingTested
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    private boolean addedConcept(int conceptNid, ConceptFetcherBI fetcher,
            GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
            throws IOException {
        try {
            // TODO version must be v2
            I_ConceptAttributeTuple<?> a1 = getVersion(conceptNid, fetcher, v1_is);
            I_ConceptAttributeTuple<?> a2 = getVersion(conceptNid, fetcher, v2_is);
            return (a1 == null && a2 != null);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedConceptStatus(int conceptNid, ConceptFetcherBI fetcher,
            GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
            throws IOException {
        try {
            I_ConceptAttributeTuple<?> a1 = getVersion(conceptNid, fetcher,
                    v1_is);
            I_ConceptAttributeTuple<?> a2 = getVersion(conceptNid, fetcher,
                    v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2.getTime()) && a1.getStatusNid() != a2.getStatusNid());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedConceptDefined(int conceptNid, ConceptFetcherBI fetcher,
            GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is)
            throws IOException {
        try {
            I_ConceptAttributeTuple<?> a1 = getVersion(conceptNid, fetcher,
                    v1_is);
            I_ConceptAttributeTuple<?> a2 = getVersion(conceptNid, fetcher,
                    v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2.getTime()) && a1.isDefined() != a2.isDefined());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
