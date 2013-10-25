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
package org.ihtsdo.tk.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.search.ScoredComponentReference;
import org.ihtsdo.tk.query.RefsetSpecQuery.GROUPING_TYPE;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 *
 * @author Chrissy Hill
 *
 */
public class DescStatement extends RefsetSpecStatement {

    private NidBitSetBI possibleLuceneDescMatches;
    private NidBitSetBI possibleLuceneConcMatches;
    private Pattern regexPattern;
    private ViewCoordinate viewCoordinate;
    private TerminologyStoreDI ts;

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint The destination concept (e.g. "paracetamol")
     * @throws Exception
     */
    public DescStatement(boolean useNotQualifier, ConceptChronicleBI queryToken, ConceptChronicleBI queryConstraint,
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
    }

    /**
     * Constructor for refset spec statement.
     *
     * @param useNotQualifier Whether to use the NOT qualifier.
     * @param queryToken The query type to use (e.g. "concept is")
     * @param queryConstraint String value for regex or lucene search
     * @throws Exception
     */
    public DescStatement(boolean useNotQualifier, ConceptChronicleBI queryToken, String queryConstraint,
            ViewCoordinate viewCoordinate) throws Exception {
        super(useNotQualifier, queryToken, queryConstraint, viewCoordinate);

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
    public boolean getStatementResult(int componentNid, Object component, GROUPING_TYPE groupingVersion,
            ViewCoordinate v1Is, ViewCoordinate v2Is) throws IOException, ContradictionException {
        DescriptionVersionBI descVersion = null;
        if (DescriptionChronicleBI.class.isAssignableFrom(component.getClass())) {
            DescriptionChronicleBI description = (DescriptionChronicleBI) component;

//TODO: what was the point of this?
//            if (groupingVersion != null || v1Is != null || v2Is != null) {
//                if (groupingVersion == null) {
//                    throw new IOException("Not in scope of V1 or V2: " + tokenEnum + " "
//                            + description.toUserString());
//                }
//                if (v1Is == null) {
//                    throw new IOException("Need to set V1 IS: " + tokenEnum + " " + description.toUserString());
//                }
//                if (v2Is == null) {
//                    throw new IOException("Need to set V2 IS: " + tokenEnum + " " + description.toUserString());
//                }
//            }
            switch (tokenEnum) {
                case DESC_IS:
                    if (groupingVersion == null) {
                        return descriptionIs(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionIs(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_IS_MEMBER_OF:
                    if (groupingVersion == null) {
                        return descriptionIsMemberOf(description.getVersion(viewCoordinate));
                    } else {
                        throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                    }
                case DESC_STATUS_IS:
                    if (groupingVersion == null) {
                        return descriptionStatusIs(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionStatusIs(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_STATUS_IS_CHILD_OF:
                    if (groupingVersion == null) {
                        return descriptionStatusIsChildOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionStatusIsChildOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_STATUS_IS_KIND_OF:
                    if (groupingVersion == null) {
                        return descriptionStatusIsKindOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionStatusIsKindOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_STATUS_IS_DESCENDENT_OF:
                    if (groupingVersion == null) {
                        return descriptionStatusIsDescendentOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionStatusIsDescendentOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_TYPE_IS:
                    if (groupingVersion == null) {
                        return descriptionTypeIs(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionTypeIs(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_TYPE_IS_CHILD_OF:
                    if (groupingVersion == null) {
                        return descriptionTypeIsChildOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionTypeIsChildOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_TYPE_IS_KIND_OF:
                    if (groupingVersion == null) {
                        return descriptionTypeIsKindOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionTypeIsKindOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_TYPE_IS_DESCENDENT_OF:
                    if (groupingVersion == null) {
                        return descriptionTypeIsDescendentOf(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionTypeIsDescendentOf(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_REGEX_MATCH:
                    if (groupingVersion == null) {
                        return descriptionRegexMatch(description.getVersion(viewCoordinate));
                    } else {
                        return descriptionRegexMatch(description.getVersion(getViewCoordinate(groupingVersion, v1Is, v2Is)));
                    }
                case DESC_LUCENE_MATCH:
                    if (groupingVersion == null) {
                        return descriptionLuceneMatch(description.getVersion(viewCoordinate));
                    } else {
                        throw new IOException(tokenEnum + ": Unsupported operation for version scope.");
                    }
                case ADDED_DESCRIPTION:
                    return addedDescription(description, groupingVersion, v1Is, v2Is);
                case CHANGED_DESCRIPTION_CASE:
                    return changedDescriptionCase(description, groupingVersion, v1Is, v2Is);
                case CHANGED_DESCRIPTION_LANGUAGE:
                    return changedDescriptionLanguage(description, groupingVersion, v1Is, v2Is);
                case CHANGED_DESCRIPTION_STATUS:
                    return changedDescriptionStatus(description, groupingVersion, v1Is, v2Is);
                case CHANGED_DESCRIPTION_TERM:
                    return changedDescriptionTerm(description, groupingVersion, v1Is, v2Is);
                case CHANGED_DESCRIPTION_TYPE:
                    return changedDescriptionType(description, groupingVersion, v1Is, v2Is);
                default:
                    throw new RuntimeException("Can't handle queryToken: " + queryToken);
            }
        } else {
            return false;
        }
    }

    @Override
    public NidBitSetBI getPossibleConcepts(NidBitSetBI parentPossibleConcepts) throws IOException, ComputationCanceled {
        long startTime = System.currentTimeMillis();

        NidBitSetBI possibleConcepts = termFactory.getEmptyNidSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getEmptyNidSet();
        }

        switch (tokenEnum) {
            case DESC_IS:
                possibleConcepts.or(parentPossibleConcepts);
                break;
            case DESC_IS_MEMBER_OF:
                Collection<? extends RefexChronicleBI<?>> refsetExtensions
                        = termFactory.getConcept(((ConceptChronicleBI) queryConstraint).getConceptNid()).getRefsetMembers();
                NidBitSetBI refsetMemberSet = termFactory.getEmptyNidSet();
                for (RefexChronicleBI ext : refsetExtensions) {
                    refsetMemberSet.setMember(ext.getReferencedComponentNid());
                }
                if (isNegated()) {
                    possibleConcepts.or(parentPossibleConcepts);
                } else {
                    possibleConcepts.or(refsetMemberSet);
                }

                break;
            case DESC_LUCENE_MATCH:
                getPossibleDescriptions(termFactory.getEmptyNidSet());
                if (possibleLuceneConcMatches != null) {
                    possibleConcepts.or(possibleLuceneConcMatches);
                }
                break;
            case DESC_STATUS_IS:
            case DESC_STATUS_IS_CHILD_OF:
            case DESC_STATUS_IS_KIND_OF:
            case DESC_STATUS_IS_DESCENDENT_OF:
            case DESC_TYPE_IS:
            case DESC_TYPE_IS_CHILD_OF:
            case DESC_TYPE_IS_KIND_OF:
            case DESC_TYPE_IS_DESCENDENT_OF:
            case DESC_REGEX_MATCH:
                possibleConcepts.or(parentPossibleConcepts);
                break;
            case ADDED_DESCRIPTION:
            case CHANGED_DESCRIPTION_CASE:
            case CHANGED_DESCRIPTION_LANGUAGE:
            case CHANGED_DESCRIPTION_STATUS:
            case CHANGED_DESCRIPTION_TERM:
            case CHANGED_DESCRIPTION_TYPE:
                // TODO - EKM
                possibleConcepts.or(parentPossibleConcepts);
                break;
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.cardinality());

        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
        System.out.println("Elapsed: " + elapsedStr + ";  Incoming count: "
                + parentPossibleConcepts.cardinality() + "; Outgoing count: " + possibleConcepts.cardinality());
        return possibleConcepts;
    }

    @Override
    public NidBitSetBI getPossibleDescriptions(NidBitSetBI parentPossibleDescriptions) throws IOException {

        NidBitSetBI possibleDescriptions = termFactory.getEmptyNidSet();
        possibleLuceneDescMatches = null;
        possibleLuceneConcMatches = null;

        if (parentPossibleDescriptions == null) {
            parentPossibleDescriptions = termFactory.getEmptyNidSet();
        }

        switch (tokenEnum) {
            case DESC_IS:
                if (isNegated()) {
                    possibleDescriptions.or(parentPossibleDescriptions);
                } else {
                    possibleDescriptions.setMember(((DescriptionVersionBI) queryConstraint).getConceptNid());
                }
                break;
            case DESC_IS_MEMBER_OF:
                Collection<? extends RefexChronicleBI<?>> refsetExtensions
                        = termFactory.getConcept(((ConceptChronicleBI) queryConstraint).getNid()).getRefsetMembers();
                NidBitSetBI refsetMemberSet = termFactory.getEmptyNidSet();
                for (RefexChronicleBI ext : refsetExtensions) {
                    int componentId = ext.getReferencedComponentNid();
                    if (componentId == termFactory.getConceptNidForNid(componentId)) {
                        refsetMemberSet.setMember(componentId);
                    } else {
                        DescriptionChronicleBI desc = (DescriptionChronicleBI) termFactory.getComponent(componentId);
                        if (desc != null) {
                            refsetMemberSet.setMember(desc.getConceptNid());
                        }
                    }
                }
                if (isNegated()) {
                    possibleDescriptions.or(parentPossibleDescriptions);
                } else {
                    possibleDescriptions.or(refsetMemberSet);
                }

                break;
            case DESC_STATUS_IS:
            case DESC_STATUS_IS_CHILD_OF:
            case DESC_STATUS_IS_KIND_OF:
            case DESC_STATUS_IS_DESCENDENT_OF:
            case DESC_TYPE_IS:
            case DESC_TYPE_IS_CHILD_OF:
            case DESC_TYPE_IS_KIND_OF:
            case DESC_TYPE_IS_DESCENDENT_OF:
            case DESC_REGEX_MATCH:
                possibleDescriptions.or(parentPossibleDescriptions);
                break;
            case DESC_LUCENE_MATCH:
                if (isNegated()) {
                    possibleDescriptions.or(parentPossibleDescriptions);
                } else {
                    try {
                        String queryConstraintString = (String) queryConstraint;

                        Collection<ScoredComponentReference> results = termFactory.doTextSearch(queryConstraintString);

                        if (results == null || !results.isEmpty()) {
                            possibleDescriptions.or(parentPossibleDescriptions);
                            break;
                        }
                        possibleLuceneConcMatches = termFactory.getEmptyNidSet();
                        possibleLuceneDescMatches = termFactory.getEmptyNidSet();
                        for (ScoredComponentReference result : results) {
                            int cnid = result.getConceptNid();
                            int dnid = result.getComponentNid();
                            possibleLuceneDescMatches.setMember(dnid);
                            possibleLuceneConcMatches.setMember(cnid);
                            possibleDescriptions.setMember(dnid);

                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(DescStatement.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleDescriptions.cardinality());
        return possibleDescriptions;
    }

    @Override
    public NidBitSetBI getPossibleRelationships(NidBitSetBI parentPossibleConcepts) throws IOException {
        throw new IOException("Get possible relationships in desc statement unsupported operation.");
    }

    private boolean descriptionIsMemberOf(DescriptionVersionBI descriptionBeingTested) throws IOException {
        return componentIsMemberOf(descriptionBeingTested);
    }

    private boolean descriptionTypeIs(DescriptionVersionBI descriptionBeingTested) {
        return descriptionTypeIs((ConceptChronicleBI) queryConstraint, descriptionBeingTested);
    }

    private boolean descriptionTypeIs(ConceptChronicleBI requiredDescriptionType, DescriptionVersionBI descriptionBeingTested) {
        return descriptionBeingTested.getTypeNid() == requiredDescriptionType.getConceptNid();
    }

    /**
     * Checks if the description being tested has a description type matching
     * the query constraint. This also checks for the description type's
     * children (depth >= 1);
     */
    private boolean descriptionTypeIsKindOf(DescriptionVersionBI descriptionBeingChecked) throws IOException, ContradictionException {

        ConceptChronicleBI descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getTypeNid());
        return termFactory.isKindOf(descTypeBeingChecked.getNid(),
                ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
    }

    /**
     * This checks the description type for depth >= 1.
     *
     * @param requiredType
     * @param descriptionBeingChecked
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private boolean descriptionTypeIsDescendentOf(ConceptChronicleBI requiredType, DescriptionVersionBI descriptionBeingChecked)
            throws IOException, ContradictionException {
        ConceptChronicleBI descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getTypeNid());
        if (descTypeBeingChecked.getNid() == ((ConceptChronicleBI) queryConstraint).getNid()) {
            return false;
        }
        return termFactory.isKindOf(descTypeBeingChecked.getNid(),
                ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
    }

    private boolean descriptionTypeIsDescendentOf(DescriptionVersionBI descriptionBeingChecked) throws IOException, ContradictionException {
        return descriptionTypeIsDescendentOf((ConceptChronicleBI) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionTypeIsChildOf(DescriptionVersionBI descriptionBeingChecked) throws IOException {
        try {
            ConceptChronicleBI descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getTypeNid());
            return termFactory.isChildOf(descTypeBeingChecked.getNid(),
                    ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
        } catch (ContradictionException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    private boolean descriptionStatusIs(DescriptionVersionBI descriptionBeingChecked) throws IOException {
        return descriptionBeingChecked.getStatusNid() == ((ConceptChronicleBI) queryConstraint).getConceptNid();
    }

    private boolean descriptionStatusIsChildOf(DescriptionVersionBI descriptionBeingChecked) throws IOException {
        try {
            ConceptChronicleBI descTypeBeingChecked = termFactory.getConcept(descriptionBeingChecked.getStatusNid());
            return termFactory.isChildOf(descTypeBeingChecked.getNid(),
                    ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean descriptionStatusIsDescendentOf(DescriptionVersionBI descriptionBeingChecked) throws IOException, ContradictionException {
        return descriptionStatusIsDescendentOf((ConceptChronicleBI) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionStatusIsDescendentOf(ConceptChronicleBI requiredStatus,
            DescriptionVersionBI descriptionBeingChecked) throws IOException, ContradictionException {
        ConceptChronicleBI statusBeingChecked = termFactory.getConcept(descriptionBeingChecked.getStatusNid());
        if (statusBeingChecked.getNid() == requiredStatus.getNid()) {
            return false;
        }
        return termFactory.isKindOf(statusBeingChecked.getNid(),
                requiredStatus.getNid(), viewCoordinate);
    }

    private boolean descriptionStatusIsKindOf(DescriptionVersionBI descriptionBeingChecked) throws IOException, ContradictionException {
        ConceptChronicleBI statusBeingChecked = termFactory.getConcept(descriptionBeingChecked.getStatusNid());
        return termFactory.isChildOf(statusBeingChecked.getNid(),
                ((ConceptChronicleBI) queryConstraint).getNid(), viewCoordinate);
    }

    private boolean descriptionIs(DescriptionVersionBI descriptionBeingChecked) throws IOException {
        DescriptionChronicleBI queryConstraintDesc = (DescriptionChronicleBI) queryConstraint;
        return descriptionBeingChecked.getNid() == queryConstraintDesc.getNid();
    }

    private boolean descriptionRegexMatch(DescriptionVersionBI descriptionBeingChecked) throws IOException {
        if (regexPattern == null) {
            String queryConstraintString = (String) queryConstraint;
            regexPattern = Pattern.compile(queryConstraintString);
            System.out.println("Compiling regex: " + regexPattern + " into: " + regexPattern);
        }

        if (regexPattern.matcher(descriptionBeingChecked.getText()).find()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean descriptionLuceneMatch(DescriptionVersionBI descriptionBeingChecked) throws IOException {

        String queryConstraintString = (String) queryConstraint;
        if (possibleLuceneDescMatches != null) {
            if (possibleLuceneDescMatches.isMember(descriptionBeingChecked.getNid())) {
                return true;
            }
        }
        Collection<ScoredComponentReference> results = null;
        try {
            results = termFactory.doTextSearch(queryConstraintString);
        } catch (ParseException ex) {
            Logger.getLogger(DescStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (results == null || !results.isEmpty()) {
            return false;
        } else {
            for (ScoredComponentReference result : results) {
                int dnid = result.getComponentNid();
                DescriptionChronicleBI description = (DescriptionChronicleBI) termFactory.getComponent(dnid);
                if (descriptionBeingChecked.getNid() == description.getNid()
                        && descriptionBeingChecked.getConceptNid() == description.getConceptNid()) {
                    return true;
                }
            }

            return false;
        }

    }

    private boolean addedDescription(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 == null && a2 != null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedDescriptionCase(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.isInitialCaseSignificant() != a2.isInitialCaseSignificant());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedDescriptionLanguage(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && !a1.getLang().equals(a2.getLang()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedDescriptionStatus(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 != null && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getStatusNid() != a2.getStatusNid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedDescriptionTerm(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && !a1.getText().equals(a2.getText()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    private boolean changedDescriptionType(
            DescriptionChronicleBI descriptionBeingTested,
            GROUPING_TYPE version, ViewCoordinate v1_is, ViewCoordinate v2_is)
            throws IOException {
        try {
            DescriptionVersionBI a1 = descriptionBeingTested.getVersion(v1_is);
            DescriptionVersionBI a2 = descriptionBeingTested.getVersion(v2_is);
            return (a1 != null
                    && a2 != null
                    && !(a1.getPathNid() == a2.getPathNid()
                    && a1.getTime() == a2.getTime())
                    && a1.getTypeNid() != a2.getTypeNid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }
}
