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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec. An example of a
 * statement is : "NOT: Concept is : Paracetamol"
 * 
 * @author Chrissy Hill
 * 
 */
public class DescStatement extends RefsetSpecStatement {

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
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryToken, I_AmTermComponent queryConstraint) {
        super(useNotQualifier, queryToken, queryConstraint);
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptId() == token.nid) {
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
     * @param useNotQualifier
     *            Whether to use the NOT qualifier.
     * @param queryToken
     *            The query type to use (e.g. "concept is")
     * @param queryConstraint
     *            String value for regex or lucene search
     */
    public DescStatement(boolean useNotQualifier, I_GetConceptData queryToken, String queryConstraint) {
        super(useNotQualifier, queryToken, queryConstraint);
        for (QUERY_TOKENS token : QUERY_TOKENS.values()) {
            if (queryToken.getConceptId() == token.nid) {
                tokenEnum = token;
                break;
            }
        }
        if (tokenEnum == null) {
            throw new RuntimeException("Unknown query type : " + queryToken);
        }
    }

    public boolean getStatementResult(I_AmTermComponent component) throws IOException, TerminologyException {
        if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
            I_DescriptionVersioned descriptionVersioned = (I_DescriptionVersioned) component;
            I_DescriptionTuple descriptionTuple = descriptionVersioned.getLastTuple();

            switch (tokenEnum) {
            case DESC_IS:
                return descriptionIs(descriptionTuple);
            case DESC_IS_MEMBER_OF:
                return descriptionIsMemberOf(descriptionTuple);
            case DESC_STATUS_IS:
                return descriptionStatusIs(descriptionTuple);
            case DESC_STATUS_IS_CHILD_OF:
                return descriptionStatusIsChildOf(descriptionTuple);
            case DESC_STATUS_IS_KIND_OF:
                return descriptionStatusIsKindOf(descriptionTuple);
            case DESC_STATUS_IS_DESCENDENT_OF:
                return descriptionStatusIsDescendentOf(descriptionTuple);
            case DESC_TYPE_IS:
                return descriptionTypeIs(descriptionTuple);
            case DESC_TYPE_IS_CHILD_OF:
                return descriptionTypeIsChildOf(descriptionTuple);
            case DESC_TYPE_IS_KIND_OF:
                return descriptionTypeIsKindOf(descriptionTuple);
            case DESC_TYPE_IS_DESCENDENT_OF:
                return descriptionTypeIsDescendentOf(descriptionTuple);
            case DESC_REGEX_MATCH:
                return descriptionRegexMatch(descriptionTuple);
            case DESC_LUCENE_MATCH:
                return descriptionLuceneMatch(descriptionTuple);
            default:
                throw new RuntimeException("Can't handle queryToken: " + queryToken);
            }
        } else {
            return false;
        }
    }

    @Override
    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame configFrame, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        I_RepresentIdSet possibleConcepts = termFactory.getEmptyIdSet();
        if (parentPossibleConcepts == null) {
            parentPossibleConcepts = termFactory.getConceptIdSet();
        }

        switch (tokenEnum) {
        case DESC_IS:
            possibleConcepts.or(parentPossibleConcepts);
            break;
        case DESC_IS_MEMBER_OF:
            Collection<? extends I_ExtendByRef> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_GetConceptData) queryConstraint).getConceptId());
            Set<I_GetConceptData> refsetMembers = new HashSet<I_GetConceptData>();
            for (I_ExtendByRef ext : refsetExtensions) {
                refsetMembers.add(termFactory.getConcept(ext.getComponentId()));
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetfromTermCollection(refsetMembers);
            if (isNegated()) {
                possibleConcepts.or(parentPossibleConcepts);
                // possibleConcepts = termFactory.getConceptIdSet();
                // possibleConcepts.removeAll(refsetMemberSet);
            } else {
                possibleConcepts.or(refsetMemberSet);
            }

            break;
        case DESC_LUCENE_MATCH:
            getPossibleDescriptions(configFrame, termFactory.getEmptyIdSet());
            possibleConcepts.and(possibleLuceneConcMatches);
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
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleConcepts.cardinality());
        return possibleConcepts;
    }

    I_RepresentIdSet possibleLuceneDescMatches;
    I_RepresentIdSet possibleLuceneConcMatches;
    public I_RepresentIdSet getPossibleDescriptions(I_ConfigAceFrame configFrame,
            I_RepresentIdSet parentPossibleDescriptions) throws TerminologyException, IOException {

        I_RepresentIdSet possibleDescriptions = termFactory.getEmptyIdSet();
        possibleLuceneDescMatches = null;
        possibleLuceneConcMatches = null;
        
        if (parentPossibleDescriptions == null) {
            parentPossibleDescriptions = termFactory.getConceptIdSet();
        }

        switch (tokenEnum) {
        case DESC_IS:
            if (isNegated()) {
                possibleDescriptions.or(parentPossibleDescriptions);
            } else {
                possibleDescriptions.setMember(((I_DescriptionVersioned) queryConstraint).getConceptId());
            }
            break;
        case DESC_IS_MEMBER_OF:
            Collection<? extends I_ExtendByRef> refsetExtensions =
                    termFactory.getRefsetExtensionMembers(((I_AmTermComponent) queryConstraint).getNid());
            Set<Integer> refsetMembers = new HashSet<Integer>();
            for (I_ExtendByRef ext : refsetExtensions) {
                int componentId = ext.getComponentId();
                if (Terms.get().hasConcept(componentId)) {
                    refsetMembers.add(componentId);
                } else {
                    I_DescriptionVersioned desc = Terms.get().getDescription(componentId);
                    if (desc != null) {
                        refsetMembers.add(desc.getConceptId());
                    }
                }
            }
            I_RepresentIdSet refsetMemberSet = termFactory.getIdSetFromIntCollection(refsetMembers);
            if (isNegated()) {
                possibleDescriptions.or(parentPossibleDescriptions);
                // possibleConcepts = termFactory.getConceptIdSet();
                // possibleConcepts.removeAll(refsetMemberSet);
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
                String queryConstraintString = (String) queryConstraint;
                Hits hits;
                try {
                    hits = termFactory.doLuceneSearch(queryConstraintString);

                    if (hits == null || hits.length() == 0) {
                        possibleDescriptions.or(parentPossibleDescriptions);
                        break;
                    }
                    possibleLuceneConcMatches = Terms.get().getEmptyIdSet();
                    possibleLuceneDescMatches = Terms.get().getEmptyIdSet();
                    Iterator iterator = hits.iterator();
                    while (iterator.hasNext()) {
                        Hit next = (Hit) iterator.next();
                        Document doc = next.getDocument();
                        // int dnid = Integer.parseInt(doc.get("dnid"));
                        int cnid = Integer.parseInt(doc.get("cnid"));
                        int dnid = Integer.parseInt(doc.get("dnid"));
                        possibleLuceneDescMatches.setMember(dnid);
                        possibleLuceneConcMatches.setMember(cnid);
                        possibleDescriptions.setMember(dnid);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new TerminologyException(e.getMessage());

                }
            }
            break;
        default:
            throw new RuntimeException("Can't handle queryToken: " + queryToken);
        }
        setPossibleConceptsCount(possibleDescriptions.size());
        return possibleDescriptions;
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_ConfigAceFrame configFrame,
            I_RepresentIdSet parentPossibleConcepts) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships in desc statement unsupported operation.");
    }

    private boolean descriptionIsMemberOf(I_DescriptionTuple descriptionBeingTested) throws IOException,
            TerminologyException {
        return componentIsMemberOf(descriptionBeingTested.getDescId());
    }

    private boolean descriptionTypeIs(I_DescriptionTuple descriptionBeingTested) {
        return descriptionTypeIs((I_GetConceptData) queryConstraint, descriptionBeingTested);
    }

    private boolean descriptionTypeIs(I_GetConceptData requiredDescriptionType,
            I_DescriptionTuple descriptionBeingTested) {
        return descriptionBeingTested.getTypeId() == requiredDescriptionType.getConceptId();
    }

    /**
     * Checks if the description being tested has a description type matching
     * the query constraint. This also checks for the description type's
     * children (depth >= 1);
     */
    private boolean descriptionTypeIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {

        if (descriptionTypeIs(descriptionBeingChecked)) {
            return true;
        }

        return descriptionTypeIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
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
    private boolean descriptionTypeIsDescendentOf(I_GetConceptData requiredType,
            I_DescriptionTuple descriptionBeingChecked) throws IOException, TerminologyException {

        try {
            I_IntSet allowedTypes = getIsAIds();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childDescTypes =
                    requiredType.getDestRelOrigins(currentStatuses, allowedTypes, termFactory.getActiveAceFrameConfig()
                        .getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            // call descriptionTypeIs on each
            for (I_GetConceptData childDescType : childDescTypes) {

                if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
                    return true;
                } else if (descriptionTypeIsDescendentOf(childDescType, descriptionBeingChecked)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean descriptionTypeIsDescendentOf(I_DescriptionTuple descriptionBeingChecked) throws IOException,
            TerminologyException {
        return descriptionTypeIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionTypeIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            // get list of all children of input concept
            Set<? extends I_GetConceptData> childDescTypes =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            // call descriptionTypeIs on each
            for (I_GetConceptData childDescType : childDescTypes) {
                if (descriptionTypeIs(childDescType, descriptionBeingChecked)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }

        return false;
    }

    private boolean descriptionStatusIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        return descriptionBeingChecked.getStatusId() == ((I_GetConceptData) queryConstraint).getConceptId();
    }

    private boolean descriptionStatusIs(I_GetConceptData requiredStatus, I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException {
        return descriptionBeingChecked.getStatusId() == requiredStatus.getConceptId();
    }

    private boolean descriptionStatusIsChildOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            Set<? extends I_GetConceptData> childStatuses =
                    ((I_GetConceptData) queryConstraint).getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData childStatus : childStatuses) {
                if (descriptionStatusIs(childStatus, descriptionBeingChecked)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean descriptionStatusIsDescendentOf(I_DescriptionTuple descriptionBeingChecked)
            throws TerminologyException, IOException {
        return descriptionStatusIsDescendentOf((I_GetConceptData) queryConstraint, descriptionBeingChecked);
    }

    private boolean descriptionStatusIsDescendentOf(I_GetConceptData requiredStatus,
            I_DescriptionTuple descriptionBeingChecked) throws TerminologyException, IOException {
        try {
            I_IntSet allowedTypes = getIsAIds();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            Set<? extends I_GetConceptData> childStatuses =
                    requiredStatus.getDestRelOrigins(currentStatuses, allowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_GetConceptData childStatus : childStatuses) {
                if (descriptionStatusIs(childStatus, descriptionBeingChecked)) {
                    return true;
                } else if (descriptionStatusIsDescendentOf(childStatus, descriptionBeingChecked)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    private boolean descriptionStatusIsKindOf(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException,
            IOException {
        if (descriptionStatusIs(descriptionBeingChecked)) {
            return true;
        }

        return descriptionStatusIsDescendentOf(descriptionBeingChecked);
    }

    private boolean descriptionIs(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        I_DescriptionVersioned queryConstraintDesc = (I_DescriptionVersioned) queryConstraint;
        return descriptionBeingChecked.getDescId() == queryConstraintDesc.getDescId();
    }

    private boolean descriptionRegexMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        String queryConstraintString = (String) queryConstraint;

        if (descriptionBeingChecked.getText().contains(queryConstraintString)) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings( { "deprecation", "unchecked" })
    private boolean descriptionLuceneMatch(I_DescriptionTuple descriptionBeingChecked) throws TerminologyException {
        if (possibleLuceneDescMatches != null) {
            return possibleLuceneDescMatches.isMember(descriptionBeingChecked.getDescId());
        }
        String queryConstraintString = (String) queryConstraint;
        Hits hits;
        try {
            hits = termFactory.doLuceneSearch(queryConstraintString);

            if (hits == null || hits.length() == 0) {
                return false;
            } else {

                Iterator iterator = hits.iterator();
                while (iterator.hasNext()) {
                    Hit next = (Hit) iterator.next();
                    Document doc = next.getDocument();
                    int dnid = Integer.parseInt(doc.get("dnid"));
                    int cnid = Integer.parseInt(doc.get("cnid"));

                    I_DescriptionVersioned description = termFactory.getDescription(dnid, cnid);
                    if (descriptionBeingChecked.getDescId() == description.getDescId()) {
                        return true;
                    }
                }

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }
}
