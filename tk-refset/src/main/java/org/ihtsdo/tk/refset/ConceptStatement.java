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

package org.ihtsdo.tk.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.refset.RefsetSpecQuery.GROUPING_TYPE;
import org.ihtsdo.tk.refset.other.ActivityBI;

/**
 * Represents partial information contained in a refset spec. An example of a statement is : "NOT: Concept is
 * : Paracetamol"
 *
 * @author Chrissy Hill, Keith Campbell
 *
 */
public class ConceptStatement extends RefsetSpecStatement {

    ConceptChronicleBI queryConstraintConcept;
    private Collection<ActivityBI> activities;
//TODO: need activity panel
//    private StopActionListener stopListener = new StopActionListener();
    private TerminologyStoreDI ts;

//TODO: need activity panel
//    private class StopActionListener implements ActionListener {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            for (ActivityBI a : activities) {
//                a.cancel();
//            }
//        }
//    }

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     */
    public ConceptStatement(boolean useNotQualifier, ConceptChronicleBI queryToken, ConceptChronicleBI queryConstraint,
            ViewCoordinate viewCoordinate) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, viewCoordinate);
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
        queryConstraintConcept = queryConstraint;
    }

    @Override
    public NidBitSetBI getPossibleDescriptions(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException {
        throw new IOException("Get possible descriptions in concept statement unsupported operation.");
    }

    @Override
    public NidBitSetBI getPossibleRelationships(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException {
        throw new IOException("Get possible relationships in concept statement unsupported operation.");
    }

    @Override
    public NidBitSetBI getPossibleConcepts(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException, ComputationCanceled, ContradictionException {
        ActivityBI activity = null;
        long startTime = System.currentTimeMillis();
        this.activities = activities;

        NidBitSetBI possibleConcepts = termFactory.getEmptyNidSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getEmptyNidSet();
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
//TODO: need activity panel first             
//                activity = setupActivityPanel(parentPossibleConcepts);
//                activities.add(activity);

                if (isNegated()) {
                    possibleConcepts.or(parentPossibleConcepts);
                } else {
//TODO: need to implement
//                    NidBitSetBI results = queryConstraintConcept.getPossibleChildOfConcepts(config);
//                    possibleConcepts.or(results);
                }
                break;
            case CONCEPT_IS_DESCENDENT_OF:
            case CONCEPT_IS_KIND_OF:
//TODO: need activity panel first
//                activity = setupActivityPanel(parentPossibleConcepts);
//                activities.add(activity);
                if (isNegated()) {
                    possibleConcepts.or(parentPossibleConcepts);
                } else {
//TODO: need to implement
//                    NidBitSetBI results = (NidBitSetBI) queryConstraintConcept.getPossibleKindOfConcepts(config);
//                    possibleConcepts.or(results);
                }
                break;
            case CONCEPT_IS_MEMBER_OF:
//TODO: need activity panel first
//                activity = setupActivityPanel(parentPossibleConcepts);
                activities.add(activity);
                                Collection<? extends RefexChronicleBI<?>> refsetExtensions = 
                        termFactory.getConcept(((ConceptChronicleBI) queryConstraint).getNid()).getRefsetMembers();
                NidBitSetBI refsetMemberSet = termFactory.getEmptyNidSet();
                for (RefexChronicleBI ext : refsetExtensions) {
                    int componentId = ext.getReferencedComponentNid();
//TODO: this needs to check the concept nid
                    if (componentId == termFactory.getConceptNidForNid(componentId)) {
                        refsetMemberSet.setMember(componentId);
                    }
                }
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
            String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
            activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Incoming count: "
                    + parentPossibleConcepts.cardinality() + "; Outgoing count: " + possibleConcepts.cardinality());
            activity.complete();
        }
        return possibleConcepts;
    }
//TODO: need activity panel first
//    private I_ShowActivity setupActivityPanel(I_RepresentIdSet parentPossibleConcepts) {
//        I_ShowActivity activity;
//        activity = Terms.get().newActivityPanel(true, config, "<html>Possible: <br>" + this.toHtmlFragment(), true);
//        activity.setIndeterminate(true);
//        activity.setProgressInfoLower("Incoming count: " + parentPossibleConcepts.cardinality());
//        activity.addStopActionListener(stopListener);
//        return activity;
//    }

    @Override
    public boolean getStatementResult(int componentNid, Object component, GROUPING_TYPE groupingVersion, ViewCoordinate v1_is,
            ViewCoordinate v2_is) throws IOException, ContradictionException {
        if(ConceptChronicleBI.class.isAssignableFrom(component.getClass())){
            ConceptChronicleBI concept = (ConceptChronicleBI) component;
//TODO: what was the point of this?
//        if (version != null || v1_is != null || v2_is != null) {
//            if (version == null) {
//                try {
//                    throw new IOException("Not in scope of V1 or V2: "
//                            + tokenEnum + " " + fetcher.fetch().toString());
//                } catch (Exception ex) {
//                    throw new IOException(ex);
//                }
//            }
//            if (v1_is == null) {
//                try {
//                    throw new IOException("Need to set V1 IS: "
//                            + tokenEnum + " " + fetcher.fetch().toString());
//                } catch (Exception ex) {
//                    throw new IOException(ex);
//                }
//            }
//            if (v2_is == null) {
//                try {
//                    throw new IOException("Need to set V2 IS: "
//                            + tokenEnum + " " + fetcher.fetch().toString());
//                } catch (Exception ex) {
//                    throw new IOException(ex);
//                }
//            }
//        }

        switch (tokenEnum) {
            case CONCEPT_IS:
                if (groupingVersion == null) {
                    return conceptIs(componentNid);
                } else {
                    return conceptIs(componentNid);
                }
            case CONCEPT_IS_CHILD_OF:
                if (groupingVersion == null) {
                    return conceptIsChildOf(componentNid, viewCoordinate);
                } else {
                    return conceptIsChildOf(componentNid, getViewCoordinate(groupingVersion, v1_is, v2_is));
                }
            case CONCEPT_IS_DESCENDENT_OF:
                if (groupingVersion == null) {
                    return conceptIsDescendantOf(componentNid, viewCoordinate);
                } else {
                    return conceptIsDescendantOf(componentNid, getViewCoordinate(groupingVersion, v1_is, v2_is));
                }
            case CONCEPT_IS_KIND_OF:
                if (groupingVersion == null) {
                    return conceptIsKindOf(componentNid, viewCoordinate);
                } else {
                    return conceptIsKindOf(componentNid, getViewCoordinate(groupingVersion, v1_is, v2_is));
                }
            case CONCEPT_IS_MEMBER_OF:
                if (groupingVersion == null) {
                    return conceptIsMemberOf(concept.getVersion(viewCoordinate));
                } else {
                    throw new IOException(tokenEnum
                            + ": Unsupported operation for version scope.");
                }
            case CONCEPT_STATUS_IS:
                if (groupingVersion == null) {
                    return conceptStatusIs(concept.getVersion(viewCoordinate));
                } else {
                    return conceptStatusIs(concept.getVersion(getViewCoordinate(groupingVersion, v1_is, v2_is)));
                }
            case CONCEPT_STATUS_IS_CHILD_OF:
                if (groupingVersion == null) {
                    return conceptStatusIsChildOf(concept.getVersion(viewCoordinate));
                } else {
                    return conceptStatusIsChildOf(concept.getVersion(getViewCoordinate(groupingVersion, v1_is, v2_is)));
                }
            case CONCEPT_STATUS_IS_DESCENDENT_OF:
                if (groupingVersion == null) {
                    return conceptStatusIsDescendantOf(concept.getVersion(viewCoordinate));
                } else {
                    return conceptStatusIsDescendantOf(concept.getVersion(getViewCoordinate(groupingVersion, v1_is, v2_is)));
                }
            case CONCEPT_STATUS_IS_KIND_OF:
                if (groupingVersion == null) {
                    return conceptStatusIsKindOf(concept.getVersion(viewCoordinate));
                } else {
                    return conceptStatusIsKindOf(concept.getVersion(getViewCoordinate(groupingVersion, v1_is, v2_is)));
                }
            case ADDED_CONCEPT:
                return addedConcept(concept, v1_is, v2_is);
            case CHANGED_CONCEPT_STATUS:
                return changedConceptStatus(concept, v1_is, v2_is);
            case CHANGED_CONCEPT_DEFINED:
                return changedConceptDefined(concept, v1_is, v2_is);
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        }else{
            return false;
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
    private boolean conceptIsChildOf(int conceptNid, ViewCoordinate viewCoordinate) throws IOException {
        try {

            return Ts.get().isChildOf(conceptNid, queryConstraintConcept.getConceptNid(), viewCoordinate);
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
    private boolean conceptIsMemberOf(ConceptVersionBI conceptBeingTested) throws IOException {
        return componentIsMemberOf(conceptBeingTested);
    }

    /**
     * Tests of the current concept is the same as the query constraint.
     *
     * @param concept
     * @return
     */
    private boolean conceptIs(int conceptNid) {
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
    private boolean conceptIsDescendantOf(int conceptNid, ViewCoordinate viewCoordinate) throws IOException, ContradictionException {
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
    private boolean conceptIsKindOf(int conceptNid, ViewCoordinate viewCoordinate) throws IOException, ContradictionException {
        return Ts.get().isKindOf(conceptNid, queryConstraintConcept.getNid(), viewCoordinate);
    }

    /**
     * Tests if the current concept has a status the same as the query constraint.
     *
     * @param concept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean conceptStatusIs(ConceptVersionBI conceptBeingTested) throws IOException {
        try {
            return conceptBeingTested.getStatusNid() == ((ConceptChronicleBI) queryConstraint).getConceptNid();
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }
    
//TODO: not sure this is working right
    private boolean conceptStatusIsChildOf(ConceptVersionBI conceptBeingTested) throws IOException {
        try {
            Collection<? extends ConceptVersionBI> childStatuses
                    = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptVersionBI childStatus : childStatuses) {
                if (conceptStatusIs(childStatus)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean conceptStatusIsDescendantOf(ConceptVersionBI conceptBeingTested) throws IOException, ContradictionException {
        ConceptChronicleBI statusBeingChecked = termFactory.getConcept(conceptBeingTested.getStatusNid());
//TODO: pass in view coordinate? also, why isn't required status being used?            
            return termFactory.isChildOf(statusBeingChecked.getNid(),
                    ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
    }

    private boolean conceptStatusIsKindOf(ConceptVersionBI conceptBeingTested) throws IOException, ContradictionException {
        return conceptStatusIs(conceptBeingTested)
                || conceptStatusIsDescendantOf(conceptBeingTested);
    }

    /**
     * Tests if the concept being tested has been added from v1 to v2
     *
     * @param conceptBeingTested
     * @return
     * @throws TerminologyException
     * @throws IOException
     */
    private boolean addedConcept(ConceptChronicleBI concept,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            // TODO version must be v2
            ConceptVersionBI a1 = concept.getVersion(v1_is);
            ConceptVersionBI a2 = concept.getVersion(v2_is);
            return (a1 == null && a2 != null);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedConceptStatus(ConceptChronicleBI concept,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            ConceptVersionBI a1 = concept.getVersion(v1_is);
            ConceptVersionBI a2 = concept.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid() && a1.getTime() == a2.getTime()) 
                    && a1.getStatusNid() != a2.getStatusNid());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedConceptDefined(ConceptChronicleBI concept,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
//            ConceptVersionBI a1 = concept.getVersion(v1_is);
//            ConceptVersionBI a2 = concept.getVersion(v2_is);
            ConceptAttributeVersionBI ca1 = concept.getConceptAttributes().getVersion(v1_is);
            ConceptAttributeVersionBI ca2 = concept.getConceptAttributes().getVersion(v2_is);
            return (ca1 != null && ca2 != null
                    && ca1.isDefined() != ca2.isDefined());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
