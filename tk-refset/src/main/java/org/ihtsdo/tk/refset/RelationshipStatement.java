/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.refset;

import org.ihtsdo.tk.refset.other.ActivityBI;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.refset.RefsetSpecQuery.GROUPING_TYPE;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 *
 * @author Chrissy Hill
 *
 */
public class RelationshipStatement extends RefsetSpecStatement {

    private Collection<ActivityBI> activities;

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     */
    public RelationshipStatement(boolean useNotQualifier,
            ConceptChronicleBI queryToken,
            ConceptChronicleBI queryConstraint,
            ViewCoordinate viewCoordinate) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, viewCoordinate);
        this.viewCoordinate = viewCoordinate;
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptNid() == token.nid) {
                tokenEnum = token;
                break;
            }
        }

        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    @Override
    public boolean getStatementResult(int componentNid, Object component, GROUPING_TYPE groupingVersion, ViewCoordinate v1_is,
            ViewCoordinate v2_is) throws IOException, ContradictionException {
        ComponentChronicleBI<?> component1 = Ts.get().getComponent(componentNid);
        RelationshipChronicleBI relationship = (RelationshipChronicleBI) Ts.get().getComponent(componentNid);
        RelationshipVersionBI relVersion = relationship.getVersion(viewCoordinate);
//TODO
//        if (groupingVersion != null || v1_is != null || v2_is != null) {
//            if (groupingVersion == null) {
//                throw new IOException("Not in scope of V1 or V2: " + tokenEnum + " " + relVersion);
//            }
//            if (v1_is == null) {
//                throw new IOException("Need to set V1 IS: " + tokenEnum + " " + relVersion);
//            }
//            if (v2_is == null) {
//                throw new IOException("Need to set V2 IS: " + tokenEnum + " " + relVersion);
//            }
//        }
        switch (tokenEnum) {
            case REL_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relIs(relVersion);
            case REL_RESTRICTION_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relRestrictionIs(relVersion);
            case REL_IS_MEMBER_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relIsMemberOf(relVersion);
            case REL_STATUS_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relStatusIs(relVersion);
            case REL_STATUS_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relStatusIsKindOf(relVersion);
            case REL_STATUS_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relStatusIsChildOf(relVersion);
            case REL_STATUS_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relStatusIsDescendentOf(relVersion);
            case REL_TYPE_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relTypeIs(relVersion);
            case REL_TYPE_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relTypeIsKindOf(relVersion);
            case REL_TYPE_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relTypeIsChildOf(relVersion);
            case REL_TYPE_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relTypeIsDescendentOf(relVersion);
            case REL_LOGICAL_QUANTIFIER_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relLogicalQuantifierIs(relVersion);
            case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relLogicalQuantifierIsKindOf(relVersion);
            case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relLogicalQuantifierIsChildOf(relVersion);
            case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relLogicalQuantifierIsDescendentOf(relVersion);
            case REL_CHARACTERISTIC_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relCharIs(relVersion);
            case REL_CHARACTERISTIC_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relCharIsKindOf(relVersion);
            case REL_CHARACTERISTIC_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relCharIsChildOf(relVersion);
            case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relCharIsDescendentOf(relVersion);
            case REL_REFINABILITY_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relRefinabilityIs(relVersion);
            case REL_REFINABILITY_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relRefinabilityIsKindOf(relVersion);
            case REL_REFINABILITY_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relRefinabilityIsChildOf(relVersion);
            case REL_REFINABILITY_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relRefinabilityIsDescendentOf(relVersion);
            case REL_DESTINATION_IS:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relDestinationIs(relVersion);
            case REL_DESTINATION_IS_KIND_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relDestinationIsKindOf(relVersion);
            case REL_DESTINATION_IS_CHILD_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relDestinationIsChildOf(relVersion);
            case REL_DESTINATION_IS_DESCENDENT_OF:
                if (groupingVersion != null) {
                    throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                }
                return relDestinationIsDescendentOf(relVersion);
            case ADDED_RELATIONSHIP:
                return addedRelationship(relationship, v1_is, v2_is);
            case CHANGED_RELATIONSHIP_CHARACTERISTIC:
                return changedRelationshipCharacteristic(relationship, v1_is, v2_is);
            case CHANGED_RELATIONSHIP_GROUP:
                return changedRelationshipGroup(relationship, v1_is, v2_is);
            case CHANGED_RELATIONSHIP_REFINABILITY:
                return changedRelationshipRefinability(relationship, v1_is, v2_is);
            case CHANGED_RELATIONSHIP_STATUS:
                return changedRelationshipStatus(relationship, v1_is, v2_is);
            case CHANGED_RELATIONSHIP_TYPE:
                return changedRelationshipType(relationship, v1_is, v2_is);
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
    }

    @Override
    public NidBitSetBI getPossibleConcepts(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException, ComputationCanceled {
        ActivityBI activity = null;
        long startTime = System.currentTimeMillis();
        this.activities = activities;

        NidBitSetBI possibleConcepts = termFactory.getEmptyNidSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getAllConceptNids();
        }

        switch (tokenEnum) {
            case REL_IS_MEMBER_OF:
                Collection<? extends RefexVersionBI<?>> refsetMembersActive = ((ConceptChronicleBI) queryConstraint).getRefsetMembersActive(viewCoordinate);
                NidBitSetBI refsetMemberSet = Ts.get().getEmptyNidSet();
                for (RefexVersionBI member : refsetMembersActive) {
                    refsetMemberSet.setMember(member.getReferencedComponentNid());
                }
                if (isNegated()) {
                    possibleConcepts.or(parentPossibleConcepts);
                } else {
                    possibleConcepts.or(refsetMemberSet);
                }

                break;
            case REL_IS:
            case REL_RESTRICTION_IS:
            case REL_STATUS_IS:
            case REL_STATUS_IS_KIND_OF:
            case REL_STATUS_IS_CHILD_OF:
            case REL_STATUS_IS_DESCENDENT_OF:
            case REL_TYPE_IS:
            case REL_TYPE_IS_KIND_OF:
            case REL_TYPE_IS_CHILD_OF:
            case REL_TYPE_IS_DESCENDENT_OF:
            case REL_LOGICAL_QUANTIFIER_IS:
            case REL_LOGICAL_QUANTIFIER_IS_KIND_OF:
            case REL_LOGICAL_QUANTIFIER_IS_CHILD_OF:
            case REL_LOGICAL_QUANTIFIER_IS_DESCENDENT_OF:
            case REL_CHARACTERISTIC_IS:
            case REL_CHARACTERISTIC_IS_KIND_OF:
            case REL_CHARACTERISTIC_IS_CHILD_OF:
            case REL_CHARACTERISTIC_IS_DESCENDENT_OF:
            case REL_DESTINATION_IS:
            case REL_DESTINATION_IS_KIND_OF:
            case REL_DESTINATION_IS_CHILD_OF:
            case REL_DESTINATION_IS_DESCENDENT_OF:
            case REL_REFINABILITY_IS:
            case REL_REFINABILITY_IS_KIND_OF:
            case REL_REFINABILITY_IS_CHILD_OF:
            case REL_REFINABILITY_IS_DESCENDENT_OF:
                possibleConcepts.or(parentPossibleConcepts);
                break;
            case ADDED_RELATIONSHIP:
            case CHANGED_RELATIONSHIP_CHARACTERISTIC:
            case CHANGED_RELATIONSHIP_GROUP:
            case CHANGED_RELATIONSHIP_REFINABILITY:
            case CHANGED_RELATIONSHIP_STATUS:
            case CHANGED_RELATIONSHIP_TYPE:
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

    @Override
    public NidBitSetBI getPossibleDescriptions(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException {
        throw new IOException("Get possible descriptions in rel statement unsupported operation.");
    }

    @Override
    public NidBitSetBI getPossibleRelationships(NidBitSetBI parentPossibleConcepts,
            Collection<ActivityBI> activities) throws IOException {
        throw new IOException("Get possible relationships in rel statement unsupported operation.");
    }

    private boolean relRefinabilityIsDescendentOf(RelationshipVersionBI relationsihp) throws IOException {
        return relRefinabilityIsDescendentOf((ConceptChronicleBI) queryConstraint, relationsihp);
    }

    private boolean relRefinabilityIsDescendentOf(ConceptChronicleBI requiredRefinability,
            RelationshipVersionBI relationship)
            throws IOException {

        try {
            Collection<? extends ConceptVersionBI> children =
                    requiredRefinability.getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptVersionBI child : children) {
                if (relRefinabilityIs(child, relationship)) {
                    return true;
                } else if (relRefinabilityIsDescendentOf(child, relationship)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relRefinabilityIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        try {
            Collection<? extends ConceptVersionBI> children = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptVersionBI child : children) {
                if (relRefinabilityIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relRefinabilityIsKindOf(RelationshipVersionBI relTuple) throws IOException {
        if (relRefinabilityIs(relTuple)) {
            return true;
        }

        return relRefinabilityIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);

    }

    private boolean relRefinabilityIs(RelationshipVersionBI relTuple) {
        return relRefinabilityIs((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relRefinabilityIs(ConceptChronicleBI requiredRefinability, RelationshipVersionBI relTuple) {
        return relTuple.getRefinabilityNid() == requiredRefinability.getConceptNid();
    }

    private boolean relCharIsDescendentOf(RelationshipVersionBI relTuple) throws IOException {
        return relCharIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relCharIsDescendentOf(ConceptChronicleBI requiredCharType, RelationshipVersionBI relTuple) throws IOException {
        try {
            Collection<? extends ConceptVersionBI> children =
                    requiredCharType.getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptChronicleBI child : children) {
                if (relCharIs(child, relTuple)) {
                    return true;
                } else if (relCharIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return false;
    }

    private boolean relCharIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptChronicleBI child : children) {
                if (relCharIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relCharIsKindOf(RelationshipVersionBI relTuple) throws IOException {

        if (relCharIs(relTuple)) {
            return true;
        }

        return relCharIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);

    }

    private boolean relCharIs(RelationshipVersionBI relTuple) {
        return relCharIs((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relCharIs(ConceptChronicleBI requiredCharType, RelationshipVersionBI relTuple) {
        return relTuple.getCharacteristicNid() == requiredCharType.getConceptNid();
    }

    private boolean relIsMemberOf(RelationshipVersionBI relTuple) throws IOException {
        return componentIsMemberOf(relTuple);
    }

    private boolean relTypeIs(RelationshipVersionBI relTuple) {
        return relTypeIs((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relTypeIs(ConceptChronicleBI requiredRelType, RelationshipVersionBI relTuple) {
        return relTuple.getTypeNid() == requiredRelType.getConceptNid();
    }

    private boolean relTypeIsKindOf(RelationshipVersionBI relTuple) throws IOException {

        if (relTypeIs(relTuple)) {
            return true;
        }

        return relTypeIsDescendentOf(relTuple);
    }

    private boolean relTypeIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptVersionBI child : children) {
                if (relTypeIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relTypeIsDescendentOf(RelationshipVersionBI relTuple) throws IOException {
        return relTypeIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relTypeIsDescendentOf(ConceptChronicleBI requiredRelType, RelationshipVersionBI relTuple) throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = requiredRelType.getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptVersionBI child : children) {
                if (relTypeIs(child, relTuple)) {
                    return true;
                } else if (relTypeIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relStatusIsDescendentOf(RelationshipVersionBI relTuple) throws IOException {
        return relStatusIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relStatusIsDescendentOf(ConceptChronicleBI requiredStatus, RelationshipVersionBI relTuple)
            throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = requiredStatus.getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptChronicleBI child : children) {
                if (relStatusIs(child, relTuple)) {
                    return true;
                } else if (relStatusIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relStatusIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();

            for (ConceptChronicleBI child : children) {
                if (relStatusIs(child, relTuple)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        return false;
    }

    private boolean relStatusIsKindOf(RelationshipVersionBI relTuple) throws IOException {
        if (relStatusIs(relTuple)) {
            return true;
        }

        return relStatusIsDescendentOf(relTuple);
    }

    private boolean relStatusIs(RelationshipVersionBI relTuple) {
        return relStatusIs((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relStatusIs(ConceptChronicleBI requiredStatus, RelationshipVersionBI relTuple) {
        return componentStatusIs(requiredStatus, relTuple);
    }

    private boolean relIs(RelationshipVersionBI relTuple) throws IOException {
        RelationshipVersionBI queryConstraintRel = (RelationshipVersionBI) queryConstraint;
        return relTuple.equals(queryConstraintRel);
    }

    private boolean relRestrictionIs(RelationshipVersionBI relTuple) throws IOException {
        throw new IOException("Unimplemented query : rel restriction is");
    }

    private boolean relLogicalQuantifierIsDescendentOf(RelationshipVersionBI relTuple) throws IOException {
        throw new IOException("Unimplemented query : rel logical quantifier is descendent");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        throw new IOException("Unimplemented query : rel logical quantifier is child of");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIsKindOf(RelationshipVersionBI relTuple) throws IOException {
        throw new IOException("Unimplemented query : rel logical quantifier is kind of");
        // unimplemented TODO
    }

    private boolean relLogicalQuantifierIs(RelationshipVersionBI relTuple) throws IOException {
        throw new IOException("Unimplemented query : rel logical quantifier is");
        // unimplemented TODO
    }

    private boolean relDestinationIs(RelationshipVersionBI relTuple) {
        return relDestinationIs((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relDestinationIs(ConceptChronicleBI requiredDestination, RelationshipVersionBI relTuple) {
        return relTuple.getTargetNid() == requiredDestination.getConceptNid();
    }

    private boolean relDestinationIsKindOf(RelationshipVersionBI relTuple) throws IOException {

        if (relDestinationIs(relTuple)) {
            return true;
        }

        return relDestinationIsDescendentOf(relTuple);
    }

    private boolean relDestinationIsChildOf(RelationshipVersionBI relTuple) throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = ((ConceptChronicleBI) queryConstraint).getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();


            for (ConceptChronicleBI child : children) {
                if (relDestinationIs(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean relDestinationIsDescendentOf(RelationshipVersionBI relTuple) throws IOException {
        return relDestinationIsDescendentOf((ConceptChronicleBI) queryConstraint, relTuple);
    }

    private boolean relDestinationIsDescendentOf(ConceptChronicleBI requiredDestination, RelationshipVersionBI relTuple)
            throws IOException {
        try {

            Collection<? extends ConceptVersionBI> children = requiredDestination.getVersion(viewCoordinate).getRelationshipsIncomingSourceConceptsActiveIsa();


            for (ConceptChronicleBI child : children) {
                if (relDestinationIs(child, relTuple)) {
                    return true;
                } else if (relDestinationIsDescendentOf(child, relTuple)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean addedRelationship(RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            //            if v2 has a relationship that is semantically the same as v1, then return false
            //            only care about active
            NidSetBI activeNids = new NidSet();
            activeNids.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
            activeNids.add(SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid());
            ViewCoordinate v1Active = new ViewCoordinate(v1_is);
            v1Active.setAllowedStatusNids(activeNids);
            ViewCoordinate v2Active = new ViewCoordinate(v2_is);
            v2Active.setAllowedStatusNids(activeNids);
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            if (a1 == null && a2 != null && a2.getStatusNid() == SnomedMetadataRfx.getSTATUS_CURRENT_NID()) {
                ConceptChronicleBI enclosingConcept = relBeingTested.getEnclosingConcept();

                UUID v2Hash = null;
                if (a2.getGroup() == 0) {
                    v2Hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(a2.getSourceNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a2.getTypeNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a2.getTargetNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a2.getCharacteristicNid()).toString());
                } else {
                    String groupHash = null;
                    int group = a2.getGroup();
                    ConceptVersionBI v2Concept = enclosingConcept.getVersion(v2Active);
                    Collection<? extends RelationshipVersionBI> v2Rels = v2Concept.getRelationshipsOutgoingActive();
                    ArrayList<String> hashes = new ArrayList<>();
                    String hash = null;
                    for (RelationshipVersionBI rel : v2Rels) {
                        if (rel.getGroup() == group && a2.getCharacteristicNid() == rel.getCharacteristicNid()) {
                            hash = Ts.get().getUuidPrimordialForNid(rel.getSourceNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getCharacteristicNid()).toString();
                            hashes.add(hash);
                        }
                    }
                    Collections.sort(hashes);
                    for (String s : hashes) {
                        groupHash = groupHash + s;
                    }
                    v2Hash = Type5UuidFactory.get(groupHash);
                }

                ConceptVersionBI v1Concept = enclosingConcept.getVersion(v1Active);
                Collection<? extends RelationshipVersionBI> v1Rels = v1Concept.getRelationshipsOutgoingActive();
                HashSet<UUID> v1RelHashes = new HashSet<>();
                for (RelationshipVersionBI rel : v1Rels) {
                    if (rel.getGroup() == 0) {
                        UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getSourceNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getCharacteristicNid()).toString());
                        v1RelHashes.add(hash);
                    } else {
                        int group = rel.getGroup();
                        ArrayList<String> hashes = new ArrayList<>();
                        String hash = null;
                        for (RelationshipVersionBI r : v1Rels) {
                            if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()) {
                                hash = Ts.get().getUuidPrimordialForNid(r.getSourceNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getCharacteristicNid()).toString();
                                hashes.add(hash);
                            }
                        }
                        Collections.sort(hashes);
                        String groupHash = null;
                        for (String s : hashes) {
                            groupHash = groupHash + s;
                        }
                        v1RelHashes.add(Type5UuidFactory.get(groupHash));
                    }
                }

                if (v1RelHashes.contains(v2Hash)) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedRelationshipCharacteristic(
            RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getCharacteristicNid() != a2.getCharacteristicNid());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedRelationshipGroup(RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getGroup() != a2.getGroup());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedRelationshipRefinability(
            RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getRefinabilityNid() != a2.getRefinabilityNid());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedRelationshipStatus(RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            if (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getStatusNid() != a2.getStatusNid()) {
                UUID v1Hash = null;
                if (a1.getGroup() == 0) {
                    v1Hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(a1.getSourceNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a1.getTypeNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a1.getTargetNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a1.getCharacteristicNid()).toString()
                            + Ts.get().getUuidPrimordialForNid(a1.getStatusNid()).toString());
                } else {
                    String groupHash = null;
                    int group = a1.getGroup();
                    ConceptVersionBI v1Concept = a1.getEnclosingConcept().getVersion(v1_is);
                    Collection<? extends RelationshipVersionBI> v1Rels = v1Concept.getRelationshipsOutgoingActive();
                    ArrayList<String> hashes = new ArrayList<>();
                    String hash = null;
                    for (RelationshipVersionBI rel : v1Rels) {
                        if (rel.getGroup() == group && a1.getCharacteristicNid() == rel.getCharacteristicNid()) {
                            if(rel.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID() 
                                    && rel.getStatusNid() == SnomedMetadataRfx.getSTATUS_CURRENT_NID()){
                               RelationshipVersionBI mergeVersion = (RelationshipVersionBI) rel.getChronicle().getVersion(v2_is);
                                if(mergeVersion != null && (mergeVersion.getTime() > rel.getTime())){
                                    //there has been a classification on the merge path that retired the classificaiton on the target path
                                    //should use this instead
                                    rel = mergeVersion;
                                }
                            }
                            hash = Ts.get().getUuidPrimordialForNid(rel.getSourceNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getCharacteristicNid()).toString()
                                    + Ts.get().getUuidPrimordialForNid(rel.getStatusNid()).toString();
                            //classifier creates duplicate relationships these should not be considered
                            if(rel.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()){
                                if(!hashes.contains(hash)){
                                    hashes.add(hash);
                                }
                            }else{
                                hashes.add(hash);
                            }
                        }
                    }
                    Collections.sort(hashes);
                    for (String s : hashes) {
                        groupHash = groupHash + s;
                    }
                    v1Hash = Type5UuidFactory.get(groupHash);
                }

                HashSet<UUID> v2RelHashes = new HashSet<>();
                ConceptVersionBI v2Concept = a2.getEnclosingConcept().getVersion(v2_is);
                Collection<? extends RelationshipVersionBI> v2Rels = v2Concept.getRelationshipsOutgoingActive();
                for (RelationshipVersionBI rel : v2Rels) {
                    if (rel.getGroup() == 0) {
                        UUID hash = Type5UuidFactory.get(Ts.get().getUuidPrimordialForNid(rel.getSourceNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getTypeNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getTargetNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getCharacteristicNid()).toString()
                                + Ts.get().getUuidPrimordialForNid(rel.getStatusNid()).toString());
                        v2RelHashes.add(hash);
                    } else {
                        int group = rel.getGroup();
                        ArrayList<String> hashes = new ArrayList<>();
                        String hash = null;
                        for (RelationshipVersionBI r : v2Rels) {
                            if (r.getGroup() == group && r.getCharacteristicNid() == rel.getCharacteristicNid()
                                    && r.getStatusNid() == rel.getStatusNid()) {
                                hash = Ts.get().getUuidPrimordialForNid(r.getSourceNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getTypeNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getTargetNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getCharacteristicNid()).toString()
                                        + Ts.get().getUuidPrimordialForNid(r.getStatusNid()).toString();
                                
                                //classifier creates duplicate relationships these should not be considered
                                if (r.getCharacteristicNid() == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()) {
                                    if (!hashes.contains(hash)) {
                                        hashes.add(hash);
                                    }
                                } else {
                                    hashes.add(hash);
                                }
                            }
                        }
                        Collections.sort(hashes);
                        String groupHash = null;
                        for (String s : hashes) {
                            groupHash = groupHash + s;
                        }
                        v2RelHashes.add(Type5UuidFactory.get(groupHash));
                    }
                }
                if (v2RelHashes.contains(v1Hash)) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedRelationshipType(RelationshipChronicleBI relBeingTested,
            ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            RelationshipVersionBI a1 = relBeingTested.getVersion(v1_is);
            RelationshipVersionBI a2 = relBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getTypeNid() != a2.getTypeNid());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
