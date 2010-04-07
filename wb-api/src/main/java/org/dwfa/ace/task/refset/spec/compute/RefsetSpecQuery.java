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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents the data provided by a refset spec. It can hold 0 or more
 * subqueries, and 0 or more statements. The query can be executed by passing in
 * a concept to test.
 * 
 * This class provides a preliminary optimization by providing a function to
 * return the possible concepts that should be tested based on the concepts in
 * the spec.
 * 
 * @author Chrissy Hill, Keith Campbell
 * 
 */
public class RefsetSpecQuery extends RefsetSpecComponent {

    private ArrayList<RefsetSpecQuery> subqueries;
    private ArrayList<RefsetSpecStatement> statements;
    private ArrayList<RefsetSpecComponent> allComponents;

    public ArrayList<RefsetSpecComponent> getAllComponents() {
        return allComponents;
    }

    public enum GROUPING_TYPE {
        OR(RefsetAuxiliary.Concept.REFSET_OR_GROUPING, true),
        AND(RefsetAuxiliary.Concept.REFSET_AND_GROUPING, true),
        CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, true),
        NOT_CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, false),
        CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, true),
        NOT_CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, false);

        private int nid;
        private boolean truth;

        private GROUPING_TYPE(I_ConceptualizeUniversally concept, boolean truth) {
            try {
                this.nid = concept.localize().getNid();
            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.truth = truth;
        }

        public int getNid() {
            return nid;
        }

        public boolean getTruth() {
            return truth;
        }
    };

    private GROUPING_TYPE groupingType;

    private I_TermFactory termFactory;

    private int totalStatementCount;
    private RefsetSpecCalculationOrderComparator executionOrderComparator;
    private RefsetSpecExecutionOrderComparator calculationOrderComparator;
    private boolean allComponentsNeedsResort = true;

    public RefsetSpecQuery(I_GetConceptData groupingConcept) throws TerminologyException, IOException {

        // create query object (statements + any sub-queries)
        executionOrderComparator = new RefsetSpecCalculationOrderComparator();
        calculationOrderComparator = new RefsetSpecExecutionOrderComparator();

        subqueries = new ArrayList<RefsetSpecQuery>();
        statements = new ArrayList<RefsetSpecStatement>();
        allComponents = new ArrayList<RefsetSpecComponent>();

        this.groupingType = getGroupingTypeFromConcept(groupingConcept);
        termFactory = Terms.get();

        totalStatementCount = 0;
    }
    
    public ArrayList<RefsetSpecQuery> getSubqueries() {
        return subqueries;
    }

    public ArrayList<RefsetSpecStatement> getStatements() {
        return statements;
    }

    private GROUPING_TYPE getGroupingTypeFromConcept(I_GetConceptData concept) throws TerminologyException, IOException {
        if (concept.getConceptId() == GROUPING_TYPE.AND.nid) {
            return GROUPING_TYPE.AND;
        } else if (concept.getConceptId() == GROUPING_TYPE.OR.nid) {
            return GROUPING_TYPE.OR;
        } else if (concept.getConceptId() == GROUPING_TYPE.CONCEPT_CONTAINS_REL.nid) {
            return GROUPING_TYPE.CONCEPT_CONTAINS_REL;
        } else if (concept.getConceptId() == GROUPING_TYPE.CONCEPT_CONTAINS_DESC.nid) {
            return GROUPING_TYPE.CONCEPT_CONTAINS_DESC;
        } else {
            throw new TerminologyException("No valid grouping token specified : " + concept.getInitialText());
        }
    }

    public RefsetSpecQuery addSubquery(I_GetConceptData groupingConcept) throws TerminologyException, IOException {
        RefsetSpecQuery subquery = new RefsetSpecQuery(groupingConcept);
        subqueries.add(subquery);
        allComponents.add(subquery);
        allComponentsNeedsResort = true;
        return subquery;
    }

    public RefsetSpecStatement addRelStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint) {
        RefsetSpecStatement statement = new RelationshipStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addConceptStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint) {
        RefsetSpecStatement statement = new ConceptStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint) {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            String constraint) {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {

    	if (allComponentsNeedsResort) {
            Collections.sort(allComponents, calculationOrderComparator);
            allComponentsNeedsResort = false;
    	}

        long startTime = System.currentTimeMillis();
        AceLog.getAppLog().info(">> Start of " + this.toString());
        I_RepresentIdSet possibleConcepts = null;
        // process all statements and subqueries
        switch (groupingType) {
        case AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.and(component.getPossibleConcepts(config, possibleConcepts));
                }
            }

            break;

        case OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling OR.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(config, parentPossibleConcepts));
                }
            }

            break;
        case CONCEPT_CONTAINS_DESC:
        case NOT_CONCEPT_CONTAINS_DESC:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling concept-contains-desc.");
            }
            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(config, parentPossibleConcepts));
                }
            }
        case CONCEPT_CONTAINS_REL:
        case NOT_CONCEPT_CONTAINS_REL:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling concept-contains-rel.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(config, parentPossibleConcepts));
                }
            }

            break;
        default:
            throw new TerminologyException("Unknown grouping type.");
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        AceLog.getAppLog().info(this + " possibleConceptTime: " + minutes + " minutes, " + seconds + " seconds.");
        setPossibleConceptsCount(possibleConcepts.cardinality());
        return possibleConcepts;
    }

    public String toString() {
        try {
            return groupingType.getTruth() + " " + termFactory.getConcept(groupingType.getNid()).getInitialText();
        } catch (Exception e) {
            return "UNKNOWN QUERY";
        }
    }

    /**
     * Executes the specified query.
     * 
     * @return True if query conditions are met, false otherwise.
     * @throws TerminologyException
     * @throws IOException
     */
    public boolean execute(I_AmTermComponent component) throws IOException, TerminologyException {

    	if (allComponentsNeedsResort) {
            Collections.sort(allComponents, executionOrderComparator);
            allComponentsNeedsResort = false;
    	}

        // process all statements and subqueries
        switch (groupingType) {
        case AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (!specComponent.execute(component)) {
                    // can exit the AND early, as at least one statement is
                    // returning false
                    return false;
                }
            }

            // all queries and statements have returned true, therefore AND will
            // return true
            return true;
        case OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling OR.");
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (specComponent.execute(component)) {
                    // exit the OR statement early, as at least one statement
                    // has returned true
                    return true;
                }
            }

            // no queries or statements have returned true, therefore the OR
            // will return false
            return false;
        case CONCEPT_CONTAINS_DESC:
            return executeConceptContainsDesc(component);
        case NOT_CONCEPT_CONTAINS_DESC:
            return !executeConceptContainsDesc(component);
        case CONCEPT_CONTAINS_REL:
            return executeConceptContainsRel(component);
        case NOT_CONCEPT_CONTAINS_REL:
            return !executeConceptContainsRel(component);
        default:
            throw new TerminologyException("Unknown grouping type.");
        }

    }

    private boolean executeConceptContainsDesc(I_AmTermComponent component) throws TerminologyException, IOException {
        if (statements.size() == 0 && subqueries.size() == 0) {
            throw new TerminologyException("Spec is invalid - dangling concept-contains-desc.");
        }

        I_GetConceptData descriptionConcept = (I_GetConceptData) component;
        List<? extends I_DescriptionTuple> descriptionTuples =
                descriptionConcept.getDescriptionTuples(null, null, termFactory.getActiveAceFrameConfig()
                    .getViewPositionSetReadOnly(), true);

        for (I_DescriptionTuple tuple : descriptionTuples) {
            I_DescriptionVersioned descVersioned = tuple.getDescVersioned();
            boolean valid = false;

            for (RefsetSpecStatement statement : statements) {
                if (!statement.execute(descVersioned)) {
                    // can exit the execution early, as at least one statement
                    // is returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            for (RefsetSpecQuery subquery : subqueries) {
                if (!subquery.execute(descVersioned)) {
                    // can exit the execution early, as at least one query is
                    // returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            if (valid) { // this description meets criteria
                return true;
            }
        }

        return false; // no descriptions met criteria
    }

    private boolean executeConceptContainsRel(I_AmTermComponent component) throws TerminologyException, IOException {
        if (statements.size() == 0 && subqueries.size() == 0) {
            throw new TerminologyException("Spec is invalid - dangling concept-contains-rel.");
        }

        I_GetConceptData relQueryConcept = (I_GetConceptData) component;
        Collection<? extends I_RelVersioned> relTuples = relQueryConcept.getSourceRels();

        for (I_RelVersioned versionedTuple : relTuples) {
            boolean valid = false;

            for (RefsetSpecStatement statement : statements) {
                if (!statement.execute(versionedTuple)) {
                    // can exit the execution early, as at least one statement
                    // is returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            for (RefsetSpecQuery subquery : subqueries) {
                if (!subquery.execute(versionedTuple)) {
                    // can exit the execution early, as at least one query is
                    // returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            if (valid) { // this relationship meets criteria
                return true;
            }
        }

        return false; // no relationships met criteria

    }

    /**
     * Negate the current query.
     */
    public void negateQuery() {

        // recursively negate the current query
        switch (groupingType) {
        case AND:
            groupingType = GROUPING_TYPE.OR;
            break;
        case OR:
            groupingType = GROUPING_TYPE.AND;
            break;
        case CONCEPT_CONTAINS_REL:
            groupingType = GROUPING_TYPE.NOT_CONCEPT_CONTAINS_REL;
            break;
        case CONCEPT_CONTAINS_DESC:
            groupingType = GROUPING_TYPE.NOT_CONCEPT_CONTAINS_DESC;
            break;
        case NOT_CONCEPT_CONTAINS_REL:
            groupingType = GROUPING_TYPE.CONCEPT_CONTAINS_REL;
            break;
        case NOT_CONCEPT_CONTAINS_DESC:
            groupingType = GROUPING_TYPE.CONCEPT_CONTAINS_DESC;
            break;
        default:
            break;
        }

        for (RefsetSpecStatement statement : statements) {
            statement.negateStatement();
        }
        for (RefsetSpecQuery query : subqueries) {
            query.negateQuery();
        }
    }

    public int getTotalStatementCount() {
        totalStatementCount = statements.size();
        for (RefsetSpecQuery query : subqueries) {
            totalStatementCount = totalStatementCount + query.getTotalStatementCount();
        }
        return totalStatementCount;
    }

    public boolean isValidQuery() {
        for (RefsetSpecQuery query : subqueries) {
            if (query.getTotalStatementCount() == 0) {
                return false;
            }
            if (!query.isValidQuery()) {
                return false;
            }
        }
        return true;
    }

    public Set<Integer> getNestedRefsets() {
        Set<Integer> results = new HashSet<Integer>();
        for (RefsetSpecStatement statement : statements) {
            switch (statement.getTokenEnum()) {
            case CONCEPT_IS_MEMBER_OF:
            case DESC_IS_MEMBER_OF:
            case REL_IS_MEMBER_OF:
                results.add(((I_AmTermComponent) statement.queryConstraint).getNid());
                break;
            default:
                break;
            }
        }
        for (RefsetSpecQuery query : subqueries) {
            results.addAll(query.getNestedRefsets());
        }
        return results;
    }

    public void setTotalStatementCount(int totalStatementCount) {
        this.totalStatementCount = totalStatementCount;
    }

    public GROUPING_TYPE getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GROUPING_TYPE groupingType) {
        this.groupingType = groupingType;
    }

    public I_RepresentIdSet getPossibleDescriptions(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleDescriptions)
            throws TerminologyException, IOException {

    	if (allComponentsNeedsResort) {
            Collections.sort(allComponents, calculationOrderComparator);
            allComponentsNeedsResort = false;
    	}

        long startTime = System.currentTimeMillis();
        AceLog.getAppLog().info(">> Start of " + this.toString());
        I_RepresentIdSet possibleDescriptions = null;
        // process all statements and subqueries
        switch (groupingType) {
        case AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleDescriptions == null) {
                    possibleDescriptions = component.getPossibleDescriptions(config, parentPossibleDescriptions);
                } else {
                    possibleDescriptions.and(component.getPossibleDescriptions(config, possibleDescriptions));
                }
            }

            break;

        case OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling OR.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleDescriptions == null) {
                    possibleDescriptions = component.getPossibleDescriptions(config, parentPossibleDescriptions);
                } else {
                    possibleDescriptions.or(component.getPossibleDescriptions(config, parentPossibleDescriptions));
                }
            }

            break;
        case CONCEPT_CONTAINS_DESC:
            throw new TerminologyException(
                "Concept-contains-desc is not supported within a description refset calculation.");
        case NOT_CONCEPT_CONTAINS_DESC:
            throw new TerminologyException(
                "NOT Concept-contains-desc is not supported within a description refset calculation.");
        case CONCEPT_CONTAINS_REL:
            throw new TerminologyException(
                "Concept-contains-rel is not supported within a description refset calculation.");
        case NOT_CONCEPT_CONTAINS_REL:
            throw new TerminologyException(
                "NOT Concept-contains-rel is not supported within a description refset calculation.");
        default:
            throw new TerminologyException("Unknown grouping type.");
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        AceLog.getAppLog().info(this + " possibleConceptTime: " + minutes + " minutes, " + seconds + " seconds.");
        setPossibleConceptsCount(possibleDescriptions.cardinality());
        return possibleDescriptions;
    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships unimplemented.");
    }
}
